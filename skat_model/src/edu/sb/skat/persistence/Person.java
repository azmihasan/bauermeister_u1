package edu.sb.skat.persistence;

import java.util.Set;

import edu.sb.skat.util.HashCodes;

public class Person extends BaseEntity{

	public enum Group{
		USER,
		ADMIN
	}

	public class Name implements Comparable<BaseEntity>{

		private String title;
		private String family;
		private String given;

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
	
		public class Address implements Comparable<BaseEntity>{

			private String street;
			private String postcode;
			private String city;
			private String country;

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

	private String email;
	private HashCodes passwordHash;
	private Group group;
	private long balance;
	private Name name;
	private Address address;
	private String phones;
	private Document avatar;
	private SkatTable table;
	private byte tablePosition;
	private Set<NetworkNegotiation> negotiations;
	
	protected Person() {
		super();
		
	}

	public Person(String email, HashCodes passwordHash, Group group, long balance, Name name, Address address, String phones, Document avatar, SkatTable table, byte tablePosition, Set<NetworkNegotiation> negotiations) {
		super();
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

	public HashCodes getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(HashCodes passwordHash) {
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

	public String getPhones() {
		return phones;
	}

	protected void setPhones(String phones) {
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
