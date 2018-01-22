package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import uk.ac.imperial.pipe.runner.StateReport.TokenFiringRecord;

public class FiringWriter implements PropertyChangeListener {

    private OutputStream out;

    public FiringWriter(OutputStream out) {
        this.out = out;
    }

    public FiringWriter(String filename) throws FileNotFoundException {
        this(new FileOutputStream(filename));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(PetriNetRunner.EXECUTION_STARTED)) {
            if (event.getNewValue() instanceof Collection) {
                writeHeader((Collection<String>) event.getNewValue());
            } else
                throw new IllegalArgumentException(
                        "FiringWriter.propertyChange:  expecting event.getNewValue() to return Collection<String> but was: " +
                                event.getNewValue().toString());
            Object round0Firing = event.getOldValue();
            checkAndWriteFiring(round0Firing);
        } else if (event.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
            checkAndWriteFiring(event.getNewValue());
        } else if (event.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
            close();
        }
    }

    private void checkAndWriteFiring(Object object) {
        if (object instanceof Firing) {
            writeFiring((Firing) object);
        } else
            throw new IllegalArgumentException(
                    "FiringWriter.propertyChange:  expecting event.getNewValue() to return Firing but was: " +
                            object.toString());
    }

    private void writeFiring(Firing firing) {
        StateReport report = new StateReport(firing.state);
        for (TokenFiringRecord record : report.getTokenFiringRecords()) {
            StringBuffer sb = new StringBuffer();
            sb.append(firing.round);
            sb.append(",\"");
            sb.append(firing.transition);
            sb.append("\"");
            for (Integer count : record.getCounts()) {
                sb.append(",");
                sb.append(count);
            }
            sb.append("\n");
            write(sb, "writeFiring");
        }
    }

    private void writeHeader(Collection<String> places) {
        StringBuffer sb = new StringBuffer();
        sb.append("\"Round\",\"Transition");
        Iterator<String> it = places.iterator();
        while (it.hasNext()) {
            sb.append("\",\"");
            sb.append(it.next());
        }
        sb.append("\"\n");
        write(sb, "writeHeader");
    }

    private void write(StringBuffer sb, String method) {
        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("FiringWriter." + method + ":  unable to write to output stream.");
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("FiringWriter.close:  output may not have closed properly.");
            System.err.println(e.getMessage());
        }
    }
}
