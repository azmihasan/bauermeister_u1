package edu.sb.skat.persistence;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "skat", name = "SkatTable")
@PrimaryKeyJoinColumn(name = "tableIdentity")
@DiscriminatorValue("SkatTable")
public class SkatTable extends BaseEntity {

	@NotNull
	@Column(nullable = false, updatable = true, unique = true)
	private String alias;

	@ManyToOne
	@JoinColumn(name = "avatarReference", nullable = false, updatable = true)
	private Document avatar;

	@NotNull
	@OneToMany(mappedBy = "table", cascade = { CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
	private Set<Person> players;
	
	@NotNull
	@OneToMany(mappedBy = "table", cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
	private Set<Game> games;

	@NotNull
	@Column(nullable = false, updatable = true)
	private long baseValuation;
	
	public SkatTable() {
		this.games = Collections.emptySet();
		this.players = Collections.emptySet();
		this.baseValuation = 0;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String value) {
		alias = value;
	}

	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document value) {
		avatar = value;
	}

	public Set<Person> getPlayers() {
		return players;
	}

	protected void setPlayers(Set<Person> value) {
		players = value;
	}

	public Set<Game> getGames() {
		return games;
	}

	protected void setGames(Set<Game> value) {
		games = value;
	}

	public long getBaseValuation() {
		return baseValuation;
	}

	public void setBaseValuation(long value) {
		baseValuation = value;
	}
	
	protected long[] getplayerReferences() {
		return this.players.stream().mapToLong(BaseEntity::getIdentity).sorted().toArray();
	}
	
	protected long[] getGameReferences() {
		return this.games.stream().mapToLong(BaseEntity::getIdentity).sorted().toArray();
	}
}