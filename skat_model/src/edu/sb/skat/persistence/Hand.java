package edu.sb.skat.persistence;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Entity
@Table(schema = "skat", name = "Hand")
@PrimaryKeyJoinColumn(name = "handId")
@DiscriminatorValue("Hand")
public class Hand extends BaseEntity{

	private Game game;
	
	private Person player;
	
	@NotNull
	private Set<Card> cards;
	
	private Boolean solo;
	
	@NotNull
	private short points;
	
	@NotNull
	private short bid;
	
	protected Hand() {
		super();
	}
	
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



