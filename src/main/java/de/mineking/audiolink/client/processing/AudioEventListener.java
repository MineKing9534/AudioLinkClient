package de.mineking.audiolink.client.processing;

import de.mineking.audiolink.client.data.track.AudioTrackEndReason;
import de.mineking.audiolink.client.data.track.CurrentTrackData;
import de.mineking.audiolink.client.data.track.MarkerState;
import de.mineking.audiolink.client.data.track.TrackData;

public abstract class AudioEventListener {
	/**
	 * Called when a track started
	 * @param track the {@link TrackData} of the started track
	 */
	public void onTrackStart(TrackData track) {}

	/**
	 * Called when a track stopped
	 * @param reason the {@link AudioTrackEndReason} that stopped the track
	 */
	public void onTrackEnd(AudioTrackEndReason reason) {}


	/**
	 * Called when the track got stuck during playback
	 */
	public void onTrackStuck() {}

	/**
	 * Called when an exception occurred during the playback
	 * @param message the message of the exception
	 */
	public void onTrackException(String message) {}

	/**
	 * Called when the marker of a track was reached
	 * @param state the {@link MarkerState} of the marker
	 * @param track the {@link CurrentTrackData} of the markers track
	 */
	public void onTrackMarker(MarkerState state, CurrentTrackData track) {}
}
