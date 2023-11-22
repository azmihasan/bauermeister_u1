package edu.sb.skat.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
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

import edu.sb.skat.util.JsonProtectedPropertyStrategy;

@Entity
@Table(schema = "skat", name = "Hand")
@PrimaryKeyJoinColumn(name = "handIdentity")
@DiscriminatorValue("Hand")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
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

	@JsonbProperty
	protected Long getGameReference() {
		return this.game == null ? 0 : this.game.getIdentity(); 
	}
	
	@JsonbTransient
	public Game getGame() {
		return game;
	}

	protected void setGame(Game game) {
		this.game = game;
	}

	@JsonbProperty
	protected Long getPlayerReference() {
		return this.player == null ? 0 : this.player.getIdentity(); 
	}
	
	@JsonbTransient
	public Person getPlayer() {
		return player;
	}

	protected void setPlayer(Person player) {
		this.player = player;
	}

	@JsonbProperty
	public Set<Card> getCards() {
		return cards;
	}

	protected void setCards(Set<Card> cards) {
		this.cards = cards;
	}

	@JsonbProperty
	public boolean getSolo() {
		return solo;
	}

	public void setSolo(boolean solo) {
		this.solo = solo;
	}

	@JsonbProperty
	public short getPoints() {
		return points;
	}

	public void setPoints(short points) {
		this.points = points;
	}

	@JsonbProperty
	public Short getBid() {
		return bid;
	}

	public void setBid(Short bid) {
		this.bid = bid;
	}
}