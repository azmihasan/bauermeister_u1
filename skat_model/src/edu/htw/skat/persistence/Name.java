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
public class Name implements Comparable<Name> {

	static private final Comparator<Name> COMPARATOR = Comparator
			.comparing(Name::getFamily, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(Name::getTitle)
			.thenComparing(Name::getGiven);

	@Size(max = 15)
	@Column(nullable = true, updatable = true)
	private String title;

	@NotNull
	@Size(max = 31)
	@Column(name = "surname", nullable = false, updatable = true)
	private String family;

	@NotNull
	@Size(max = 31)
	@Column(name = "forename", nullable = false, updatable = true)
	private String given;

	@JsonbProperty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@JsonbProperty
	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	@JsonbProperty
	public String getGiven() {
		return given;
	}

	public void setGiven(String given) {
		this.given = given;
	}

	@Override
	public int compareTo(Name other) {
		return COMPARATOR.compare(this, other);
	}
}