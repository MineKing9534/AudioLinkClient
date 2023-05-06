package de.mineking.audiolink.client.main;

import java.net.URI;

public record AudioLinkSource(String name, String host, boolean https, int port, String password) {
	public URI getURI(String base, String path) {
		return URI.create(base + (https ? "s" : "") + "://" + host + ":" + port + "/" + path);
	}
}
