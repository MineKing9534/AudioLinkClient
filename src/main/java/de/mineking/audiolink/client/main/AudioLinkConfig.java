package de.mineking.audiolink.client.main;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class AudioLinkConfig {
	public final List<AudioLinkSource> sources;

	public final Duration buffer;

	public AudioLinkConfig(Duration buffer, List<AudioLinkSource> sources) {
		this.buffer = buffer;
		this.sources = sources;
	}

	public AudioLinkConfig(Duration buffer, AudioLinkSource... sources) {
		this(buffer, Arrays.asList(sources));
	}
}
