package edu.sb.skat.persistence;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "skat", name = "Game")
@PrimaryKeyJoinColumn(name = "gameId")
@DiscriminatorValue("Game")
public class Game extends BaseEntity{
    public enum State {
    	DEAL,
    	NEGOTIATE,
    	ACTIVE,
    	DONE
    }

    private State state;
    
    private SkatTable table;
    
    @NotNull
    private Set<Hand> hands;
    
    @Size(min=0, max=1)
    private Card leftTrickCard;
    
    @Size(min=0, max=1)
    private Card middleTrickCard;
    
    @Size(min=0, max=1)
    private Card rightTrickCard;

    protected Game() {
    	super();
    	this.state = State.DEAL;
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