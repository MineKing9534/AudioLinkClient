package de.mineking.audiolink.client.main;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class AudioLinkConfig {
	public final List<AudioLinkSource> sources;
	public final Duration buffer;

	/**
	 * @param buffer the internal buffer of packages to keep. It is important to have some buffer to avoid your internet connection messing with you.
	 * @param sources the {@link AudioLinkSource}s to use. The client will load balance between these
	 */
	public AudioLinkConfig(Duration buffer, List<AudioLinkSource> sources) {
		this.buffer = buffer;
		this.sources = sources;
	}

	public AudioLinkConfig(Duration buffer, AudioLinkSource... sources) {
		this(buffer, Arrays.asList(sources));
	}
}
