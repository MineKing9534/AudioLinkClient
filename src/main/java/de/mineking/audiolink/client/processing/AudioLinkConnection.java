package de.mineking.audiolink.client.processing;

import de.mineking.audiolink.client.data.*;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AudioLinkConnection extends WebSocketClient {
	private final AudioLinkSource source;
	private final long bufferDuration;

	private boolean started = false;
	private boolean shutdown = false;

	private int provideCounter = 0;

	private final Queue<byte[]> buffer = new LinkedList<>();

	private final Map<PlayerLayer, Set<AudioEventListener>> listeners = new ConcurrentHashMap<>();
	private Runnable disconnectListener;
	private CompletableFuture<Optional<CurrentTrackData>> currentTrack;

	public AudioLinkConnection(AudioLinkClient client, AudioLinkSource source, String clientInfo) {
		super(source.getURI("ws", "gateway"));

		this.source = source;
		this.bufferDuration = client.config.buffer.toMillis() / 20;

		try {
			connectBlocking();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}

		send(AudioLinkClient.gson.toJson(new ClientConfiguration(source.password(), clientInfo)));
		AudioLinkClient.log.info("Connected to source '{}'", source.getURI("ws", "gateway"));
	}

	public record ClientConfiguration(String password, String clientInfo) {
	}

	public AudioLinkSource getSource() {
		return source;
	}

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
					var type = EventType.get(reader.readByte());
					var layer = PlayerLayer.get(reader.readByte());

					switch(type) {
						case START -> {
							var track = new TrackData(reader);
							callListener(layer, listener -> listener.onTrackStart(track));
						}
						case END -> {
							var reason = AudioTrackEndReason.get(reader.readByte());
							callListener(layer, listener -> listener.onTrackEnd(reason));
						}
						case STUCK -> callListener(layer, AudioEventListener::onTrackStuck);
						case EXCEPTION -> {
							var message = reader.readUTF();
							callListener(layer, listener -> listener.onTrackException(message));
						}
						case MARKER -> {
							var state = MarkerState.get(reader.readByte());
							var track = new CurrentTrackData(reader);
							callListener(layer, listener -> listener.onTrackMarker(state, track));
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

	private interface ListenerHandler {
		void handle(AudioEventListener listener) throws IOException;
	}

	private void callListener(PlayerLayer layer, ListenerHandler handler) throws IOException {
		if(layer == PlayerLayer.ALL) {
			for(var listeners : this.listeners.values()) {
				for(var listener : listeners) {
					handler.handle(listener);
				}
			}

			return;
		}

		if(!listeners.containsKey(layer)) {
			return;
		}

		for(var listener : listeners.get(layer)) {
			handler.handle(listener);
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

	public void setDisconnectListener(Runnable listener) {
		this.disconnectListener = listener;
	}

	public void addListener(PlayerLayer layer, AudioEventListener listener) {
		if(layer == PlayerLayer.ALL) {
			for(var l : PlayerLayer.values()) {
				addListener(l, listener);
			}

			return;
		}

		listeners.computeIfAbsent(layer, x -> new HashSet<>());
		listeners.get(layer).add(listener);
	}

	public void removeListener(AudioEventListener listener) {
		listeners.forEach((layer, listeners) -> {
			listeners.remove(listener);
		});
	}

	public void socketRequest(String command, Map<String, Object> args) {
		try {
			send(AudioLinkClient.gson.toJson(new CommandData(command, args)));
		} catch(WebsocketNotConnectedException ignore) {
		}
	}

	public void socketRequest(String command) {
		socketRequest(command, Collections.emptyMap());
	}

	private void playerRequest(PlayerLayer layer, String cmd, Map<String, Object> params) {
		if(layer == null) {
			layer = PlayerLayer.PRIMARY;
		}

		if(layer == PlayerLayer.ALL) {
			throw new IllegalArgumentException();
		}

		params = new HashMap<>(params);
		params.put("layer", layer.id);

		socketRequest(cmd, params);
	}

	public void playTrack(PlayerLayer layer, TrackLoader loader, Duration position, Duration marker) {
		var params = new HashMap<String, Object>();

		params.put("position", position == null ? 0 : position.toMillis());
		params.put("marker", marker == null ? -1 : marker.toMillis());

		try {
			loader.applyData(params);

			playerRequest(layer, "play", params);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void playTrack(PlayerLayer layer, TrackLoader loader) {
		playTrack(layer, loader, null, null);
	}

	public void stopTrack(PlayerLayer layer) {
		playerRequest(layer, "stop", new HashMap<>());
	}

	public void setPaused(PlayerLayer layer, boolean state) {
		playerRequest(layer, "pause", Map.of("state", state));
	}

	public void setVolume(PlayerLayer layer, int volume) {
		playerRequest(layer, "volume", Map.of("volume", volume));
	}

	public void seek(PlayerLayer layer, long position) {
		playerRequest(layer, "seek", Map.of("position", position));
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
	public void onOpen(ServerHandshake data) {}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		shutdown();

		if(remote && disconnectListener != null) {
			disconnectListener.run();
		}
	}

	@Override
	public void onMessage(String s) {}

	@Override
	public void onError(Exception e) {
		AudioLinkClient.log.error("WebSocket error", e);
	}
}
