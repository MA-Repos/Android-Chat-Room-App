package edu.carleton.COMP2601.assignment2.communication;

import java.io.IOException;

import org.json.JSONException;

public class ThreadWithReactor extends Thread implements ReactorInterface {
	private boolean running;
	private EventSource source;
	private Reactor reactor;

	public ThreadWithReactor(EventSource source) {
		this.source = source;
		this.running = false;
		this.reactor = new Reactor();
	}

	public ThreadWithReactor(EventSource source, Reactor reactor) {
		this.source = source;
		this.running = false;
		this.reactor = reactor;
	}

	public void quit() {
		running = false;
	}

	public void run() {
		running = source != null;
		while (running) {
			Event event;
			try {
				event = source.getEvent();
				if (event != null) {
					try {
						dispatch(event);
					} catch (NoEventHandler e) {
						running = false;
					} catch (JSONException e) {
						running = false;
					}
				} else
					quit();
			} catch (IOException e1) {
				quit();
			} catch (ClassNotFoundException e1) {
				quit();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void register(String type, EventHandler event) {
		reactor.register(type, event);
	}

	@Override
	public void deregister(String type) {
		reactor.deregister(type);
	}

	@Override
	public void dispatch(Event event) throws NoEventHandler, JSONException, IOException {
		reactor.dispatch(event);
	}

	public EventSource getEventSource() {
		return source;
	}
}
