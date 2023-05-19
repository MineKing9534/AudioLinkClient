package de.mineking.audiolink.client.main;

import de.mineking.audiolink.client.main.response.ConnectionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.function.Consumer;

public class AudioLinkSource {
	public final String name;
	public final String host;
	public final boolean https;
	public final int port;
	public final String password;

	public AudioLinkSource(String name, String host, boolean https, int port, String password) {
		this.name = name;
		this.host = host;
		this.https = https;
		this.port = port;
		this.password = password;
	}

	/**
	 * @param protocol the protocol to use, either {@code http} or {@code ws}
	 * @param path     the http path
	 * @return A {@link URI} object representing this source
	 */
	public URI getURI(String protocol, String path) {
		return URI.create(protocol + (https ? "s" : "") + "://" + host + ":" + port + "/" + path);
	}

	/**
	 * @return The amount of connections this source is currently handling or {@code null} if this source is not currently available
	 */
	public Integer getConnectionCount() {
		try {
			return httpRequest("GET", "connection", con -> {}, ConnectionResponse.class).count();
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * @return Whether this source is currently available
	 */
	public boolean isAvailable() {
		return getConnectionCount() != null;
	}

	/**
	 * Performs a http request.
	 *
	 * @param method    the http method
	 * @param path      the http path
	 * @param finalizer a consumer to make some custom configuration for that request
	 * @return the {@link InputStream} of the result
	 * @throws IOException if the request fails
	 */
	public InputStream httpRequest(String method, String path, Consumer<HttpURLConnection> finalizer) throws IOException {
		var connection = (HttpURLConnection) getURI("http", path).toURL().openConnection();

		connection.setRequestMethod(method);
		connection.setRequestProperty("Authorization", password);

		finalizer.accept(connection);

		connection.connect();

		return connection.getInputStream();
	}

	/**
	 * Performs a http request.
	 *
	 * @param method    the http method
	 * @param path      the http path
	 * @param finalizer a consumer to make some custom configuration for that request
	 * @param type      the {@link Class} of the result type
	 * @param <T>       the result type
	 * @return the result
	 * @throws IOException if the request fails
	 */
	public <T> T httpRequest(String method, String path, Consumer<HttpURLConnection> finalizer, Class<T> type) throws IOException {
		try(var reader = new InputStreamReader(httpRequest(method, path, finalizer))) {
			return AudioLinkClient.gson.fromJson(reader, type);
		}
	}
}
