package edu.sb.skat.persistence;

public class Document extends BaseEntity{

	private byte content;
	private String hash;
	private String type;
	
	protected Document() {
		super();
	}

	public Document(byte content, String hash, String type) {
		super();
		this.content = content;
		this.hash = hash;
		this.type = type;
	}

	public byte getContent() {
		return content;
	}

	public void setContent(byte content) {
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
