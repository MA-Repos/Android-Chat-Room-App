package edu.carleton.COMP2601.assignment2;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import edu.carleton.COMP2601.assignment2.communication.Event;
import edu.carleton.COMP2601.assignment2.communication.EventHandler;
import edu.carleton.COMP2601.assignment2.communication.EventSource;
import edu.carleton.COMP2601.assignment2.communication.JSONEvent;
import edu.carleton.COMP2601.assignment2.communication.JSONEventSource;
import edu.carleton.COMP2601.assignment2.communication.NoEventHandler;
import edu.carleton.COMP2601.assignment2.communication.Reactor;
import edu.carleton.COMP2601.assignment2.communication.ThreadWithReactor;

/*
 *  Name: Mir Abbas
 *  
 *  The reactor pattern code is based on the reactor pattern java code provided
 *  by prof. Tony White.
 *  Server runs a reactor server pattern. Accepts messages from clients and converts them to JSON object events
 *  The reactor server is based on the code from edu.carleton.COMP2601.assignment2.communication.  
 *  That was mostly given to use by the professor
 */

public class Server {

	public static int PORT = 2001;
	public Reactor srvReactor;
	public ConcurrentHashMap<String, ThreadWithReactor> connectedClients;
	private ServerSocket srvSocket;

	public void init() {
		srvReactor = new Reactor();
		srvReactor.register(Common.CONNECT_REQUEST, new ConnectRequestHandler());
		srvReactor.register(Common.DISCONNECT_REQUEST, new DisconnetRequestHandler());
		srvReactor.register(Common.MOVE_MESSAGE, new MoveMessageHandler());

		//initialize list of current connected clients
		connectedClients = new ConcurrentHashMap<String, ThreadWithReactor>();

	}

	protected void add(String client, ThreadWithReactor twr) {
		connectedClients.put(client, twr);
	}

	protected void remove(String client) {
		connectedClients.remove(client);
	}

	protected ThreadWithReactor get(String client) {
		return connectedClients.get(client);
	}

	//send USER UPDATE
	protected void broadcast() throws JSONException, IOException {
		JSONObject broadcastMSG = new JSONObject();
		broadcastMSG.put(Common.type, Common.USERS_UPDATED);
		broadcastMSG.put("users", connectedClients.keySet());
		for (String user : connectedClients.keySet()) {
			EventSource es = ((ThreadWithReactor)connectedClients.get(user)).getEventSource();
			((JSONEventSource)es).write(broadcastMSG);
		}
	}
	protected void broadcastMesage(String msgToBroadcast, String source) throws JSONException, IOException {
		JSONObject broadcastMSG = new JSONObject();
		broadcastMSG.put(Common.type, Common.MOVE_MESSAGE);
		broadcastMSG.put(Common.msg, " (Public) "+msgToBroadcast);
		broadcastMSG.put(Common.source, source);

		for (String user : connectedClients.keySet()) {
			EventSource es = ((ThreadWithReactor)connectedClients.get(user)).getEventSource();
			((JSONEventSource)es).write(broadcastMSG);
		}

	}

	protected boolean broadcastDisconnect(String source) throws JSONException, IOException {
		JSONObject broadcastMSG = new JSONObject();
		broadcastMSG.put(Common.type, Common.DISCONNECT_RESPONSE);
		broadcastMSG.put(Common.source, source);
		ThreadWithReactor twr = get(source);
		if(twr !=null){
			((JSONEventSource)twr.getEventSource()).write(broadcastMSG);
			return false;
		}

		return true;
	}

	//connect to port
	void run() throws IOException, ClassNotFoundException, NoEventHandler,
	JSONException {
		srvSocket = new ServerSocket(PORT);

		if (srvSocket != null)
			System.out.println("Server Started!");

		while (true) {
			Socket skt = srvSocket.accept();
			JSONEventSource JSONserverImp = new JSONEventSource(skt);
			ThreadWithReactor twr = new ThreadWithReactor(JSONserverImp,
					srvReactor);
			twr.start();
		}
	}


	public class ConnectRequestHandler implements EventHandler {
		public void handleEvent(Event event) throws JSONException {
			try {
				JSONEvent myJSONEvent = (JSONEvent)event;

				//adding all the users to clients Hashmap
				add((String)myJSONEvent.get("source"), (ThreadWithReactor)Thread.currentThread());

				//Creating the Connected response message to be sent back to server and respond back to sending client
				JSONObject srvJSONResponse = new JSONObject();
				srvJSONResponse.put(Common.type, Common.CONNECTED_RESPONSE);
				myJSONEvent.getES().write(srvJSONResponse);

				//Let all the users know that a new user has joined.  Sends a USER_UPDATE message to client
				String msg = "has joined.";
				String src = myJSONEvent.get("source");
				broadcastMesage(msg, src);
				broadcast();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//disconnect request
	public class DisconnetRequestHandler implements EventHandler {
		public void handleEvent(Event event) throws JSONException, IOException {

			JSONEvent myJSONEvent = (JSONEvent)event;
			ThreadWithReactor twr = get(myJSONEvent.get("source"));
			if(twr != null){
				JSONObject srvJSONResponse = new JSONObject();
				srvJSONResponse.put(Common.type, Common.DISCONNECT_RESPONSE);
				srvJSONResponse.put(Common.source, myJSONEvent.get("source"));

				broadcastMesage(" Disconnected! ", myJSONEvent.get("source"));
				boolean disconnected = broadcastDisconnect(myJSONEvent.get("source"));


				twr.quit();
				remove(myJSONEvent.get("source"));
				broadcast();

			}
		}
	}


	//move on the board message handler
	public class MoveMessageHandler implements EventHandler {
		public void handleEvent(Event event) throws JSONException, IOException {
			JSONEvent myJSONEvent = (JSONEvent)event;
			JSONObject srvJSONResponse = new JSONObject();
			srvJSONResponse.put(Common.type, Common.MOVE_MESSAGE);
			srvJSONResponse.put(Common.source, 	myJSONEvent.get("source"));
			String dest = myJSONEvent.get("destination");

			srvJSONResponse.put(Common.destination, 	dest);	
			srvJSONResponse.put(Common.msg,  " (Private) "+myJSONEvent.get("msg"));

			if(dest.equals("everyone")){
				//				JSONEvent myJSONEvent2 = (JSONEvent)event;
				String msg = myJSONEvent.get(Common.msg);
				String src = myJSONEvent.get(Common.source);
				broadcastMesage(msg, src);

			}else{

				ThreadWithReactor twr = get(dest);
				if (twr == null) {
					srvJSONResponse.put(Common.msg, "Sorry, logged off " + myJSONEvent.get("destination"));
					myJSONEvent.getES().write(srvJSONResponse);		
				} else {
					((JSONEventSource)twr.getEventSource()).write(srvJSONResponse);
				}

			}
		}
	}


	/**
	 * @param args
	 * @throws IOException
	 * @throws JSONException
	 * @throws NoEventHandler
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException,
	ClassNotFoundException, NoEventHandler, JSONException {
		// Startup the server
		Server newServer = new Server();
		newServer.init();
		newServer.run();
	}
}
