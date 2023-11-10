package edu.sb.skat.persistence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.CacheIndex;

import edu.sb.skat.util.HashCodes;

@Entity
@Table(schema = "skat", name = "Document")
@PrimaryKeyJoinColumn(name = "documentIdentity")
@DiscriminatorValue("Document")
public class Document extends BaseEntity{
	
	static private final byte[] EMPTY_BYTES = {};
	
	@NotNull
	@Column(nullable = false, updatable = false, insertable = true)
	private byte[] content;
	
	@NotNull @Size(min = 64, max = 64)
	@Column(nullable = false, updatable = false, insertable = true, length = 64, unique = true)
	@CacheIndex(updateable = false)
	private String hash;
	
	@NotNull @Size(max = 63)
	@Column(nullable = false, updatable = true)
	private String type;
	
	protected Document() {
		this(EMPTY_BYTES);
		
	}

	public Document(byte[] content) {
		super();
		this.content = content;
		this.hash = HashCodes.sha2HashText(256, content);
		this.type = "application/octet-stream";
	}

	public byte[] getContent() {
		return content;
	}

	protected void setContent(byte[] content) {
		this.content = content;
	}

	public String getHash() {
		return hash;
	}

	protected void setHash(String hash) {
		this.hash = hash;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
