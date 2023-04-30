package de.mineking.audiolink.client.data.track;

public sealed interface SearchResult permits TrackCollection, SearchResult.NoMatchesResponse, SearchResult.FailedResponse {
	enum SearchResultType {
		TRACK(TrackData.class),
		PLAYLIST(PlaylistData.class),
		NONE(NoMatchesResponse.class),
		FAILED(FailedResponse.class);

		public final Class<? extends SearchResult> clazz;

		SearchResultType(Class<? extends SearchResult> clazz) {
			this.clazz = clazz;
		}

		public static SearchResultType get(String name) {
			for(SearchResultType r : values()) {
				if(r.name().equals(name)) {
					return r;
				}
			}

			return null;
		}
	}

	final class NoMatchesResponse implements SearchResult {
	}

	record FailedResponse(String message) implements SearchResult {
	}
}
