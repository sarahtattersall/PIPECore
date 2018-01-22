package uk.ac.imperial.pipe.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

public class PetriNetValidationEventHandler implements ValidationEventHandler {

    private static final String FROM_FILE = " from file ";
    private static final String PETRI_NET_VALIDATION_EVENT_HANDLER_ERROR_ATTEMPTING_TO_BUILD = "PetriNetValidationEventHandler error attempting to build ";
    private static final String UNEXPECTED_ELEMENT = "unexpected element";
    private boolean continueProcessing;
    private ValidationEvent event;
    private List<FormattedEvent> formattedEvents;
    private boolean suppressUnexpectedElementMessages;
    private String filename = "";
    private XmlFileEnum xmlFileType;

    public PetriNetValidationEventHandler(boolean continueProcessing, boolean suppressUnexpectedElementMessages) {
        this.continueProcessing = continueProcessing;
        this.suppressUnexpectedElementMessages = suppressUnexpectedElementMessages;
        formattedEvents = new ArrayList<>();
        xmlFileType = XmlFileEnum.PETRI_NET;
    }

    public PetriNetValidationEventHandler() {
        this(false, true);
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        this.event = event;
        // ValidationEventHandler says we must exit on fatal error
        if (event.getSeverity() == event.FATAL_ERROR)
            return false;
        boolean unexpectedElement = saveAndCheckUnexpectedElement(event);
        if ((unexpectedElement) && (!continueProcessing)) {
            return true; // continue only if saw unexpected element
        } else
            return continueProcessing;
    }

    public void printMessages() {
        for (FormattedEvent event : formattedEvents) {
            if (printMessage(event)) {
                System.err.println(event.formattedEvent);
            }
        }
    }

    public String getMessage() {
        String message = "";
        for (FormattedEvent event : formattedEvents) {
            if (printMessage(event)) {
                if (event.formattedMessage.equalsIgnoreCase("")) {
                    message = event.formattedEvent;
                } else {
                    message = event.formattedMessage;
                }
                break;
            }
        }
        return message;
    }

    protected boolean printMessage(FormattedEvent event) {
        if (!suppressUnexpectedElementMessages) {
            return true;
        } else if ((!continueProcessing) && (!event.unexpected)) {
            return true;
        } else
            return false;
    }

    private boolean saveAndCheckUnexpectedElement(ValidationEvent event) {
        if ((event.getLinkedException() == null) && (event.getMessage().startsWith(UNEXPECTED_ELEMENT))) {
            formattedEvents.add(new FormattedEvent(true, formatEvent(), formatMessage()));
            return true;
        } else {
            formattedEvents.add(new FormattedEvent(false, formatEvent(), formatMessage()));
            return false;
        }
    }

    private String formatMessage() {
        String message = "";
        if (event.getLinkedException() != null) {
            message = PETRI_NET_VALIDATION_EVENT_HANDLER_ERROR_ATTEMPTING_TO_BUILD + xmlFileType + FROM_FILE +
                    filename + ": " + event.getLinkedException().getMessage();

        }
        return message;
    }

    private String getAllEvents() {
        StringBuffer sb = new StringBuffer();
        sb.append("");
        for (FormattedEvent event : formattedEvents) {
            if (printMessage(event)) {
                sb.append(event.formattedEvent);
            }
        }
        return sb.toString();
    }

    public String formatEvent() {
        //=======
        //	public String printEvent() {
        //>>>>>>> fixes for issues #22,24.  Update POM to 1.1.0-SNAPSHOT
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
        public String formattedMessage;

        public FormattedEvent(boolean unexpected, String formattedEvent, String formattedMessage) {
            this.unexpected = unexpected;
            this.formattedEvent = formattedEvent;
            this.formattedMessage = formattedMessage;
        }

    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public final void setXmlFileType(XmlFileEnum xmlFileType) {
        this.xmlFileType = xmlFileType;
    }
}
