package edu.sb.skat.persistence;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import edu.sb.skat.util.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
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
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class NetworkNegotiation extends BaseEntity {

	static public enum Type {
		WEB_RTC
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "negotiatorReference", nullable = false, updatable = false, insertable = true)
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
		this(null, Type.WEB_RTC);
	}
	
	public NetworkNegotiation(Person negotiator, Type type) {
		this.negotiator = negotiator;
		this.type = type;
	}

	@JsonbProperty
	public Person getNegotiator() {
		return negotiator;
	}

	protected void setNegotiator(Person value) {
		negotiator = value;
	}

	@JsonbProperty
	public Type getType() {
		return type;
	}

	protected void setType(Type value) {
		type = value;
	}

	@JsonbProperty
	public String getOffer() {
		return offer;
	}

	public void setOffer(String value) {
		offer = value;
	}

	@JsonbProperty
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String value) {
		answer = value;
	}
}