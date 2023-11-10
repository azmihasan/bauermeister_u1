package edu.sb.skat.persistence;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "skat", name = "NetworkNegotiation")
@PrimaryKeyJoinColumn(name = "networkNegotiationId")
@DiscriminatorValue("NetworkNegotiation")
public class NetworkNegotiation extends BaseEntity {

	public enum Type{
	    WEB_RTC
	}
	
	@ManyToOne
    private Person negotiator;
    
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
    private Type type;
    
	@Column(nullable = false, updatable = true)
    private String offer;
    
    @Size(min=0, max=1)
    @Column(updatable = true)
    private String answer;
    
    protected NetworkNegotiation() {
    	super();
    	this.negotiator = new Person();
    	this.type = Type.WEB_RTC;
    }
    
    public Person getNegotiator(){
        return negotiator;
    }
    
    protected void setNegotiator(Person value){
        negotiator = value;
    }
    public Type getType(){
        return type;
    }
    protected void setType(Type value){
        type = value;
    }

    public String getOffer(){
        return offer;
    }
    public void setOffer(String value){
        offer = value;
    }
    public String getAnswer(){
        return answer;
    }
    public void setAnswer(String value){
        answer = value;
    }
}