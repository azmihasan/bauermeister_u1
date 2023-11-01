package edu.sb.skat.persistence;

import java.util.Set;

public class Game{
    public enum State {
    	DEAL,
    	NEGOTIATE,
    	ACTIVE,
    	DONE
    }

    private State state = State.DEAL;
    private SkatTable table;
    private Set<Hand> hands;
    private Card leftTrickCard;
    private Card middleTrickCard;
    private Card rightTrickCard;

    protected Game() {
    	super();
    }
    
    public Game(State state, SkatTable table, Set<Hand> hands, Card leftTrickCard, Card middleTrickCard, Card rightTrickCard){
        this.state = state;
        this.table = table;
        this.hands = hands;
        this.leftTrickCard = leftTrickCard;
        this.middleTrickCard = middleTrickCard;
        this.rightTrickCard = rightTrickCard;
    }

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public SkatTable getTable() {
		return table;
	}

	protected void setTable(SkatTable table) {
		this.table = table;
	}

	public Set<Hand> getHands() {
		return hands;
	}

	protected void setHands(Set<Hand> hands) {
		this.hands = hands;
	}

	public Card getLeftTrickCard() {
		return leftTrickCard;
	}

	public void setLeftTrickCard(Card leftTrickCard) {
		this.leftTrickCard = leftTrickCard;
	}

	public Card getMiddleTrickCard() {
		return middleTrickCard;
	}

	public void setMiddleTrickCard(Card middleTrickCard) {
		this.middleTrickCard = middleTrickCard;
	}

	public Card getRightTrickCard() {
		return rightTrickCard;
	}

	public void setRightTrickCard(Card rightTrickCard) {
		this.rightTrickCard = rightTrickCard;
	}

}