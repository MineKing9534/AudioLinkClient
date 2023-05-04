package de.mineking.audiolink.client.data;

public enum PlayerLayer {
	ALL(Byte.MIN_VALUE),

	PRIMARY(0),
	SECONDARY(1);

	public final byte id;

	PlayerLayer(int id) {
		this.id = (byte) id;
	}

	public static PlayerLayer get(byte id) {
		for(PlayerLayer t : values()) {
			if(t.id == id) {
				return t;
			}
		}

		return null;
	}
}
