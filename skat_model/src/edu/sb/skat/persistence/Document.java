package edu.sb.skat.persistence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.CacheIndex;

@Entity
@Table(schema = "skat", name = "Document")
@PrimaryKeyJoinColumn(name = "documentIdentity")
@DiscriminatorValue("Document")
public class Document extends BaseEntity{
	
	@NotNull
	@Size(max=512)
	private byte[] content;
	
	@NotNull @Size(min = 64, max = 64)
	@Column(nullable = false, updatable = false, insertable = true, length = 64, unique = true)
	@CacheIndex(updateable = false)
	private String hash;
	
	@Column(nullable = false, updatable = true)
	private String type;
	
	protected Document() {
		super();
		
	}

	public Document(byte[] content, String hash, String type) {
		super();
		this.content = content;
		this.hash = hash;
		this.type = type;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}



}
