package edu.sb.skat.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.CacheIndex;

import edu.sb.skat.util.HashCodes;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Embedded;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.ElementCollection;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;

@Entity
@Table(schema = "skat", name = "Person")
@PrimaryKeyJoinColumn(name = "personIdentity")
@DiscriminatorValue("Person")
public class Person extends BaseEntity {

	static public enum Group {
		USER, ADMIN
	}

	static private final String DEFAULT_PASSWORD_HASH = HashCodes.sha2HashText(256, "changeit");

	@NotNull @Email @Size(max = 128)
	@Column(nullable = false, updatable = true, length = 128, unique = true)
	@CacheIndex(updateable = true)
	private String email;

	@NotNull @Size(max = 64)
	@Column(nullable = false, updatable = true, length = 64)
	private String passwordHash;

	@NotNull @Enumerated(EnumType.STRING)
	@Column(name = "groupAlias", nullable = false, updatable = true)
	private Group group;

	@NotNull
	@Column(nullable = false, updatable = true)
	private long balance;

	@NotNull @Valid @Embedded
	private Name name;

	@NotNull @Valid @Embedded
	private Address address;

	@ElementCollection
	@CollectionTable(
		schema = "skat",
		name = "PersonPhoneAssociation",
		joinColumns = @JoinColumn(name = "personReference", nullable = false, updatable = false, insertable = true)
	)
	@Column(name = "phone", nullable = false, updatable = false, insertable = true)
	private Set<String> phones;

	@ManyToOne
	@JoinColumn(name = "avatarReference", nullable = false, updatable = true)
	private Document avatar;

	@ManyToOne
	@JoinColumn(name = "tableReference", nullable = true, updatable = true)
	private SkatTable table;

	@Min(0)
	@Max(2)
	@Column(nullable = true, updatable = true)
	private Byte tablePosition;

	@NotNull
	@OneToMany(mappedBy = "negotiator", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE })
	private Set<NetworkNegotiation> negotiations;

	public Person() {
		this.passwordHash = DEFAULT_PASSWORD_HASH;
		this.group = Group.USER;
		this.name = new Name();
		this.address = new Address();
		this.phones = new HashSet<String>();
		this.table = null;
		this.tablePosition = null;
		this.negotiations = Collections.emptySet();
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
	
	protected Long getTableReference() {
		return this.table == null ? null : this.table.getIdentity();
	}
}