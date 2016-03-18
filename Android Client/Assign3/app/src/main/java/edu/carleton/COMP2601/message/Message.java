package edu.carleton.COMP2601.message;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4179774191935207363L;

	//message types
	public static final String DATA = "data";
	public static final String LOGIN = "login";
	public static final String LOGOUT = "logout";
	public static final String DISCONNECT = "disconnect";
	
	//default header fields
	public static final String DEFAULT_SENDER = "unknown";
	public static final String DEFAULT_RECEIVER = "unknown";
    public static final String DEFAULT_TYPE = "unknown";

	
	public Header header;
	public Body body;
	
	public Message() {
		header = new Header();
		body = new Body();
	}
	
	public String toString(){
		return header.toString() + "\n" + body.toString();
	}
}
