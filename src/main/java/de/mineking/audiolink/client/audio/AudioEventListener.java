package de.mineking.audiolink.client.audio;

import de.mineking.audiolink.client.data.track.AudioTrackEndReason;
import de.mineking.audiolink.client.data.track.CurrentTrackData;
import de.mineking.audiolink.client.data.track.MarkerState;
import de.mineking.audiolink.client.data.track.TrackData;

public abstract class AudioEventListener {
	public void onTrackStart(TrackData track) {}

	public void onTrackEnd(AudioTrackEndReason reason) {}

	public void onTrackStuck() {}

	public void onTrackException(String message) {}

	public void onTrackMarker(MarkerState state, CurrentTrackData track) {}

	public void cleanup() {}
}
