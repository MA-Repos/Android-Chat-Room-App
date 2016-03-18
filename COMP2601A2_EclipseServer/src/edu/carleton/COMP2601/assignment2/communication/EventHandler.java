package edu.carleton.COMP2601.assignment2.communication;

import java.io.IOException;

import org.json.JSONException;

public interface EventHandler {
	public void handleEvent(Event event) throws JSONException, IOException;
}
