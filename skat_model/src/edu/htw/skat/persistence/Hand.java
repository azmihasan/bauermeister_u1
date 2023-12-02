package edu.htw.skat.persistence;

import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import edu.htw.skat.util.Copyright;
import edu.htw.skat.util.JsonProtectedPropertyStrategy;


/**
 * This class models hand entities.
 */
@Entity
@Table(schema = "skat", name = "Hand")
@PrimaryKeyJoinColumn(name = "handIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year=2019, holders="Sascha Baumeister")
public class Hand extends BaseEntity {
	static public enum Position { FOREHAND, MIDDLEHAND, REARHAND }

	@Column(nullable = true, updatable = true)
	private Short bid;

	@Column(nullable = false, updatable = true)
	private boolean solo;

	@ManyToOne(optional = true)
	@JoinColumn(name = "playerReference", nullable = true, updatable = false, insertable = true)
	private Person player;

	@NotNull
	@ManyToMany
	@JoinTable(
		schema = "skat",
		name = "HandCardAssociation",
		joinColumns = @JoinColumn(name = "handReference", nullable = false, updatable = false, insertable = true),
		inverseJoinColumns = @JoinColumn(name = "cardReference", nullable = false, updatable = true),
		uniqueConstraints = @UniqueConstraint(columnNames = {"handReference", "cardReference"})
	)
	private Set<Card> cards;


	/**
	 * Initializes a new instance for JPA, JSON-B and JAX-B.
	 */
	protected Hand () {
		this(null);
	}


	/**
	 * Initializes a new instance associated with the given player.
	 * @param player the player, or {@code null} for none
	 */
	public Hand (final Person player) {
		this.bid = 0;
		this.player = player;
		this.cards = new HashSet<>();
	}


	/**
	 * Returns the player reference. This operation is provided solely for marshaling purposes.
	 * @return the *:0..1 related player identity, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	protected Long getPlayerReference () {
		return this.player == null ? null : this.player.getIdentity();
	}


	/**
	 * Returns the player.
	 * @return the *:1 related player, or {@code null} for none
	 */
	@JsonbTransient
	public Person getPlayer () {
		return this.player;
	}


	/**
	 * Sets the player.
	 * @param player the *:1 related player
	 */
	protected void setPlayer (final Person player) {
		this.player = player;
	}


	/**
	 * Returns the table position.
	 * @return the associated player's position at the table, or {@code -1} for none
	 */
	@JsonbProperty
	public byte getTablePosition () {
		return this.player == null || this.player.getTablePosition() == null ? -1 : this.player.getTablePosition();
	}


	/**
	 * Returns whether this hand is solo or not.
	 * @return the solo state
	 */
	@JsonbProperty
	public boolean isSolo () {
		return this.solo;
	}


	/**
	 * Sets whether this hand is solo or not.
	 * @param solo the solo state
	 */
	public void setSolo (final boolean solo) {
		this.solo = solo;
	}


	/**
	 * Returns the bid.
	 * @return the bid, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Short getBid () {
		return this.bid;
	}


	/**
	 * Sets the bid.
	 * @param bid the bid, or {@code null} for none
	 */
	public void setBid (final Short bid) {
		this.bid = bid;
	}


	/**
	 * Returns the associated cards.
	 * @return the *:* related cards
	 */
	@JsonbTransient
	public Set<Card> getCards () {
		return this.cards;
	}


	/**
	 * Sets the associated cards.
	 * @param cards the *:* related cards
	 */
	protected void setCards (final Set<Card> cards) {
		this.cards = cards;
	}


	/**
	 * Returns the points.
	 * @return the sum of all associated card's points
	 */
	@JsonbTransient
	public short getPoints () {
		return (short) this.cards.stream().mapToInt(Card::getPoints).sum();
	}
}