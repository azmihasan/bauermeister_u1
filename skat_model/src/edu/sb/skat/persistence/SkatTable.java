package edu.sb.skat.persistence;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "skat", name = "SkatTable")
@PrimaryKeyJoinColumn(name = "skatTableId")
@DiscriminatorValue("SkatTable")
public class SkatTable extends BaseEntity{
    private String alias;
    private Document avatar;
    private Set<Person> players;
    private Set<Game> games;
    private long baseValuation;
    
    protected SkatTable() {
    	super();
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