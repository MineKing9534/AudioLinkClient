package de.mineking.audiolink.client.data;

public interface URLProvider {
	String getUrl();

	static URLProvider ofUrl(String url) {
		return () -> url;
	}
}
