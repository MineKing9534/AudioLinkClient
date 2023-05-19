package de.mineking.audiolink.client.main;

import java.net.URI;

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

	public URI getURI(String base, String path) {
		return URI.create(base + (https ? "s" : "") + "://" + host + ":" + port + "/" + path);
	}
}
