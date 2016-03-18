package edu.carleton.COMP2601.assignment2.communication;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONEvent extends Event {
	private JSONEventSource es;
	private JSONObject jo;

	public JSONEvent(JSONObject jo, JSONEventSource es) throws JSONException {
		super(jo.getString("type"));
		this.es = es;
		this.jo = jo;
	}

	public String getType() throws JSONException{
		return jo.getString("type");
	}
	public String getBody() throws JSONException{
		return jo.getString("body");
	}
	public String getSource() throws JSONException{
		return jo.getString("source");
	}
	public String getDestination() throws JSONException{
		return jo.getString("destination");
	}
	public String get(String key) {
		try {
			return jo.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}
	public JSONEventSource getES() {
		return es;
	}	
}
