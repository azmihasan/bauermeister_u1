package edu.sb.skat.persistence;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity
@Table(schema = "skat", name = "SkatTable")
@PrimaryKeyJoinColumn(name = "skatTableId")
@DiscriminatorValue("SkatTable")
public class SkatTable extends BaseEntity{
    
	@Column(nullable = false, updatable = true)
	private String alias;
    
	@ManyToOne (optional = false)
	@JoinColumn(name="skatTableId", nullable = false, updatable = false, insertable = true)
	private Document avatar;
    
	@Min(value = 0)
	@Max (value = 6)
	@OneToMany(mappedBy = "SkatTable")
	private Set<Person> players;
    
	@OneToMany(mappedBy = "SkatTable")
	private Set<Game> games;
    
	@Column(nullable = false, updatable = true)
	private long baseValuation;
	
    protected SkatTable() {
    	
    }

    public String getAlias(){
        return alias;
    }
    public void setAlias(String value){
        alias = value;
    }
    public Document getAvatar(){
        return avatar;
    }
    public void setAvatar(Document value){
        avatar = value;
    }

    public Set<Person> getPlayers(){
        return players;
    }
    protected void setPlayers(Set<Person> value){
        players = value;
    }
    public Set<Game> getGames(){
        return games;
    }
    protected void setGames(Set<Game> value){
        games = value;
    }
    public long getBaseValuation(){
        return baseValuation;
    }
    public void setBaseValuation(long value){
        baseValuation = value;
    }
}