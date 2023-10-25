package org.vanted.addons.matrix.keggUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.graffiti.util.Pair;

import java.util.Map.Entry;

public class MatchingTableMap {
	private BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> map = new DualHashBidiMap<>();
	private ArrayList<Pair<String, String>> firstPositions = new ArrayList<>();

	
	/**
	 * constructs an empty table
	 */
	public MatchingTableMap() {
		super();
	}

	/**
	 * constructs a table with the given values finds matchings between them
	 * firstPosition and/or secondPosition can be null
	 * 
	 * @param firstPosition Compound names and IDs from experiment data
	 * @param secondPosition Compound names and IDs from KEGG pathway
	 */
	public MatchingTableMap(ArrayList<Pair<String, String>> firstPosition,
			ArrayList<Pair<String, String>> secondPosition) {
		super();
		this.firstPositions = firstPosition;
		int count = 0;
		if (firstPosition != null) {
			this.setFirstPositionValues(firstPosition);
			count++;
		}
		if (secondPosition != null) {
			this.setSecPositionValues(secondPosition);
			count++;
		}
		if (count == 2)
			this.match();
	}

	/**
	 * constructs a Map with a mapping already existing
	 * @param presentMapping existing mapping between experiment compounds and KEGG pathway compounds
	 */
	public MatchingTableMap(BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> presentMapping) {
		
		for(ArrayList<Pair<String, String>> list: presentMapping.keySet()) {
			firstPositions.addAll(list);
		}
		this.map = presentMapping;
	}

	/**
	 * inserts the whole list into the map and maps them to pairs of empty Strings
	 * 
	 * @param values Compound names and IDs from experiment data
	 */
	public void setFirstPositionValues(ArrayList<Pair<String, String>> values) {
		for (Pair<String, String> pair : values) {			
			ArrayList<Pair<String, String>> newList = new ArrayList<>();
			newList.add(pair);
			map.put(newList, new Pair<>("", ""));
			if(!this.firstPositions.contains(pair)) {
				this.firstPositions.add(pair);
			}
		}
	}

	/**
	 * inserts the whole list into the map and maps them to a pairs of empty Strings
	 * 
	 * @param values Compound names and IDs from KEGG pathway
	 */
	public void setSecPositionValues(ArrayList<Pair<String, String>> values) {
		for (Pair<String, String> pair : values) {
			ArrayList<Pair<String, String>> newList = new ArrayList<>();
			Pair<String, String> newPair = new Pair<>("", "");
			newList.add(newPair);
			map.put(newList, pair);
			map.getKey(pair).clear();
		}
	}

	/**
	 * inserts the values in the first position simultaneously checks for matches
	 * with second position and puts matching pairs together if the matching second
	 * pair was not mapped to a pair before. pairs match if either the first or
	 * second Strings are equal if no match is found, the new value is mapped to a
	 * pair of empty Strings
	 * 
	 * @param values Compound names and IDs from experiment data
	 */
	public void addFirstPositionValues(ArrayList<Pair<String, String>> values) {
		for (Pair<String, String> pair : values) {
			this.addFirstPositionValue(pair);
		}
	}

	/**
	 * inserts the values in the second position simultaneously checks for matches
	 * with first position and puts matching pairs together if the matching first
	 * pair was not mapped to a pair before. pairs match if either the first or
	 * second Strings are equal if no match is found, the new value is mapped to a
	 * pair of empty Strings
	 * 
	 * @param values Compound names and IDs from KEGG pathway
	 */
	public void addSecPositionValues(ArrayList<Pair<String, String>> values) {
		for (Pair<String, String> pair : values) {
			this.addSecPositionValue(pair);
		}
	}

	/**
	 * inserts the value in the first position simultaneously checks for matches
	 * with second position and puts matching pairs together if the matching second
	 * pair was not mapped to a pair before. pairs match if either the first or
	 * second Strings are equal if no match is found, the new value is mapped to a
	 * pair of empty Strings
	 * 
	 * @param value Compound name and ID from experiment data
	 */
	private void addFirstPositionValue(Pair<String, String> value) {
		if(!firstPositions.contains(value)) {
			Set<Pair<String, String>> secondPositions = map.values();

			for (Pair<String, String> secPair : secondPositions) {
				if (this.getMatch(value, secPair) && !map.getKey(secPair).contains(value)) {
					map.getKey(secPair).add(value);
					return;
				}
			}
			ArrayList<Pair<String, String>> newList = new ArrayList<>();
			newList.add(value);
			map.put(newList , new Pair<>("", ""));
			firstPositions.add(value);
		
		}
	}

