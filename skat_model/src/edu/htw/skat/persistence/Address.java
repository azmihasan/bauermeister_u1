package edu.htw.skat.persistence;

import java.util.Comparator;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import edu.htw.skat.util.JsonProtectedPropertyStrategy;

@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Address implements Comparable<Address> {

	static private final Comparator<Address> COMPARATOR = Comparator.comparing(Address::getCountry)
			.thenComparing(Address::getCity).thenComparing(Address::getPostcode).thenComparing(Address::getStreet);

	@NotNull
	@Size(max = 63)
	@Column(nullable = false, updatable = true)
	private String street;

	@NotNull
	@Size(max = 15)
	@Column(nullable = false, updatable = true)
	private String postcode;

	@NotNull
	@Size(max = 63)
	@Column(nullable = false, updatable = true)
	private String city;

	@NotNull
	@Size(max = 63)
	@Column(nullable = false, updatable = true)
	private String country;

	@JsonbProperty
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	@JsonbProperty
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	@JsonbProperty
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@JsonbProperty
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public int compareTo(Address other) {
		return COMPARATOR.compare(this, other);
	}
}