package de.mineking.audiolink.client.data.track;

/**
 * Taken from <a href="https://github.com/Walkyst/lavaplayer-fork">lavaplayer</a>
 */
public enum MarkerState {
	/**
	 * The specified position has been reached with normal playback.
	 */
	REACHED,
	/**
	 * The marker has been removed by setting the marker of the track to null.
	 */
	REMOVED,
	/**
	 * The marker has been overwritten by setting the marker of the track to another non-null marker.
	 */
	OVERWRITTEN,
	/**
	 * A seek was performed which jumped over the marked position.
	 */
	BYPASSED,
	/**
	 * The track was stopped before it ended, before the marked position was reached.
	 */
	STOPPED,
	/**
	 * The playback position was already beyond the marked position when the marker was placed.
	 */
	LATE,
	/**
	 * The track ended without the marker being triggered (either due to an exception or because the track duration was
	 * smaller than the marked position).
	 */
	ENDED;

	public static MarkerState get(byte id) {
		for(MarkerState r : values()) {
			if(r.ordinal() == id) {
				return r;
			}
		}

		return null;
	}
}