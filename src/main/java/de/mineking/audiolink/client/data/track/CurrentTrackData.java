package de.mineking.audiolink.client.data.track;

import java.io.DataInputStream;
import java.io.IOException;

public class CurrentTrackData extends TrackData {
	public final long position;

	public CurrentTrackData(DataInputStream in) throws IOException {
		super(in);

		this.position = in.readLong();
	}
}
