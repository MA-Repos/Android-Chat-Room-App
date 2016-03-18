package edu.carleton.COMP2601.assignment2.communication;

import java.io.IOException;

import org.json.JSONException;

public interface EventSource {
	public Event getEvent() throws IOException, ClassNotFoundException, JSONException;
}
