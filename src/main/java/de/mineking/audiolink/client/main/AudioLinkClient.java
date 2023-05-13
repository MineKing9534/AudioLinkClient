package de.mineking.audiolink.client.main;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.mineking.audiolink.client.data.track.SearchResult;
import de.mineking.audiolink.client.main.response.ConnectionResponse;
import de.mineking.audiolink.client.processing.AudioLinkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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

	/**
	 * @return the configured {@link AudioLinkSource} with the lowest amount of current connections
	 */
	public Optional<AudioLinkSource> findSource() {
		var sources = new LinkedHashMap<AudioLinkSource, Integer>();

		for(var source : config.sources) {
			try {
				var response = httpRequest(source, "GET", "connection", con -> {}, ConnectionResponse.class);

				sources.put(source, response.count());
			} catch(IOException ignored) { }
		}

		return sources.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey);
	}

	/**
	 * @return a {@link AudioLinkSource} that can be used for basic queries.
	 * @apiNote WARNING: this will always return the same source, so don't use this for actual connections! Internally this is for example used for track queries...
	 */
	public Optional<AudioLinkSource> getDefaultSource() {
		if(defaultSource == null) {
			defaultSource = findSource().orElse(null);
		}

		return Optional.ofNullable(defaultSource);
	}

	/**
	 * Tries to find a {@link AudioLinkSource} and creates a new {@link AudioLinkConnection} with that source.
	 * @param clientInfo a string identifying this connection. It will be displayed in the server logs
	 * @return an optional holding the resulting {@link AudioLinkConnection}. This will return an empty optional if none of the configured sources is available
	 */
	public Optional<AudioLinkConnection> connect(String clientInfo) {
		return findSource().map(source -> new AudioLinkConnection(this, source, clientInfo));
	}

	private InputStream httpRequest(AudioLinkSource source, String method, String path, Consumer<HttpURLConnection> finalizer) throws IOException {
		var connection = (HttpURLConnection) source.getURI("http", path).toURL().openConnection();

		connection.setRequestMethod(method);
		connection.setRequestProperty("Authorization", source.password());

		finalizer.accept(connection);

		connection.connect();

		return connection.getInputStream();
	}

	/**
	 * Makes an http request
	 * @param source the {@link AudioLinkSource} to make the request to
	 * @param method the http method
	 * @param path the http path
	 * @param finalizer a consumer to make some custom configuration for that request
	 * @param type the {@link Class} of the result type
	 * @param <T> the result type
	 * @return the result
	 * @throws IOException if the request fails
	 */
	public <T> T httpRequest(AudioLinkSource source, String method, String path, Consumer<HttpURLConnection> finalizer, Class<T> type) throws IOException {
		try(var reader = new InputStreamReader(httpRequest(source, method, path, finalizer))) {
			return AudioLinkClient.gson.fromJson(reader, type);
		}
	}

	/**
	 * Search for tracks using the input query. This will make a request to the default source.
	 * @param query the query string
	 * @param result the result handler
	 * @param error a {@link Runnable} that is executed when something unexpected happens
	 * @see #getDefaultSource()
	 */
	public void searchTrack(String query, Consumer<SearchResult> result, Runnable error) {
		getDefaultSource().ifPresentOrElse(
				source -> {
					try(var reader = new InputStreamReader(httpRequest(source, "GET", "track?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8), con -> {}))) {
						var object = JsonParser.parseReader(reader).getAsJsonObject();

						result.accept(AudioLinkClient.gson.fromJson(object,
								SearchResult.SearchResultType.get(object.get("type").getAsString()).clazz
						));
					} catch(Exception e) {
						error.run();
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

	/**
	 * Shuts down this client
	 */
	public void shutdown() {
		executor.shutdownNow();
	}
}
