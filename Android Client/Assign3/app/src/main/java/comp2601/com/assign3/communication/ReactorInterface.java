package comp2601.com.assign3.communication;

import java.io.IOException;

import org.json.JSONException;

public interface ReactorInterface {
	public void register(String type, EventHandler event);
	public void deregister(String type);
	public void dispatch(Event event) throws NoEventHandler, JSONException, IOException;
}
