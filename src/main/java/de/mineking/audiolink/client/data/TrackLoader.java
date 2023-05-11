package de.mineking.audiolink.client.data;

import de.mineking.audiolink.client.main.AudioLinkClient;
import de.mineking.audiolink.client.processing.AudioLinkConnection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

public interface TrackLoader {
	/**
	 * Apply the required data to the parameter map of a {@link AudioLinkConnection#playTrack(PlayerLayer, TrackLoader)} call
	 * @param arguments the parameter map to modify
	 * @throws IOException if something went wrong
	 */
	void applyData(Map<String, Object> arguments) throws IOException;

	/**
	 * Load a track from a url. WARNING: This has to be a valid URL. You can NOT use search prefixes here
	 * @param url the url of the track
	 * @return the {@link TrackLoader} that can be used in {@link AudioLinkConnection#playTrack(PlayerLayer, TrackLoader)}
	 * @see AudioLinkClient#searchTrack(String, Consumer, Runnable)
	 */
	static TrackLoader fromURL(String url) {
		return URLProvider.ofUrl(url);
	}

	/**
	 * Load a track from an input stream. This can for example be used if you for example have a mp3-file in your resources that you want to play.
	 * @param stream the {@link InputStream} containing your audio data
	 * @return the {@link TrackLoader} that can be used in {@link AudioLinkConnection#playTrack(PlayerLayer, TrackLoader)}
	 */
	static TrackLoader fromStream(InputStream stream) {
		return arguments -> arguments.put("data", Base64.getEncoder().encodeToString(stream.readAllBytes()));
	}
}
