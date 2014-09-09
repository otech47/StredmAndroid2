package com.stredm.flume.object;


public class Event {
	private String mId;
	private String mEvent;

	public Event() {
		// default testing values
		this("-1",
			"event");
	}
	
	public Event(String id, String event) {
		setId(id);
		setEvent(event);
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getEvent() {
		return mEvent;
	}
	
	public void setEvent(String event) {
		this.mEvent = event;
	}

}