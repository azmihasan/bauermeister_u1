package edu.sb.skat.persistence;

public class NetworkNegotiation{

	public enum Type{
	    WEB_RTC
	}
    private Person negotiator;
    private Type type;
    private String offer;
    private String answer; //0..1 ANNOTATION NEEDED
    
    protected NetworkNegotiation() {
    	super();
    }
    
    public Person getNegotiator(){
        return negotiator;
    }
    protected void setNegotiator(Person value){
        negotiator = value;
    }
    public Type getType(){
        return type;
    }
    protected void setType(Type value){
        type = value;
    }

    public String getOffer(){
        return offer;
    }
    public void setOffer(String value){
        offer = value;
    }
    public String getAnswer(){
        return answer;
    }
    public void setAnswer(String value){
        answer = value;
    }
}