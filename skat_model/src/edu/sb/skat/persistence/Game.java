package edu.sb.skat.persistence;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "skat", name = "Game")
@PrimaryKeyJoinColumn(name = "gameIdentity")
@DiscriminatorValue("Game")
public class Game extends BaseEntity{
    
	public enum State {
    	DEAL,
    	NEGOTIATE,
    	ACTIVE,
    	DONE
    }
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private State state;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name="gameIdentity", nullable=false, updatable=false)
    @Column(nullable=false, updatable=false, insertable=true)
    private SkatTable table;
    
    @NotNull
    @OneToMany(mappedBy="Game", cascade= {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    @ElementCollection
    @CollectionTable(name = "game_hands", joinColumns = @JoinColumn(name = "gameIdentity"))
    private Set<Hand> hands;
    
    @Valid
    @ManyToOne
    @JoinColumn(name="gameIdentity", nullable=false, updatable=false)
    @Column(nullable=true)
    private Card leftTrickCard;
    
    @Valid
    @ManyToOne
    @JoinColumn(name="gameIdentity", nullable=false, updatable=false)
    @Column(nullable=true)
    private Card middleTrickCard;
    
    @Valid
    @ManyToOne
    @JoinColumn(name="gameIdentity", nullable=false, updatable=false)
    @Column(nullable=true)
    private Card rightTrickCard;

    protected Game() {
    	super();
    	this.table = new SkatTable();
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