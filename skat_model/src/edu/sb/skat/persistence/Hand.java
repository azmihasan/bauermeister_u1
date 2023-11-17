package edu.sb.skat.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "skat", name = "Hand")
@PrimaryKeyJoinColumn(name = "handIdentity")
@DiscriminatorValue("Hand")
public class Hand extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "gameReference", nullable = false, updatable = false, insertable = true)
	private Game game;

	@ManyToOne
	@JoinColumn(name = "playerReference", nullable = true, updatable = false, insertable = true)
	private Person player;

	@Column(nullable = true, updatable = true)
	private Short bid;
	
	@Column(nullable = false, updatable = true)
	private boolean solo;

	@Column(nullable = false, updatable = true)
	private short points;

	@NotNull
	@ManyToMany
	@JoinTable(
		schema = "skat",
		name = "handCard",
		joinColumns = @JoinColumn(name = "handReference", nullable = false, updatable = false, insertable = true),
		inverseJoinColumns = @JoinColumn(name = "cardReference", nullable = false, updatable = false, insertable = true)
	)
	private Set<Card> cards;

	protected Hand() {
		this(null, null);
	}

	public Hand(Game game, Person player) {
		this.game = game;
		this.player = player;
		this.cards = new HashSet<>();
	}

	public Game getGame() {
		return game;
	}

	protected void setGame(Game game) {
		this.game = game;
	}

	public Person getPlayer() {
		return player;
	}

	protected void setPlayer(Person player) {
		this.player = player;
	}

	public Set<Card> getCards() {
		return cards;
	}

	protected void setCards(Set<Card> cards) {
		this.cards = cards;
	}

	public boolean getSolo() {
		return solo;
	}

	public void setSolo(boolean solo) {
		this.solo = solo;
	}

	public short getPoints() {
		return points;
	}

	public void setPoints(short points) {
		this.points = points;
	}

	public Short getBid() {
		return bid;
	}

	public void setBid(Short bid) {
		this.bid = bid;
	}
}