package edu.htw.skat.persistence;

import static javax.persistence.EnumType.STRING;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import edu.htw.skat.util.Copyright;
import edu.htw.skat.util.JsonProtectedPropertyStrategy;


/**
 * This class models card entities.
 */
@Entity
@Table(schema = "skat", name = "Card", uniqueConstraints = @UniqueConstraint(columnNames = {"suit", "rank"}))
@PrimaryKeyJoinColumn(name = "cardIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year=2022, holders="Sascha Baumeister")
public class Card extends BaseEntity {
	static public enum Rank { SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
	static public enum Suit { DIAMONDS, HEARTS, SPADES, CLUBS }

	@NotNull
	@Enumerated(STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Suit suit;

	@NotNull
	@Enumerated(STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Rank rank;


	/**
	 * Initializes a new instance for JPA, JSON-B and JAX-B.
	 */
	public Card () {
		this(null, null);
	}


	/**
	 * Initializes a new instance associated with the given suit and rank.
	 * @param suit the suit, or {@code null} for none
	 * @param rank the rank, or {@code null} for none
	 */
	public Card (final Suit suit, final Rank rank) {
		this.suit = suit;
		this.rank = rank;
	}


	/**
	 * Returns the points.
	 * @return the points associated with this card
	 */
	@JsonbProperty
	public short getPoints () {
		switch (this.rank) {
			default: return 0;
			case JACK: return 2;
			case QUEEN: return 3;
			case KING: return 4;
			case TEN: return 10;
			case ACE: return 11;
		}
	}


	/**
	 * Returns the suit.
	 * @return the suit
	 */
	@JsonbProperty
	public Suit getSuit () {
		return this.suit;
	}


	/**
	 * Sets the suit.
	 * @param suit the suit
	 */
	protected void setSuit (final Suit suit) {
		this.suit = suit;
	}


	/**
	 * Returns the rank.
	 * @return the rank
	 */
	@JsonbProperty
	public Rank getRank () {
		return this.rank;
	}


	/**
	 * Sets the rank.
	 * @param rank the rank
	 */
	protected void setRank (final Rank rank) {
		this.rank = rank;
	}
}