	/**
	 * inserts the values in the second position simultaneously checks for matches
	 * with first position and puts matching pairs together if the matching first
	 * pair was not mapped to a pair before. pairs match if either the first or
	 * second Strings are equal if no match is found, the new value is mapped to a
	 * pair of empty Strings
	 * 
	 * @param value Compound name and ID from KEGG pathway
	 */
	private void addSecPositionValue(Pair<String, String> value) {
		for(ArrayList<Pair<String, String>> list:map.keySet()) {
			for (Pair<String, String> firstPair : list) {
				if (this.getMatch(firstPair, value) && map.get(list).equals(new Pair<>("", ""))) {
					map.put(list, value);
					return;
				}
			}
		}
		ArrayList<Pair<String, String>> newList = new ArrayList<>();
		Pair<String, String> newPair = new Pair<>("", "");
		newList.add(newPair);
		map.put(newList, value);
		map.getKey(value).clear();
	}

	/**
	 * retrieves the ArrayList mapped to this second position pair
	 * @param scndPair ID and compound name from KEGG
	 * @return experiment compounds mapped to scndPair
	 */
	public ArrayList<Pair<String, String>> getFirstValues(Pair<String, String> scndPair) {
		return this.map.getKey(scndPair);
	}

	/**
	 * retrieves the pair mapped to this first position pair
	 * @param firstPair ID and compound name from experiment
	 * @return KEGG compounds mapped to firstPair
	 */
	public Pair<String, String> getSecondValue(Pair<String, String> firstPair) {
		for(ArrayList<Pair<String, String>> list:map.keySet()) {
			for (Pair<String, String> currfstPair : list) {
				if (currfstPair.equals(firstPair)) {
					return this.map.get(list);
				}
			}
		}
		return null;
		
	}

	/**
	 * replaces the current map with one, where all second values are mapped to a
	 * Pair of empty Strings
	 */
	private void deleteAllFirst() {
		BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> newMap = new DualHashBidiMap<>();
		Set<Pair<String, String>> set = new HashSet<>(map.values());
		for (Pair<String, String> scndPair : set) {
			ArrayList<Pair<String, String>> newList = new ArrayList<>();
			Pair<String, String> newPair = new Pair<>("", "");
			newList.add(newPair);
			newMap.put(newList, scndPair);
			newMap.getKey(scndPair).clear();
		}
		this.map = newMap;
	}

	/**
	 * replaces the current map with one, where all first values are mapped to a
	 * Pair of empty Strings
	 */
	public void deleteAllScnd() {
		BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> newMap = new DualHashBidiMap<>();
		Set<ArrayList<Pair<String, String>>> set = new HashSet<>(map.keySet());
		for (ArrayList<Pair<String, String>> firstPositions : set) {
			for(Pair<String, String> firstPair: firstPositions) {
				ArrayList<Pair<String, String>> newList = new ArrayList<>();
				newList.add(firstPair);
				newMap.put(newList, new Pair<>("", ""));
			}
		}
		this.map = newMap;
	}
	
	/**
	 * returns true if both Strings of the pair contain the value ""
	 * 
	 * @param pair pair that is tested for emptiness
	 * @return true if pair is empty
	 */
	private boolean isEmptyPair(Pair<String, String> pair) {
		String fst = pair.getFst();
		String scnd = pair.getSnd();
		if (fst == null || scnd == null)
			return false;
		return (fst.equals("") && scnd.equals(""));
	}

	/**
	 * returns true if either the first or second Strings of the two pairs are equal
	 * 
	 * @param firstPair ID and name of first compound
	 * @param secPair ID and name of second compound
	 * @return true if ID or name are the same for both pairs
	 */
	private boolean getMatch(Pair<String, String> firstPair, Pair<String, String> secPair) {
		String firstId = firstPair.getFst();
		String firstName = firstPair.getSnd();

		String[] secondPairNames = secPair.getSnd().split("; ");
		
		if (firstId != null && !firstId.isEmpty() && firstId.equals(secPair.getFst()))
			return true;
		for(String name: secondPairNames) {
			if (firstName != null && !firstName.isEmpty() && firstName.equalsIgnoreCase(name))
				return true;
		}
			return false;
	}

	/**
	 * returns true only if the two Strings of the pairs are the same
	 * @param a ID and name of first compound
	 * @param b ID and name of second compound
	 * @return true if ID and name are the same for both pairs
	 */
	private boolean getCompleteMatch(Pair<String, String> a, Pair<String, String> b){
		String firstId = a.getFst();
		String firstName = a.getSnd();

		return !(firstId == null || firstName == null)  && firstId.equals(b.getFst()) && firstName.equals(b.getSnd());
	}
	
	/**
	 * finds matches between first position and second position pairs and maps them
	 * together
	 */
	public void match() {
		this.deleteAllFirst();
		ArrayList<Pair<String, String>> secondPositionsControl = new ArrayList<>(map.values());

		for (Pair<String, String> firstPair : firstPositions) {			
			boolean matchFound = false;
			ArrayList<Pair<String, String>> secondPositions = new ArrayList<>(secondPositionsControl);
			for (Pair<String, String> secPair : secondPositions) {
				if (this.getMatch(firstPair, secPair)) {
					matchFound = true;
					map.getKey(secPair).add(firstPair);
					secondPositionsControl.remove(secPair);
				}
			}
			if (!matchFound && !this.isEmptyPair(firstPair)) {
				Pair<String, String> emptyPair = new Pair<>("", "");
				if (map.containsValue(emptyPair))
					map.getKey(emptyPair).add(firstPair);
				else {
					ArrayList<Pair<String, String>> newList = new ArrayList<>();
					newList.add(firstPair);
					map.put(newList, new Pair<>("", ""));
				}
			}
		}
		this.removeEmptyMappings();
	}

