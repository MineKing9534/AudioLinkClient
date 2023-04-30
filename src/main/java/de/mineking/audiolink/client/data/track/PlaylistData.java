package de.mineking.audiolink.client.data.track;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public non-sealed class PlaylistData implements TrackCollection {
	public final boolean isSearchResult;
	public final String name;

	public final List<TrackData> tracks;

	public PlaylistData(DataInputStream stream) throws IOException {
		this.isSearchResult = stream.readBoolean();
		this.name = stream.readUTF();

		var temp = new LinkedList<TrackData>();

		for(int i = 0; i < stream.readInt(); i++) {
			temp.add(new TrackData(stream));
		}

		this.tracks = temp;
	}

	@Override
	public List<TrackData> getAllTracks() {
		return tracks;
	}
}
