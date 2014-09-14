package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.List;

public class Result<T> {

	private List<String> messages;
	private List<Entry<T>> entries;

	public Result() {
		entries = new ArrayList<Entry<T>>(); 
	}
	
	public List<String> getMessages() {
		List<String> messages = new ArrayList<>(); 
		for (Entry<T> entry : entries) {
			messages.add(entry.message); 
		}
		return messages;
	}

	public void addMessage(String message) {
		entries.add(new Entry<T>(message, null)); 
	}
	public void addEntry(String message, T object) {
		entries.add(new Entry<T>(message, object)); 
	}

	public boolean hasResult() {
		return (entries.size() > 0);
	}
	public List<Entry<T>> getEntries() {
		return entries;
	}
	public Entry<T> getEntry() {
		if (!hasResult()) return null; 
		else return entries.get(0);
	}
	public String getMessage() {
		if (!hasResult()) return null; 
		else return getEntry().message;
	}

	public class Entry<V> {
		String message;
		V value; 
		public Entry(String message, V value) {
			this.message = message; 
			this.value = value;
		}
	}
}
