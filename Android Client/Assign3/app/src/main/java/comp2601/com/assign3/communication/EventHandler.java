package comp2601.com.assign3.communication;

import java.io.IOException;

import org.json.JSONException;

public interface EventHandler {
	public void handleEvent(Event event) throws JSONException, IOException;
}
