package edu.sb.skat.persistence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "skat", name = "Card")
@PrimaryKeyJoinColumn(name = "cardIdentity")
@DiscriminatorValue("Card")
public class Card extends BaseEntity{

	public enum Suit{
	    DIAMONDS,
	    HEARTS,
	    SPADES,
	    CLUBS
	}

	public enum Rank{
	    SEVEN,
	    EIGHT,
	    NINE,
	    TEN,
	    JACK,
	    QUEEN,
	    KING,
	    ACE
	}
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Suit suit;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rank rank;
	
	protected Card() {}
	
	public Card(Suit suit, Rank rank) {
		this.suit = suit;
		this.rank = rank;
	}

	public Suit getSuit() {
		return suit;
	}
	public void setSuit(Suit suit) {
		this.suit = suit;
	}
	public Rank getRank() {
		return rank;
	}
	public void setRank(Rank rank) {
		this.rank = rank;
	}

}



