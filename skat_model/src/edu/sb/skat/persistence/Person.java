package edu.sb.skat.persistence;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import edu.sb.skat.util.HashCodes;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Embedded;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ElementCollection;
import javax.persistence.CollectionTable;


@Entity
@Table(schema = "skat", name = "Person")
@PrimaryKeyJoinColumn(name = "personIdentity")
@DiscriminatorValue("Person")
public class Person extends BaseEntity{
	
	public enum Group{
		USER,
		ADMIN
	}
	
	@Embeddable
	public class Name implements Comparable<BaseEntity>{
		@Column(nullable = true, updatable = true)
		private String title;
		@Column(nullable = false, updatable = true)
		private String family;
		@Column(nullable = false, updatable = true)
		private String given;
		
		protected Name () {
			
		}
		
		public Name(String title, String family, String given) {
			this.title = title;
			this.family = family;
			this.given = given;
		}

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
	
	@Embeddable
	public class Address implements Comparable<BaseEntity>{
		
		@Column(nullable = false, updatable = true)
		private String street;
		
		@Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Invalid postal code format")
		@Column(nullable = false, updatable = true)
		private String postcode;
		
		@Column(nullable = false, updatable = true)
		private String city;
		
		@Column(nullable = false, updatable = true)
		private String country;
		
		protected Address () {
			
		}
		
		public Address(String street, String postcode, String city, String country) {
			this.street = street;
			this.postcode = postcode;
			this.city = city;
			this.country = country;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getPostcode() {
			return postcode;
		}

		public void setPostcode(String postcode) {
			this.postcode = postcode;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		@Override
		public int compareTo(BaseEntity o) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	private long personId;
	
	@NotNull
	@Email
	@Column(nullable = false, updatable = true, length=128)
	private String email;
	
	@Column(nullable = false, updatable = false, length=64)
	private String passwordHash;
	
	static private final String DEFAULT_PASSWORD_HASH = HashCodes.sha2HashText(256, "password");
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true)
	private Group group;
	
	@Column(nullable = false, updatable = true)
	private long balance;
	
	@Embedded
	@Column(nullable = false, updatable = false)
	private Name name;
	
	@Embedded
	@Column(nullable = false, updatable = false)
	private Address address;
	
	@Pattern(regexp = "\\d{3}-\\d{3}-\\d{4}", message = "Invalid phone number format")
	@Column(nullable = false, updatable = true)
	private Set<String> phones;
	
	@ManyToOne
	@JoinColumn(name = "documentId")
	@Column(updatable = false, insertable = true)
	private Document avatar;
	
	@ManyToOne
	@JoinColumn(name = "skatTableId")
	@Column(nullable = true, updatable = true, insertable = true)
	private SkatTable table;
	
	@NotNull
	@Column(nullable = true, updatable = true, insertable = true)
	private byte tablePosition;
	
	@ManyToOne
	@JoinColumn(name = "networkNegotiationId")
	@ElementCollection
	@CollectionTable
	@Column(nullable = false, updatable = false, insertable = true)
	private Set<NetworkNegotiation> negotiations;
	
	protected Person() {
		//this.group = Group.USER;
		//this.passwordHash = DEFAULT_PASSWORD_HASH;
		//this.name = new Name();
		//this.address = new Address();
		
		//this("") Welchen Sinn hat das???????
		
	}

	public Person(String email, String passwordHash, Group group, long balance, Name name, Address address, Set<String> phones, Document avatar, SkatTable table, byte tablePosition, Set<NetworkNegotiation> negotiations) {
		
		this.email = email;
		this.passwordHash = passwordHash;
		this.group = group;
		this.balance = balance;
		this.name = name;
		this.address = address;
		this.phones = phones;
		this.avatar = avatar;
		this.table = table;
		this.tablePosition = tablePosition;
		this.negotiations = negotiations;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public Name getName() {
		return name;
	}

	protected void setName(Name name) {
		this.name = name;
	}

	public Address getAddress() {
		return address;
	}

	protected void setAddress(Address address) {
		this.address = address;
	}

	public Set<String> getPhones() {
		return phones;
	}

	protected void setPhones(Set<String> phones) {
		this.phones = phones;
	}

	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}

	public SkatTable getTable() {
		return table;
	}

	public void setTable(SkatTable table) {
		this.table = table;
	}

	public byte getTablePosition() {
		return tablePosition;
	}

	public void setTablePosition(byte tablePosition) {
		this.tablePosition = tablePosition;
	}

	public Set<NetworkNegotiation> getNegotiations() {
		return negotiations;
	}

	protected void setNegotiations(Set<NetworkNegotiation> negotiations) {
		this.negotiations = negotiations;
	}







}
