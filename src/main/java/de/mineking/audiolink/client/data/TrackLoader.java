package de.mineking.audiolink.client.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

public interface TrackLoader {
	void applyData(Map<String, Object> arguments) throws IOException;

	static TrackLoader fromURL(String url) {
		return URLProvider.ofUrl(url);
	}

	static TrackLoader fromStream(InputStream stream) {
		return arguments -> arguments.put("data", Base64.getEncoder().encodeToString(stream.readAllBytes()));
	}
}
