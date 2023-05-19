package de.mineking.audiolink.client.data;

/**
 * The player layer that you want to access. Most of the time you actually want to use the primary layer.
 *
 * @see #PRIMARY
 * @see #SECONDARY
 */
public enum PlayerLayer {
	/**
	 * Target all layers
	 */
	ALL(Byte.MIN_VALUE),

	/**
	 * The primary layer. This should be used for most of your requests and for simple bots actually the only one.
	 */
	PRIMARY(0),
	/**
	 * A secondary player layer. The data of this layer are mixed on top of the audio of the primary layer.
	 */
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
