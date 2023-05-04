package de.mineking.audiolink.client.data;

import java.util.Map;

public interface URLProvider extends TrackLoader {
	String getUrl();

	@Override
	default void applyData(Map<String, Object> arguments) {
		arguments.put("url", getUrl());
	}

	static URLProvider ofUrl(String url) {
		return () -> url;
	}
}
