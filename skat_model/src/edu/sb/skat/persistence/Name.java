package edu.sb.skat.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Name implements Comparable<BaseEntity> {
	@Size(max = 15)
	@Column(nullable = true, updatable = true)
	private String title;
	
	@NotNull @Size(max = 31)
	@Column(name = "surname", nullable = false, updatable = true)
	private String family;
	
	@NotNull @Size(max = 31)
	@Column(name = "forename", nullable = false, updatable = true)
	private String given;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGiven() {
		return given;
	}

	public void setGiven(String given) {
		this.given = given;
	}

	@Override
	public int compareTo(BaseEntity o) {
		// TODO Auto-generated method stub
		return 0;
	}
}