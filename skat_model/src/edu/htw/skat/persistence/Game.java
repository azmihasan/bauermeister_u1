package edu.htw.skat.persistence;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.EnumType.STRING;
import java.util.Collections;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import edu.htw.skat.persistence.Hand.Position;
import edu.htw.skat.util.Copyright;
import edu.htw.skat.util.JsonProtectedPropertyStrategy;


/**
 * This class models skat game entities.
 * @see https://en.wikipedia.org/wiki/Skat_(card_game)
 */
@Entity
@Table(schema = "skat", name = "Game")
@PrimaryKeyJoinColumn(name = "gameIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year=2022, holders="Sascha Baumeister")
public class Game extends BaseEntity {
	static public enum State { DEAL, BET, EXCHANGE, ACTIVE, DONE }
	static public enum Modifier { HAND, POOR, BROKE, OUVERT }
	static public enum Type {
		PASS (false, (byte) 0),
		DIAMONDS (true, (byte) 9),
		HEARTS (true, (byte) 10),
		SPADES (true, (byte) 11),
		CLUBS (true, (byte) 12),
		GRAND (true, (byte) 24),
		NULL (false, (byte) 23);

		private final boolean suited;
		private final byte value;


		/**
		 * Initialized a new instance.
		 * @param suited whether or not the associated games are suited
		 * @param value the base game value
		 */
		private Type (final boolean suited, final byte value) {
			this.suited = suited;
			this.value = value;
		}

		/**
		 * Returns whether or not the associated games are suited.
		 * @return {@code true} if the associated games are suited, {@code false} otherwise
		 */
		public boolean suited () {
			return this.suited;
		}


		/**
		 * Returns the base game value.
		 * @return the base game value
		 */
		public byte value () {
			return this.value;
		}
	}


	@NotNull
	@Enumerated(STRING)
	@Column(nullable = false, updatable = true)
	private State state;

	@Enumerated(STRING)
	@Column(nullable = true, updatable = true)
	private Type type;

	@Enumerated(STRING)
	@Column(nullable = true, updatable = true)
	private Modifier modifier;

	@Enumerated(STRING)
	@Column(nullable = true, updatable = true)
	private Position winner;

	@ManyToOne(optional = false)
	@JoinColumn(name = "tableReference", nullable = false, updatable = false, insertable = true)
	private SkatTable table;

	@ManyToOne(optional = false)
	@JoinColumn(name = "forehandReference", nullable = false, updatable = false, insertable = true, unique = true)
	private Hand forehand;

	@ManyToOne(optional = false)
	@JoinColumn(name = "middlehandReference", nullable = false, updatable = false, insertable = true, unique = true)
	private Hand middlehand;

	@ManyToOne(optional = false)
	@JoinColumn(name = "rearhandReference", nullable = false, updatable = false, insertable = true, unique = true)
	private Hand rearhand;

	@ManyToOne(optional = false)
	@JoinColumn(name = "skatReference", nullable = false, updatable = false, insertable = true, unique = true)
	private Hand skat;

	@NotNull
	@OneToMany(mappedBy = "game", cascade = {REMOVE, REFRESH, MERGE, DETACH})
	private Set<Trick> tricks;


	/**
	 * Initializes a new instance for JPA, JSON-B and JAX-B.
	 */
	protected Game () {
		this(null, null, null, null, null);
	}

	
	/**
	 * Initializes a new instance associated with the given table and hands.
	 * @param table the table, or {@code null} for none
	 * @param forehand the forehand, or {@code null} for none
	 * @param middlehand the middlehand, or {@code null} for none
	 * @param rearhand the rearhand, or {@code null} for none
	 * @param skat the skat, or {@code null} for none
	 */
	public Game (final SkatTable table, final Hand forehand, final Hand middlehand, final Hand rearhand, final Hand skat) {
		this.state = State.DEAL;
		this.type = null;
		this.modifier = null;
		this.winner = null;
		this.table = table;
		this.forehand = forehand;
		this.middlehand = middlehand;
		this.rearhand = rearhand;
		this.skat = skat;
		this.tricks = Collections.emptySet();
	}


	/**
	 * Returns the state.
	 * @return the state
	 */
	@JsonbProperty
	public State getState () {
		return this.state;
	}


	/**
	 * Sets the state.
	 * @param state the state
	 */
	public void setState (final State state) {
		this.state = state;
	}


	/**
	 * Returns the type.
	 * @return the (optional) game type, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Type getType () {
		return this.type;
	}


	/**
	 * Sets the type.
	 * @param type the game type, or {@code null} for none
	 */
	public void setType (final Type type) {
		this.type = type;
	}


	/**
	 * Returns the modifier.
	 * @return the (optional) game modifier, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	public Modifier getModifier () {
		return this.modifier;
	}


	/**
	 * Sets the modifier.
	 * @param modifier the game modifier, or {@code null} for none
	 */
	public void setModifier (final Modifier modifier) {
		this.modifier = modifier;
	}


	/**
	 * Returns the winner.
	 * @return the winner's position, or {@code null} for none
	 */
	public Position getWinner () {
		return this.winner;
	}


	/**
	 * Sets the winner.
	 * @param winner the winner's position, or {@code null} for none
	 */
	public void setWinner (final Position winner) {
		this.winner = winner;
	}


	/**
	 * Returns the table reference. This operation is provided solely for marshaling purposes.
	 * @return the *:1 related table's identity, or {@code null} for none
	 */
	@JsonbProperty(nillable = true)
	protected Long getTableReference () {
		return this.table == null ? null : this.table.getIdentity();
	}


	/**
	 * Returns the table.
	 * @return the *:1 related table
	 */
	@JsonbTransient
	public SkatTable getTable () {
		return this.table;
	}


	/**
	 * Sets the table.
	 * @param table the *:0..1 related table, or {@code null} for none
	 */
	protected void setTable (final SkatTable table) {
		this.table = table;
	}


	/**
	 * Returns the associated forehand.
	 * @return the 1:0..1 related forehand
	 */
	@JsonbTransient
	public Hand getForehand () {
		return this.forehand;
	}


	/**
	 * Sets the associated forehand.
	 * @param forehand the 1:0..1 related forehand
	 */
	protected void setForehand (final Hand forehand) {
		this.forehand = forehand;
	}


	/**
	 * Returns the associated middlehand.
	 * @return the 1:0..1 related middlehand
	 */
	@JsonbTransient
	public Hand getMiddlehand () {
		return this.middlehand;
	}


	/**
	 * Sets the associated middlehand.
	 * @param middleHand the 1:0..1 related middlehand
	 */
	protected void setMiddlehand (final Hand middlehand) {
		this.middlehand = middlehand;
	}


	/**
	 * Returns the associated rearhand.
	 * @return the 1:0..1 related rearhand
	 */
	@JsonbTransient
	public Hand getRearhand () {
		return this.rearhand;
	}


	/**
	 * Sets the associated rearhand.
	 * @param rearhand the 1:0..1 related rearhand
	 */
	protected void setRearhand (final Hand rearhand) {
		this.rearhand = rearhand;
	}


	/**
	 * Returns the associated skat.
	 * @return the *:0..1 related skat
	 */
	@JsonbTransient
	public Hand getSkat () {
		return this.skat;
	}


	/**
	 * Sets the associated skat.
	 * @param trickHand the 1:0..1 related skat
	 */
	protected void setSkat (final Hand skat) {
		this.skat = skat;
	}


	/**
	 * Returns the associated tricks.
	 * @return the 1:* related tricks
	 */
	@JsonbProperty
	public Set<Trick> getTricks () {
		return this.tricks;
	}


	/**
	 * Sets the associated tricks.
	 * @param tricks the 1:* related tricks
	 */
	protected void setTricks (final Set<Trick> tricks) {
		this.tricks = tricks;
	}


	/**
	 * Returns the forehand points.
	 * @return the points accumulated by the associated forehand
	 */
	@JsonbProperty
	public short getForehandPoints () {
		return (short) (this.tricks.stream().filter(trick -> trick.getWinner() == Position.FOREHAND).mapToInt(Trick::getPoints).sum() + (this.forehand.isSolo() ? this.skat.getPoints() : 0));
	}


	/**
	 * Returns the middlehand points.
	 * @return the points accumulated by the associated middlehand
	 */
	@JsonbProperty
	public short getMiddlehandPoints () {
		return (short) (this.tricks.stream().filter(trick -> trick.getWinner() == Position.MIDDLEHAND).mapToInt(Trick::getPoints).sum() + (this.forehand.isSolo() ? this.skat.getPoints() : 0));
	}


	/**
	 * Returns the rearhand points.
	 * @return the points accumulated by the associated rearhand
	 */
	@JsonbProperty
	public short getReadhandPoints () {
		return (short) (this.tricks.stream().filter(trick -> trick.getWinner() == Position.REARHAND).mapToInt(Trick::getPoints).sum() + (this.forehand.isSolo() ? this.skat.getPoints() : 0));
	}
}