package de.mineking.audiolink.client.main;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.mineking.audiolink.client.data.track.SearchResult;
import de.mineking.audiolink.client.processing.AudioLinkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class AudioLinkClient {
	public final static Gson gson = new Gson();
	public final static Logger log = LoggerFactory.getLogger("AudioLinkClient");

	public final AudioLinkConfig config;
	public final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

	private AudioLinkSource defaultSource;

	public AudioLinkClient(AudioLinkConfig config) {
		this.config = config;
	}

	public AudioLinkConfig getConfig() {
		return config;
	}

	public Optional<AudioLinkSource> findSource() {
		var sources = new LinkedHashMap<AudioLinkSource, Integer>();

		for(var source : config.sources) {
			try(var reader = new BufferedReader(httpRequest(source, "GET", "connection", con -> {}))) {
				sources.put(source, Integer.parseInt(reader.readLine()));
			} catch(IOException ignored) {
			}
		}

		return sources.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey);
	}

	public Optional<AudioLinkSource> getDefaultSource() {
		if(defaultSource == null) {
			defaultSource = findSource().orElse(null);
		}

		return Optional.ofNullable(defaultSource);
	}

	public Optional<AudioLinkConnection> connect(String clientInfo) {
		return findSource().map(source -> new AudioLinkConnection(this, source, clientInfo));
	}


	private InputStreamReader httpRequest(AudioLinkSource source, String method, String path, Consumer<HttpURLConnection> finalizer) throws IOException {
		var connection = (HttpURLConnection) source.getURI("http", path).toURL().openConnection();

		connection.setRequestMethod(method);
		connection.setRequestProperty("Authorization", source.password());

		finalizer.accept(connection);

		connection.connect();

		return new InputStreamReader(connection.getInputStream());
	}

	public void searchTrack(String query, Consumer<SearchResult> result, Runnable error) {
		getDefaultSource().ifPresentOrElse(
				source -> {
					try(var reader = httpRequest(source, "GET", "track?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8), con -> {})) {
						var object = JsonParser.parseReader(reader).getAsJsonObject();

						result.accept(AudioLinkClient.gson.fromJson(object,
								SearchResult.SearchResultType.get(object.get("type").getAsString()).clazz
						));
					} catch(Exception e) {
						AudioLinkClient.log.error("Error searching track", e);
					}
				},
				() -> {
					if(error != null) {
						error.run();
					}
				}
		);

	}

	public void shutdown() {
		executor.shutdownNow();
	}
}
