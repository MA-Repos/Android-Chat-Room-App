package edu.carleton.COMP2601.assignment2.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONEventSource implements EventSource {
	BufferedWriter writer;
	BufferedReader reader;
	Socket s;
	int no;

	public JSONEventSource(String host, int port) throws UnknownHostException,
			IOException {
		OutputStreamWriter osw;
		InputStreamReader isw;

		s = new Socket(host, port);
		osw = new OutputStreamWriter(s.getOutputStream());

		writer = new BufferedWriter(osw);
		isw = new InputStreamReader(s.getInputStream());
		reader = new BufferedReader(isw);
		no = 0;

	}

	public JSONEventSource(Socket s) throws UnknownHostException, IOException {
		reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		no = 0;
	}

	public Event getEvent() throws IOException, ClassNotFoundException,
			JSONException {
		StringBuffer buf = new StringBuffer();
		String line;
		boolean done = false;
		while (!done) {
			line = reader.readLine();
			if (line == null || line.isEmpty())
				done = true;
			else {
				no++;
				System.out.println("["+no+"]"+line);
				buf.append(line);
			}
		}
		if (buf.length()==0)
			return null;
		JSONObject jo = new JSONObject(buf.toString());
		return new JSONEvent(jo, this);
	}

	public void write(JSONObject msg) throws IOException {
		writer.write(msg.toString()+"\n");
		writer.write("\n");
		writer.flush();
	}
}
