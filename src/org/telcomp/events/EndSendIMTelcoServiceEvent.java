package org.telcomp.events;

import java.util.HashMap;
import java.util.Random;
import java.io.Serializable;

public final class EndSendIMTelcoServiceEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	private final long id;
	private boolean imSended;

	public EndSendIMTelcoServiceEvent(HashMap<String, ?> hashMap) {
		id = new Random().nextLong() ^ System.currentTimeMillis();
		this.setImSended((boolean) Boolean.parseBoolean((String) hashMap.get("imSended")));
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		return (o instanceof EndSendIMTelcoServiceEvent) && ((EndSendIMTelcoServiceEvent)o).id == id;
	}
	
	public boolean isImSended() {
		return imSended;
	}

	public void setImSended(boolean imSended) {
		this.imSended = imSended;
	}

	public int hashCode() {
		return (int) id;
	}
	
	public String toString() {
		return "endSendIMEvent[" + hashCode() + "]";
	}
}
