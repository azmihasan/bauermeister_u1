package edu.sb.skat.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import edu.sb.skat.util.JsonProtectedPropertyStrategy;

@Entity
@Table(schema = "skat", name = "Card")
@PrimaryKeyJoinColumn(name = "cardIdentity")
@DiscriminatorValue("Card")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Card extends BaseEntity {

	static public enum Suit {
		DIAMONDS, HEARTS, SPADES, CLUBS
	}

	static public enum Rank {
		SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Suit suit;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Rank rank;

	protected Card() {
		this(null, null);
	}

	public Card(Suit suit, Rank rank) {
		this.suit = suit;
		this.rank = rank;
	}

	@JsonbProperty
	public Suit getSuit() {
		return suit;
	}

	protected void setSuit(Suit suit) {
		this.suit = suit;
	}

	@JsonbProperty
	public Rank getRank() {
		return rank;
	}

	protected void setRank(Rank rank) {
		this.rank = rank;
	}
}