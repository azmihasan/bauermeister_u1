package edu.sb.skat.persistence;

import java.util.Collections;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
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
public class Game extends BaseEntity {

	public enum State {
		DEAL, NEGOTIATE, ACTIVE, DONE
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true)
	private State state;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "tableReference", nullable = false, updatable = false, insertable = true)
	private SkatTable table;

	@NotNull
	@OneToMany(mappedBy = "Game", cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE,
			CascadeType.DETACH })
	private Set<Hand> hands;

	@ManyToOne
	@JoinColumn(name = "leftTrickCardReference", nullable = true, updatable = true)
	private Card leftTrickCard;

	@ManyToOne
	@JoinColumn(name = "middleTrickCardReference", nullable = true, updatable = true)
	private Card middleTrickCard;

	@ManyToOne
	@JoinColumn(name = "rightTrickCardReference", nullable = true, updatable = true)
	private Card rightTrickCard;

	protected Game() {
		this(null);
	}

	public Game(SkatTable table) {
		this.state = State.DEAL;
		this.table = table;
		this.hands = Collections.emptySet();
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