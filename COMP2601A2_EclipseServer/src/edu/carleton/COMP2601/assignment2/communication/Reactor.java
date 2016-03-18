package edu.carleton.COMP2601.assignment2.communication;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;

public class Reactor implements ReactorInterface {
	
	HashMap<String, EventHandler> map;
	
	public Reactor() {
		map = new HashMap<String, EventHandler>();
	}

	public synchronized void register(String key, EventHandler handler) {
		map.put(key, handler);
	}
	
	public synchronized void deregister(String key) {
		map.remove(key);
	}
	
	@Override
	public void dispatch(Event event) throws NoEventHandler, JSONException, IOException {
		EventHandler handler = map.get(event.type);

		if (handler != null){ 
			handler.handleEvent(event);
		}else{
			throw new NoEventHandler(event.type);
		}
	}
}
