package comp2601.com.assign3.communication;

import java.io.IOException;

import org.json.JSONException;

public interface EventSource {
	public Event getEvent() throws IOException, ClassNotFoundException, JSONException;
}
