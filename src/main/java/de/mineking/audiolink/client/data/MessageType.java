package de.mineking.audiolink.client.data;

public enum MessageType {
	AUDIO(0),
	EVENT(1),
	TRACK_INFO(2);

	public final byte id;

	MessageType(int id) {
		this.id = (byte) id;
	}

	public static MessageType get(byte id) {
		for(MessageType t : values()) {
			if(t.id == id) {
				return t;
			}
		}

		return null;
	}
}
