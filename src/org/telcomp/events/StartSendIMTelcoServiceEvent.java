package org.telcomp.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public final class StartSendIMTelcoServiceEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	private final long id;
	
	private String toUserUri;
	private String fromUserUri;
	private String message;
	private String callId;

	public StartSendIMTelcoServiceEvent(HashMap<String, ?> hashMap) {
		id = new Random().nextLong() ^ System.currentTimeMillis();
		this.toUserUri = (String) hashMap.get("toUserUri");
		this.message = (String) hashMap.get("message");
		this.fromUserUri = (String) hashMap.get("fromUserUri");
		this.callId = (String) hashMap.get("callId");
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		return (o instanceof StartSendIMTelcoServiceEvent) && ((StartSendIMTelcoServiceEvent)o).id == id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String getToUserUri(){
		return this.toUserUri;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public String getFromUserUri(){
		return this.fromUserUri;
	}
	
	public String getCallIdHeader(){
		return this.callId;
	}
	
	public String toString() {
		return "startSendIMEvent[" + hashCode() + "]";
	}
}
