package edu.carleton.COMP2601.assignment2.communication;

import java.io.IOException;

import org.json.JSONException;

public interface ReactorInterface {
	public void register(String type, EventHandler event);
	public void deregister(String type);
	public void dispatch(Event event) throws NoEventHandler, JSONException, IOException;
}
