package edu.htw.skat.persistence;


/**
 * Game type for Skat games.
 */
public enum Type {
	PASS(false, (byte) 0),
	DIAMONDS(true, (byte) 9),
	HEARTS(true, (byte) 10),
	SPADES(true, (byte) 11),
	CLUBS(true, (byte) 12),
	GRAND(true, (byte) 24),
	NULL(false, (byte) 23);
	
	private final boolean suited;
	private final byte value;

	/**
	 * Initialized a new instance.
	 * @param suited whether or not the game is suited
	 * @param value the base game value
	 */
	private Type (final boolean suited, final byte value) {
		this.suited = suited;
		this.value = value;
	}

	/**
	 * Returns whether or not the game is suited.
	 * @return {@code true} if the game is suited, {@code false} otherwise
	 */
	public boolean isSuited () {
		return this.suited;
	}


	/**
	 * Returns the base game value.
	 * @return the base game value
	 */
	public byte getValue () {
		return this.value;
	}
}
