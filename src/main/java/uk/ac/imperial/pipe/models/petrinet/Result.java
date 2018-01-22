package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.List;

public class Result<T> {

    private List<String> messages;
    private List<ResultEntry<T>> entries;

    public Result() {
        entries = new ArrayList<ResultEntry<T>>();
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        for (ResultEntry<T> entry : entries) {
            messages.add(entry.message);
        }
        return messages;
    }

    public String getAllMessages() {
        if (!hasResult())
            return null;
        else {
            StringBuffer sb = new StringBuffer();
            for (String message : getMessages()) {
                sb.append(message);
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    public void addMessage(String message) {
        entries.add(new ResultEntry<T>(message, null));
    }

    public void addEntry(String message, T object) {
        entries.add(new ResultEntry<T>(message, object));
    }

    public boolean hasResult() {
        return (entries.size() > 0);
    }

    public List<ResultEntry<T>> getEntries() {
        return entries;
    }

    public ResultEntry<T> getEntry() {
        if (!hasResult())
            return null;
        else
            return entries.get(0);
    }

    public String getMessage() {
        if (!hasResult())
            return null;
        else
            return getEntry().message;
    }

    public void addResult(Result<T> resultOne) {
        entries.addAll(resultOne.getEntries());
    }

}
