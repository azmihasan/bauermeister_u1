package edu.sb.skat.persistence;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "skat", name = "NetworkNegotiation")
@PrimaryKeyJoinColumn(name = "negotiationIdentity")
@DiscriminatorValue("NetworkNegotiation")
public class NetworkNegotiation extends BaseEntity {

	public enum Type {
		WEB_RTC
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "negotiatorReference", nullable = false, updatable = true)
	private Person negotiator;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, insertable = true)
	private Type type;

	@NotNull @Size(max = 2046)
	@Column(nullable = false, updatable = true)
	private String offer;

	@Size(max = 2046)
	@Column(nullable = true, updatable = true)
	private String answer;
	
	protected NetworkNegotiation() {
		this(new Person());
	}
	
	public NetworkNegotiation(Person negotiator) {
		this.negotiator = negotiator;
		this.type = Type.WEB_RTC;
	}

	public Person getNegotiator() {
		return negotiator;
	}

	protected void setNegotiator(Person value) {
		negotiator = value;
	}

	public Type getType() {
		return type;
	}

	protected void setType(Type value) {
		type = value;
	}

	public String getOffer() {
		return offer;
	}

	public void setOffer(String value) {
		offer = value;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String value) {
		answer = value;
	}
}