	/**
	 * removes firstPosition from its former mapping and maps it to scndPosition
	 * i.e. adds it to the resp ArrayList
	 * @param firstPosition ID and name of an experiment compound
	 * @param scndPosition ID and name of a KEGG compound
	 */
	public void map(Pair<String, String> firstPosition, Pair<String, String> scndPosition){
		
		for(ArrayList<Pair<String, String>> list: map.keySet()) {
			for(Pair<String, String> leftPair: list) {
				if(this.getCompleteMatch(leftPair, firstPosition)) {
					list.remove(leftPair);
					//create mapping to an empty String pair if scndPosition is empty
					if(this.isEmptyPair(scndPosition)) {
						ArrayList<Pair<String, String>> newList = new ArrayList<>();
						newList.add(leftPair);
						map.put(newList , new Pair<>("", ""));
						firstPositions.add(leftPair);
						this.removeEmptyMappings();
						return;
					}
					// else add to the resp ArrayList
					for(Pair<String, String> rightPair: map.values()) {
						if(this.getCompleteMatch(rightPair, scndPosition)){
							map.getKey(rightPair).add(leftPair);
							this.removeEmptyMappings();
							return;
						}
					}
				}
			}
		}
	}
		
	/**
	 * removes the given pair from the ArrayList, where it is found
	 * puts it in a new ArrayList and maps it against an empty pair
	 * @param firstPosition ID and name of an experiment compound
	 */
	public void dissolveMapping(Pair<String, String> firstPosition){
		for(ArrayList<Pair<String, String>> list: map.keySet()) {
			for(Pair<String, String> leftPair: list) {
				if(this.getCompleteMatch(leftPair, firstPosition)) {
					list.remove(leftPair);
					
					ArrayList<Pair<String, String>> newList = new ArrayList<>();
					newList.add(leftPair);
					map.put(newList , new Pair<>("", ""));
					return;
				}
			}
		}
	}
	
	/**
	 * removes an entry from map if the ArrayList has the size = 0 and the second position
	 * is Pair<String, String>("", "")
	 */
	private void removeEmptyMappings(){
		BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> newMap = 
				new DualHashBidiMap<>();
		Set<Entry<ArrayList<Pair<String, String>>, Pair<String, String>>> set = map.entrySet();
		
		for (Entry<ArrayList<Pair<String, String>>, Pair<String, String>> entry : set) {
			Pair<String, String> secPair = entry.getValue();
			ArrayList<Pair<String, String>> firstPos = entry.getKey();
			if (!(firstPos.size() < 1  && this.isEmptyPair(secPair))) {
				Pair<String, String> emptyPair = new Pair<>("", "");
				firstPos.add(emptyPair);
				newMap.put(firstPos, secPair);
				firstPos.remove(emptyPair);
			}
		}
		this.map = newMap;
	}

	/**
	 * returns this table as an array of the form String[row][columns]
	 * 
	 * @return String[row][columns]
	 */
	public String[][] toArray() {
		
		int size = 0;
		for(ArrayList<Pair<String, String>> list: map.keySet()) {
			if(list.size() == 0) {
				size += 1;
			}else {
				size += list.size();
			}
		}
		
		String[][] array = new String[size][4];
		Set<Entry<ArrayList<Pair<String, String>>, Pair<String, String>>> set = map.entrySet();

		int row = 0;
		for (Entry<ArrayList<Pair<String, String>>, Pair<String, String>> entry : set) {
			Pair<String, String> secPosition = entry.getValue();
			if(entry.getKey().size() < 1) {
				array[row][0] = "";
				array[row][1] = "";
				array[row][2] = secPosition.getFst();
				array[row++][3] = secPosition.getSnd();
			}
			else {
				for(Pair<String, String> firstPosition: entry.getKey()){	
					array[row][0] = firstPosition.getFst();
					array[row][1] = firstPosition.getSnd();
					array[row][2] = secPosition.getFst();
					array[row++][3] = secPosition.getSnd();
				}
			}
		}
		return array;
	}

	/**
	 * returns a set containing the second value pairs that map to first values
	 * where there is at least one String != ""
	 * @return Set<Pair<String, String>>
	 */
	public Set<Pair<String, String>> getSecondValuesWithMatchings() {
		Set<Pair<String, String>> pairs = new HashSet<>();

		for (Entry<ArrayList<Pair<String, String>>, Pair<String, String>> entry : this.map.entrySet()) {
			if (entry.getKey().size() > 0) {
				pairs.add(entry.getValue());
			}
		}
		return pairs;
	}

	public BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> getMap(){
		return this.map;
	}
}
