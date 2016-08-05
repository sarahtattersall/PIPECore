package uk.ac.imperial.pipe.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;


public class PetriNetValidationEventHandler implements ValidationEventHandler {

	private boolean continueProcessing;
	private ValidationEvent event;
	private List<FormattedEvent> formattedEvents;
	private boolean suppressUnexpectedElementMessages;

	public PetriNetValidationEventHandler(boolean continueProcessing, boolean suppressUnexpectedElementMessages) {
		this.continueProcessing = continueProcessing; 
		this.suppressUnexpectedElementMessages = suppressUnexpectedElementMessages; 
		formattedEvents = new ArrayList<>();  
	}

	public PetriNetValidationEventHandler() {
		this(false, true); 
	}
	
	
	@Override
	public boolean handleEvent(ValidationEvent event) {
		this.event = event;
		// ValidationEventHandler says we must exit on fatal error
		if (event.getSeverity() == event.FATAL_ERROR) return false;
		
		boolean unexpectedElement = saveAndCheckUnexpectedElement(event);
		if ((unexpectedElement) && (!continueProcessing)) {
			return true;  // continue only if saw unexpected element 
		}
		else return continueProcessing;
	}

	public void printMessages() {
		for (FormattedEvent event : formattedEvents) {
			if (printMessage(event)) {
				System.err.println(event.formattedEvent); 
			}
		}
	}

	protected boolean printMessage(FormattedEvent event) {
		if (!suppressUnexpectedElementMessages) {
			return true;
		}
		else if ((!continueProcessing) && (!event.unexpected)) {
			return true;
		}
		else return false; 
	}

	private boolean saveAndCheckUnexpectedElement(ValidationEvent event) {
		if ((event.getLinkedException() == null) && (event.getMessage().startsWith("unexpected element"))) {
			formattedEvents.add(new FormattedEvent(true, printEvent())); 
			return true;
		}
		else {
			formattedEvents.add(new FormattedEvent(false, printEvent())); 
			return false;
		}
	}

	public String printEvent() {
		ValidationEventLocator locator = event.getLocator(); 
		StringBuffer sb = new StringBuffer(); 
		sb.append("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: "); 
		sb.append("\nMessage: "); 
		sb.append(event.getMessage()); 
		sb.append("\nObject: "); 
		sb.append((locator.getObject() != null) ? (locator.getObject().toString()) : "null"); 
		sb.append("\nURL: "); 
		sb.append((locator.getURL() != null) ? (locator.getURL().toString()) : "null"); 
		sb.append("\nNode: "); 
		sb.append((locator.getNode() != null) ? (locator.getNode().toString()) : "null"); 
		sb.append("\nLine: "); 
		sb.append(locator.getLineNumber()); 
		sb.append("\nColumn: "); 
		sb.append(locator.getColumnNumber()); 
		sb.append("\nLinked exception: "); 
		sb.append((event.getLinkedException() != null) ? (event.getLinkedException().getMessage()) : "null"); 
		return sb.toString();
	}

	public List<FormattedEvent> getFormattedEvents() {
		return formattedEvents;
	}
	
	protected class FormattedEvent {
		public boolean unexpected; 
		public String formattedEvent; 
		
		public FormattedEvent(boolean unexpected, String formattedEvent) {
			this.unexpected = unexpected; 
			this.formattedEvent = formattedEvent; 
		}
		
	}
}
