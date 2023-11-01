package edu.sb.skat.persistence;

import java.util.Set;

public class Hand{

	private Game game;
	private Person player;
	private Set<Card> cards;
	private Boolean solo;
	private short points;
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



