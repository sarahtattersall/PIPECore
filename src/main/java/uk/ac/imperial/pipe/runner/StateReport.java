package uk.ac.imperial.pipe.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.imperial.state.State;

/**
 * Returns the state of {@link uk.ac.imperial.state.State} as one or more records, sorted by Token id, with a list of place counts for that token, sorted by the PlaceId:
 * Example:  assume two tokens, "Default" and "red", and three places, "P0", "P1", "P2".  Assume the places are marked as follows:  
 * P0: Default=0, red=1; P1: Default=2, red=3; Default=5, red=6
 * Two records will be created:
 * Token    P0  P1  P2 
 * -------------------
 * Default  0   2   5   
 * red      1   3   6
 * 
 * It is assumed that all Places have the same set of tokens, with a non-null count for each token.  If not, an IllegalStateException will be thrown by the constructor.  
 */

public class StateReport {

	private State state;
	private List<Record> records;
	private List<String> places;
	private int numberTokens;
	private SortedSet<String> tokens;
	private Map<String, Map<String, Integer>> map;

	public StateReport(State state) {
		this.state = state; 
		buildReport(); 
	}

	private void buildReport() {
		records = new ArrayList<Record>(); 
		map = state.asMap();
		buildPlaces();
		buildTokens();
		List<Integer> counts = null; 
		Integer count = null; 
		int foundCount = -1; 
		for (String token : tokens) {
			counts = new ArrayList<>(); 
			for (String place: places) {
				count = map.get(place).get(token); 
				foundCount = map.get(place).keySet().size();  
				if (foundCount != numberTokens) throw new IllegalStateException("StateReport:  expected all places to have the same number of tokens: "+numberTokens+" but found: "+foundCount); 
				if (count != null) counts.add(count);
				else throw new IllegalStateException("StateReport:  null count found for token: "+token); 
			}
			records.add(new Record(token, counts)); 
		}
//		List<Integer> counts = null; 
//		List<Map<String, Integer>> tokenCounts = new ArrayList<>(); 
//		for (String place: places) {
//			tokenCounts.add(new TreeMap<String, Integer>(map.get(place))); 
//		}
//		for (int i = 0; i < places.size(); i++) {
//			Map<String, Integer> tokenCount = tokenCounts.get(i); 
//				for (String token : tokenCount.keySet()) {
//					records.add(new Record(places.get(i), token, tokenCount.get(token))); 
//			}
//		}
		
	}
	private void buildTokens() {
		String firstPlace = places.get(0); 
		tokens = new TreeSet<String>(map.get(firstPlace).keySet()); 
		numberTokens = tokens.size(); 
		
	}
	private void buildPlaces() {
		Object[] placeObjects = (new TreeSet<String>(map.keySet())).toArray();
		places = new ArrayList<>(); 
		for (int i = 0; i < placeObjects.length; i++) {
			places.add((String) placeObjects[i]); 
		}
	}
	

	public List<Record> getRecords() {
		return records;
	}
	public List<String> getPlaces() {
		return places; 
	}
	
	public class Record {
		public final String token; 
		private final List<Integer> placeCounts; 

		public Record(String token, List<Integer> placeCounts) {
			this.token = token; 
			this.placeCounts = placeCounts; 
		}

		public List<Integer> getCounts() {
			return placeCounts;
		}
	}

}