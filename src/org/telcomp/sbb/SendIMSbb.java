package org.telcomp.sbb;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.slee.ActivityContextInterface;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;

import net.java.slee.resource.sip.SleeSipProvider;

import org.telcomp.events.EndSendIMTelcoServiceEvent;
import org.telcomp.events.StartSendIMTelcoServiceEvent;

public abstract class SendIMSbb implements javax.slee.Sbb {
	
	private SleeSipProvider sipFactoryProvider;
	private MessageFactory messageFactory;
	private AddressFactory addressFactory;
	private HeaderFactory headerFactory;

	public void onStartSendIMTelcoServiceEvent(StartSendIMTelcoServiceEvent event, ActivityContextInterface aci) {
		System.out.println("*******************************************");
		System.out.println("SendIMTelcoService Invoked");
		System.out.println("Input CallIdHeader = "+event.getCallIdHeader());
		System.out.println("Input FromUserUri = "+event.getFromUserUri());
		System.out.println("Input ToUserUri = "+event.getToUserUri());
		System.out.println("Input Message = "+event.getMessage());
		
		String toUserUri = event.getToUserUri();
		String fromUserUri = null;
		String message = event.getMessage();
		CallIdHeader CallHeader = null;
		String fromUserName = null;
		String toUserName = this.getName(toUserUri);
		
		if(event.getFromUserUri() != null){
			try {
				fromUserUri = event.getFromUserUri();
				fromUserName = this.getName(fromUserUri);
				CallHeader = this.headerFactory.createCallIdHeader(event.getCallIdHeader());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else{
			fromUserUri = "sip:TelcompIM@telcomp.org";
			fromUserName = "Telcomp Hybrid Services";
			CallHeader = sipFactoryProvider.getNewCallId();
		}
		
		Request messageRequest = this.createMessage(toUserName, fromUserName, toUserUri, fromUserUri, message, CallHeader);
		try {
			ClientTransaction ct = sipFactoryProvider.getNewClientTransaction(messageRequest);
			ct.sendRequest();
			ct.terminate();
			HashMap<String, Object> operationInputs = new HashMap<String, Object>();
			operationInputs.put("imSended", (String) "true");
			EndSendIMTelcoServiceEvent EndSendIMTelcoServiceEvent = new EndSendIMTelcoServiceEvent(operationInputs);
			this.fireEndSendIMTelcoServiceEvent(EndSendIMTelcoServiceEvent, aci, null);
			System.out.println("Output IMSended = true");
			System.out.println("*******************************************");
		} catch (Exception e) {
			HashMap<String, Object> operationInputs = new HashMap<String, Object>();
			operationInputs.put("imSended", (String) "false");
			EndSendIMTelcoServiceEvent EndSendIMTelcoServiceEvent = new EndSendIMTelcoServiceEvent(operationInputs);
			this.fireEndSendIMTelcoServiceEvent(EndSendIMTelcoServiceEvent, aci, null);
			System.out.println("Output IMSended = false");
			System.out.println("*******************************************");
		}
		aci.detach(this.sbbContext.getSbbLocalObject());
	}
	
	public Request createMessage(String toUser, String fromUser, String toSipUri, String fromSipUri, String content, CallIdHeader callId){
		Request messageRequest = null;
		try{
			javax.sip.address.Address addressTo = addressFactory.createAddress(toSipUri);
			addressTo.setDisplayName(toUser);
			Address addressFrom = addressFactory.createAddress(fromSipUri);
			addressFrom.setDisplayName(fromUser);
			FromHeader from = headerFactory.createFromHeader(addressFrom, null);
			MaxForwardsHeader maxhead = headerFactory.createMaxForwardsHeader(70);
			SipURI contactURI = addressFactory.createSipURI(((SipURI) from.getAddress().getURI()).getUser(), 
					sipFactoryProvider.getListeningPoints()[0].getIPAddress());
			Address addressContact = addressFactory.createAddress(contactURI);
			CSeqHeader cshead = headerFactory.createCSeqHeader(2L, Request.MESSAGE);
			ContactHeader contact = headerFactory.createContactHeader(addressContact);
			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
			List<ViaHeader> vias = new ArrayList<ViaHeader>(1);
			vias.add(sipFactoryProvider.getLocalVia("UDP", "z9hC4GbK095871331.0"));
			ToHeader to = headerFactory.createToHeader(addressTo, null);			 
			SipURI requestURI = (SipURI)to.getAddress().getURI();
			messageRequest = messageFactory.createRequest(requestURI, Request.MESSAGE, callId, cshead, from, to, vias, maxhead);
			messageRequest.addHeader(contact);
			byte[] contents = content.getBytes();
			messageRequest.setContent(contents, contentTypeHeader);
		} catch (Exception e){
			e.printStackTrace();
		}
		return messageRequest;
	}
	
	private String getName(String prevName){
		return prevName.substring(prevName.indexOf(':')+1, prevName.indexOf('@'));
	}


	
	// TODO: Perform further operations if required in these methods.
	public void setSbbContext(SbbContext context) { 
		this.sbbContext = context;
		try {
			Context ctx = (Context) new InitialContext().lookup("java:comp/env");
			sipFactoryProvider = (SleeSipProvider) ctx.lookup("slee/resources/jainsip/1.2/provider");
			messageFactory = sipFactoryProvider.getMessageFactory();
			addressFactory = sipFactoryProvider.getAddressFactory();
			headerFactory = sipFactoryProvider.getHeaderFactory();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
    public void unsetSbbContext() { this.sbbContext = null; }
    
    // TODO: Implement the lifecycle methods if required
    public void sbbCreate() throws javax.slee.CreateException {}
    public void sbbPostCreate() throws javax.slee.CreateException {}
    public void sbbActivate() {}
    public void sbbPassivate() {}
    public void sbbRemove() {}
    public void sbbLoad() {}
    public void sbbStore() {}
    public void sbbExceptionThrown(Exception exception, Object event, ActivityContextInterface activity) {}
    public void sbbRolledBack(RolledBackContext context) {}
	
	public abstract void fireEndSendIMTelcoServiceEvent(EndSendIMTelcoServiceEvent event, ActivityContextInterface aci, javax.slee.Address address);

	
	/**
	 * Convenience method to retrieve the SbbContext object stored in setSbbContext.
	 * 
	 * TODO: If your SBB doesn't require the SbbContext object you may remove this 
	 * method, the sbbContext variable and the variable assignment in setSbbContext().
	 *
	 * @return this SBB's SbbContext object
	 */
	
	protected SbbContext getSbbContext() {
		return sbbContext;
	}

	private SbbContext sbbContext; // This SBB's SbbContext

}
