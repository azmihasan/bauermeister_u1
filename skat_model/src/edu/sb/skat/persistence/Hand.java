package edu.sb.skat.persistence;

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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "skat", name = "Hand")
@PrimaryKeyJoinColumn(name = "handId")
@DiscriminatorValue("Hand")
public class Hand extends BaseEntity{
	
	@ManyToOne (optional = false)
	@JoinColumn(name="handId", nullable = false, updatable = false, insertable = true)
	private Game game;
	
	@ManyToOne (optional = false)
	@JoinColumn(name="handId", nullable = false, updatable = false, insertable = true)
	private Person player;
	
	@NotNull
	@ManyToMany
	@JoinTable(
			schema = "skat",
			name = "Hand",
			joinColumns = @JoinColumn(nullable = false, updatable = false, insertable = true, name = "handId"),
			inverseJoinColumns = @JoinColumn(nullable = false, updatable = false, insertable = true, name = "handId"),
			uniqueConstraints = @UniqueConstraint(columnNames = { "handId", "cardId" })
		)
	private Set<Card> cards;
	
	@Column(nullable = false)
	private Boolean solo;
	
	@Column(nullable = true, updatable = true)
	private short points;
	
	@Column(nullable = true, updatable = true)
	private short bid;
	
	protected Hand() {}
	
	public Hand(Game game, Person player, Set<Card> cards, Boolean solo, short points, short bid){
		this.game = game;
		this.player = player;
		this.cards = cards;
		this.solo = solo;
		this.points = points;
		this.bid = bid;
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
	public Boolean getSolo() {
		return solo;
	}
	public void setSolo(Boolean solo) {
		this.solo = solo;
	}
	public short getPoints() {
		return points;
	}
	public void setPoints(short points) {
		this.points = points;
	}
	public short getBid() {
		return bid;
	}
	public void setBid(short bid) {
		this.bid = bid;
	}
}



