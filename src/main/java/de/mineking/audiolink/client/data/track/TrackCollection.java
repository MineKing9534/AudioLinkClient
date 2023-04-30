package de.mineking.audiolink.client.data.track;

import de.mineking.audiolink.client.data.URLProvider;

import java.util.List;
import java.util.function.Function;

public sealed interface TrackCollection extends SearchResult, URLProvider permits TrackData, PlaylistData {
	List<TrackData> getAllTracks();

	default TrackData getFirstTrack() {
		return getAllTracks().get(0);
	}

	default <T> T getTracks(Function<TrackData, T> trackBuilder, Function<PlaylistData, T> playlistBuilder) {
		if(this instanceof TrackData t) {
			return trackBuilder.apply(t);
		}

		else if(this instanceof PlaylistData p) {
			return p.isSearchResult
					? trackBuilder.apply(getFirstTrack())
					: playlistBuilder.apply(p);
		}

		return null;
	}

	@Override
	default String getUrl() {
		return getFirstTrack().url;
	}
}
