package edu.carleton.COMP2601.message;

import java.io.Serializable;

public class Header implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5089357238869549961L;

	public String sender;	// Identity of sender; e.g., Bob
	public String receiver;	// Identity of receiver; e.g., Lou
	public String type;	// Type of message e.g. login, data, ...
	
	public Header() {
		sender = Message.DEFAULT_SENDER;
		receiver = Message.DEFAULT_RECEIVER;
		type = Message.DEFAULT_TYPE;
	}
	public String toString(){
		return "HEADER: sender:" + sender + " receiver:" + receiver + " type: " + type;
	}
}

