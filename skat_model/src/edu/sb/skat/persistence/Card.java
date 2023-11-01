package edu.sb.skat.persistence;

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

	private Suit suit;
	private Rank rank;
	
	protected Card() {
		super();
	}
	
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



