package de.mineking.audiolink.client.audio;

import de.mineking.audiolink.client.data.CommandData;
import de.mineking.audiolink.client.data.EventType;
import de.mineking.audiolink.client.data.MessageType;
import de.mineking.audiolink.client.data.URLProvider;
import de.mineking.audiolink.client.data.track.AudioTrackEndReason;
import de.mineking.audiolink.client.data.track.CurrentTrackData;
import de.mineking.audiolink.client.data.track.MarkerState;
import de.mineking.audiolink.client.data.track.TrackData;
import de.mineking.audiolink.client.main.AudioLinkClient;
import de.mineking.audiolink.client.main.AudioLinkSource;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AudioLinkConnection extends WebSocketClient {
	private final AudioLinkClient client;
	private final AudioLinkSource source;
	private final long bufferDuration;

	private boolean started = false;
	private boolean shutdown = false;

	private int provideCounter = 0;

	private final Queue<byte[]> buffer = new LinkedList<>();

	private final Set<AudioEventListener> listeners = new HashSet<>();
	private CompletableFuture<Optional<CurrentTrackData>> currentTrack;

	public AudioLinkConnection(AudioLinkClient client, AudioLinkSource source) {
		super(source.getURI("ws", "gateway"));

		this.client = client;
		this.source = source;
		this.bufferDuration = client.config.buffer.toMillis() / 20;

		try {
			connectBlocking();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}

		send(AudioLinkClient.gson.toJson(new ClientConfiguration(source.password(), bufferDuration)));
		AudioLinkClient.log.info("Connected to source '{}'", source.getURI("ws", "gateway"));
	}

	public record ClientConfiguration(String password, long buffer) {}

	@Override
	public void onMessage(ByteBuffer input) {
		var reader = new DataInputStream(new ByteArrayInputStream(input.array()));

		try {
			switch(MessageType.get(reader.readByte())) {
				case AUDIO -> {
					var data = reader.readAllBytes();

					if(data.length == 0) {
						this.buffer.add(null);
					}

					else {
						this.buffer.add(data);
					}
				}

				case EVENT -> {
					for(AudioEventListener listener : listeners) {
						switch(EventType.get(reader.readByte())) {
							case START -> listener.onTrackStart(new TrackData(reader));
							case END -> listener.onTrackEnd(AudioTrackEndReason.get(reader.readByte()));
							case STUCK -> listener.onTrackStuck();
							case EXCEPTION -> listener.onTrackException(reader.readUTF());
							case MARKER -> listener.onTrackMarker(MarkerState.get(reader.readByte()), new CurrentTrackData(reader));
						}
					}
				}

				case TRACK_INFO -> {
					if(currentTrack != null) {
						currentTrack.complete(
								input.capacity() == 1
										? Optional.empty()
										: Optional.of(new CurrentTrackData(reader))
						);
						currentTrack = null;
					}
				}
			}
		} catch(Exception e) {
			AudioLinkClient.log.error("Exception", e);
		}
	}

	public void shutdown() {
		if(shutdown) {
			return;
		}

		AudioLinkClient.log.info("Disconnected Client with Server '{}'", source.host() + ":" + source.port());

		shutdown = true;
		close();
	}

	public void addListener(AudioEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(AudioEventListener listener) {
		listeners.remove(listener);
	}

	public void socketRequest(String command, Map<String, Object> args) {
		try {
			send(AudioLinkClient.gson.toJson(new CommandData(command, args)));
		} catch(WebsocketNotConnectedException ignore) {}
	}

	public void socketRequest(String command) {
		socketRequest(command, Collections.emptyMap());
	}

	public void playTrack(URLProvider url, Duration position, Duration marker) {
		socketRequest("play",
				Map.of(
						"url", url.getUrl(),
						"position", position.toMillis(),
						"marker", marker.toMillis()
				)
		);
	}

	public void playTrack(URLProvider url) {
		socketRequest("play", Map.of("url", url.getUrl()));
	}

	public void stopTrack() {
		socketRequest("stop");
	}

	public void setPaused(boolean state) {
		socketRequest("pause", Map.of("state", state));
	}

	public void setVolume(int volume) {
		socketRequest("volume", Map.of("volume", volume));
	}

	public void seek(long position) {
		socketRequest("seek", Map.of("position", position));
	}

	public CompletableFuture<Optional<CurrentTrackData>> getCurrentTrack() {
		if(currentTrack == null) {
			currentTrack = new CompletableFuture<>();
			socketRequest("current");
		}

		return currentTrack;
	}

	public synchronized byte[] provide() {
		if(!started) {
			socketRequest("stream");
			started = true;
		}

		if(provideCounter++ >= 50) {
			var diff = this.buffer.size() - bufferDuration;

			if(Math.abs(diff) >= 10) {
				socketRequest("bufferCheck", Map.of("difference", diff));
			}

			provideCounter = 0;
		}

		return this.buffer.poll();
	}

	@Override
	public void onOpen(ServerHandshake data) { }

	@Override
	public void onClose(int code, String reason, boolean remote) {
		shutdown();

		if(remote) {
			listeners.forEach(AudioEventListener::cleanup);
		}
	}

	@Override
	public void onMessage(String s) { }

	@Override
	public void onError(Exception e) {
		AudioLinkClient.log.error("WebSocket error", e);
	}
}
