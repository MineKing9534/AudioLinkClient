package de.mineking.audiolink.client.data.track;

import de.mineking.audiolink.client.data.URLProvider;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public non-sealed class TrackData implements URLProvider, TrackCollection {
	public String url;
	public String title;
	public String author;
	public String artworkUrl;

	public long length;
	public boolean isStream;

	public String identifier;
	public String isrc;

	public TrackData(String url, String title, String author, String artworkUrl, long length, boolean isStream, String identifier, String isrc) {
		this.url = url;
		this.title = title;
		this.author = author;
		this.artworkUrl = artworkUrl;
		this.length = length;
		this.isStream = isStream;
		this.identifier = identifier;
		this.isrc = isrc;
	}

	public TrackData(TrackData data) {
		this.url = data.url;
		this.title = data.title;
		this.author = data.author;
		this.artworkUrl = data.artworkUrl;
		this.length = data.length;
		this.isStream = data.isStream;
		this.identifier = data.identifier;
		this.isrc = data.isrc;
	}

	public TrackData(DataInputStream in) throws IOException {
		this.url = in.readUTF();
		this.title = in.readUTF();
		this.author = in.readUTF();
		this.artworkUrl = in.readUTF();
		this.length = in.readLong();
		this.isStream = in.readBoolean();
		this.identifier = in.readUTF();
		this.isrc = in.readUTF();
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeUTF(url);
		out.writeUTF(title);
		out.writeUTF(author);
		out.writeUTF(artworkUrl != null ? artworkUrl : "");
		out.writeLong(length);
		out.writeBoolean(isStream);
		out.writeUTF(identifier);
		out.writeUTF(isrc != null ? isrc : "");
	}

	public byte[] getData() throws IOException {
		var baos = new ByteArrayOutputStream();
		var temp = new DataOutputStream(baos);

		write(temp);

		return baos.toByteArray();
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public List<TrackData> getAllTracks() {
		return Collections.singletonList(this);
	}
}
