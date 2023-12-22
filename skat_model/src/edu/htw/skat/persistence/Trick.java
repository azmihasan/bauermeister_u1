package edu.htw.skat.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import edu.htw.skat.persistence.Hand.Position;
import edu.htw.skat.util.Copyright;
import edu.htw.skat.util.JsonProtectedPropertyStrategy;


/**
 * This class models trick entities.
 */
@Entity
@Table(schema = "skat", name = "Trick")
@PrimaryKeyJoinColumn(name = "trickIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year=2023, holders="Sascha Baumeister")
public class Trick extends BaseEntity {
	@ManyToOne(optional = false)
	@JoinColumn(name = "gameReference", nullable = false, updatable = false, insertable = true)
	private Game game;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Position lead;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = true, updatable = true)
	private Position winner;

	@ManyToOne(optional = true)
	@JoinColumn(name = "firstCardReference", nullable = false, updatable = true)
	private Card firstCard;

	@ManyToOne(optional = true)
	@JoinColumn(name = "secondCardReference", nullable = false, updatable = true)
	private Card secondCard;

	@ManyToOne(optional = true)
	@JoinColumn(name = "thirdCardReference", nullable = false, updatable = true)
	private Card thirdCard;


	/**
	 * Initializes a new instance for JPA, JSON-B and JAX-B.
	 */
	protected Trick () {
		this(null, Position.FOREHAND);
	}


	/**
	 * Initializes a new instance associated with the given game.
	 * @param game the game, or {@code null} for none
	 */
	public Trick (final Game game, final Position lead) {
		this.game = game;
		this.lead = lead;
	}


	/**
	 * Returns the points.
	 * @return the points associated with this trick
	 */
	@JsonbProperty
	public short getPoints () {
		short points = 0;
		if (this.firstCard != null) points += this.firstCard.getPoints();
		if (this.secondCard != null) points += this.secondCard.getPoints();
		if (this.thirdCard != null) points += this.thirdCard.getPoints();
		return points;
	}


	/**
	 * Returns the game reference. This operation is provided solely for marshaling purposes.
	 * @return the *:1 related game identity, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	protected Long getGameReference () {
		return this.game == null ? null : this.game.getIdentity();
	}


	/**
	 * Returns the game.
	 * @return the *:1 related game
	 */
	@JsonbTransient
	public Game getGame () {
		return this.game;
	}



	/**
	 * Sets the game.
	 * @param game the *:1 related game
	 */
	protected void setGame (final Game game) {
		this.game = game;
	}


	/**
	 * Returns the lead.
	 * @return the lead position
	 */
	@JsonbProperty
	public Position getLead () {
		return this.lead;
	}


	/**
	 * Sets the lead.
	 * @param lead the lead position
	 */
	protected void setLead (final Position lead) {
		this.lead = lead;
	}


	/**
	 * Returns the winner, equaling the follow-up trick's lead.
	 * @return the winner's position, or {@code null} for none 
	 */
	@JsonbProperty(nillable = true)
	public Position getWinner () {
		return this.winner;
	}


	/**
	 * Sets the winner, equaling the follow-up trick's lead.
	 * @param winner the winner's position, or {@code null} for none
	 */
	public void setWinner (final Position winner) {
		this.winner = winner;
	}


	/**
	 * Returns the first card.
	 * @return the *:1 related first card, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Card getFirstCard () {
		return this.firstCard;
	}



	/**
	 * Sets the first card.
	 * @param firstCard the *:1 related first card, or {@code null} for none
	 */
	public void setFirstCard (final Card firstCard) {
		this.firstCard = firstCard;
	}



	/**
	 * Returns the second card.
	 * @return the *:1 related second card, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Card getSecondCard () {
		return this.secondCard;
	}



	/**
	 * Sets the second card.
	 * @param secondCard the *:1 related second card, or {@code null} for none
	 */
	public void setSecondCard (final Card secondCard) {
		this.secondCard = secondCard;
	}



	/**
	 * Returns the third card.
	 * @return the *:1 related third card, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Card getThirdCard () {
		return this.thirdCard;
	}


	/**
	 * Sets the third card.
	 * @param rightCard the *:1 related third card, or {@code null} for none
	 */
	public void setThirdCard (final Card thirdCard) {
		this.thirdCard = thirdCard;
	}
}