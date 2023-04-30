package de.mineking.audiolink.client.data;

public enum EventType {
	START(0),
	END(1),
	STUCK(2),
	EXCEPTION(3),
	MARKER(4);

	private final byte id;

	EventType(int id) {
		this.id = (byte) id;
	}

	public static EventType get(byte id) {
		for(EventType t : values()) {
			if(t.id == id) {
				return t;
			}
		}

		return null;
	}
}