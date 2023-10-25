package org.vanted.addons.matrix.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.AttributeHelper;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.vanted.addons.matrix.graph.LabelNode;


public class Order {
	private final BidiMap<MatrixLabelButton, LabelNode> map = new DualHashBidiMap<>();
	private final ArrayList<MatrixLabelButton> priority = new ArrayList<>();
	private HashMap<String, ArrayList<MatrixLabelButton>> visibleGroupMap = new HashMap<>();
	private final HashMap<String, ArrayList<MatrixLabelButton>> deletedGroupMap = new HashMap<>();

	private String groupProperty = null;
	private String sortProperty = null;
	private String timePoint;
	private int size;
	private int visibleSize;
	
	/**creates the MatrixLabelButtons corresponding to the LabelNodes
	 * saves both in "map"
	 * also maps the whole list of MatrixLabelButtons to a null String in groupMap
	 * @param nodes: Label nodes
	 * @param vertical: Are labels displayed vertically?
	 * @param isPathway: Node refers to a pathway if true, a compound if false.
	 */
	public Order(ArrayList<LabelNode> nodes, boolean vertical, boolean isPathway) {
						
		for(LabelNode node: nodes){
			if(AttributeHelper.getLabel(node, "not found").equals("dummy")) {
				continue; //the dummy Nodes that are built for the Graph to work. TODO: come up with more elegant solution
			}

			MatrixLabelButton button = new MatrixLabelButton(node, vertical, isPathway);
			button.setContentAreaFilled(true);
			
			map.put(button, node);
		}
		buildGroupMaps();
	}

//methods
	/**
	 * builds groupMap with the MatrixLabelButtons from map.
	 * considers: visibility of each PanelNode mapped to a button,
	 * groups buttons by the category "groupProperty"
	 * sorts them by sortProperty
	 */
	private void buildGroupMaps() {
		visibleGroupMap.clear();
		deletedGroupMap.clear();
		size = 0;
		visibleSize = 0;
		//populate GroupMaps with active buttons and categories
		for (Entry<MatrixLabelButton, LabelNode> entry : map.entrySet()) {
			LabelNode node = entry.getValue();  // Actual type is PathwayTextNode
			size++;
			if (node.isVisible())
				visibleSize++;

			MatrixLabelButton button = entry.getKey();
			String category = "";
			if (!priority.contains(button))
				category = node.getStringProperty(groupProperty);

			if (!visibleGroupMap.containsKey(category))
				visibleGroupMap.put(category, new ArrayList<>());
			visibleGroupMap.get(category).add(button);
		}
	
//sort the visibleGroupMap according to the set sorting property		
		if(sortProperty != null) {
			Iterator<String> iterator = this.visibleGroupMap.keySet().iterator();
			HashMap<String, ArrayList<MatrixLabelButton>> sortedGroupMap = new HashMap<>();
			while(iterator.hasNext()) {
				String key = iterator.next();
				ArrayList<MatrixLabelButton> buttonList = this.visibleGroupMap.get(key);
				
				buttonList.sort(new ButtonSorter(sortProperty, timePoint));
				
				sortedGroupMap.put(key, buttonList);
			}
			this.visibleGroupMap = sortedGroupMap;
		}
	}

	/**
	 * sets the value for groupProperty and (re)builds the groupMap
	 * according to that grouping property
	 * @param property
	 */
	public void orderBy(String property) {
		this.groupProperty = property;
		buildGroupMaps();
	}
	
	/**
	 * sets the value for sortProperty and (re)builds the groupMap
	 * sorted by that property
	 * @param property
	 */
	public void sortBy(String property) {
		this.sortProperty = property;
		buildGroupMaps();
	}
	
	public void setTimePoint(String timePoint) {
		this.timePoint = timePoint;
	}
	
	/**
	 * first deselects all buttons in this Order
	 * then sets buttons selected according to the given property and threshold
	 * returns a ArrayList of the now selected buttons
	 * @param property
	 * @param threshold
	 * @return
	 */
	public ArrayList<MatrixLabelButton> selectBy(String property, double threshold, String greaterSmaller) {
		this.deselectAll();
		
		Iterator<Entry<MatrixLabelButton, LabelNode>>  it = map.entrySet().iterator();
		ArrayList<MatrixLabelButton> matchingButtons = new ArrayList<MatrixLabelButton>();
		
		while(it.hasNext()) {
			Entry<MatrixLabelButton, LabelNode> entry = it.next();
			MatrixLabelButton button = entry.getKey();
			//CompoundTextNode node = (CompoundTextNode) entry.getValue();
			LabelNode node = entry.getValue();

			if(greaterSmaller == "<") {
				if(node.getDoubleProperty(property, this.timePoint) != null && node.getDoubleProperty(property, this.timePoint) < threshold) {
					button.setSelected(true);
					matchingButtons.add(button);
				}
			}
			else {
				if(node.getDoubleProperty(property, this.timePoint) != null && node.getDoubleProperty(property, this.timePoint) > threshold) {
					button.setSelected(true);
					matchingButtons.add(button);
				}
			}
		}
		return matchingButtons;
	}

	/**
	 * sets selected=false to all buttons in this Order
	 */
	public ArrayList<MatrixLabelButton> deselectAll() {
		ArrayList<MatrixLabelButton> allButtons = new ArrayList<MatrixLabelButton>(map.keySet());
		
		for(MatrixLabelButton b:allButtons) {
			b.setSelected(false);
		}
		
		return allButtons;
	}
	
	/**
	 * returns the elements mapped to one category
	 * @param category
	 * @return: ArrayList<MatrixLabelButton>
	 */
	public ArrayList<MatrixLabelButton> getListFor(String category){
		return visibleGroupMap.get(category);
	}
	
	/**
	 * returns a ArrayList of the categories in this order
	 * @return: ArrayList<String>
	 */
	public ArrayList<String> getCategories() {
		ArrayList<String> list = new ArrayList<String>(visibleGroupMap.keySet());
		if(list.contains("")) {
			list.remove("");
			list.add(0 , "");
		}
		return list;
	}
	
	/**
	 * returns the overall number of visible MatrixLabelButtons (w/o those for categories) in thos Order
	 * @return size
	 */
	public int size() {
		return size;
	}

	public int getVisibleSize() {
		return visibleSize;
	}

	public void resetSize() {
		visibleSize = size;
	}

	/**returns a LinkedHashMap<MatrixLabelButton, LabelNode> in the order as they are in groupMap
	 * @return
	 */
	public LinkedHashMap <MatrixLabelButton, LabelNode> getSortedList(){
		LinkedHashMap <MatrixLabelButton, LabelNode> sortedMap = new LinkedHashMap <>(); //since a LinkedHashmap keeps its insertion order
		ArrayList<MatrixLabelButton> sortedButtons = new ArrayList<>();
		ArrayList<String> cats = getCategories();
		for (String c:cats) {
			sortedButtons.addAll(visibleGroupMap.get(c));
		}
		for (MatrixLabelButton b: sortedButtons) {
			LabelNode node = map.get(b);
			sortedMap.put(b, node);
		}
		return sortedMap;
	}

	/**
	 * set visible = false for all LabelNodes corresponding to the buttons in the list
	 * @param button:
	 */
	public boolean deleteButton(MatrixLabelButton button) {
		if(map.containsKey(button)){
				map.get(button).setVisible(false);
				return true;
			}
			return false;
		}
	
	/**
	 * checks whether this Order contains the specified button
	 * inverts the boolean "visible" for the corresponding LabelNode
	 * which determines whether it will be ignored when the matrix is build
	 * @param button: MatrixLabelButton that is hidden or shown
	 * @return true if the operation succeeds, false otherwise
	 */
	public boolean invertVisible(MatrixLabelButton button) {

		if(map.containsKey(button)){
			map.get(button).setVisible(!map.get(button).isVisible());
			return true;
		}
		return false;
	}

	public void de_prioritize(MatrixLabelButton button) {
		if(this.priority.contains(button))
			priority.remove(button);
		else
			priority.add(button);
	}
	
	/**
	 * returns Map in which all deleted buttons are mapped to the current category
	 * @return HashMap<String, ArrayList<MatrixLabelButton>>
	 */
	public HashMap<String, ArrayList<MatrixLabelButton>> getDeletedButtons(){
		return deletedGroupMap;
	}

	public MatrixLabelButton getButton(LabelNode node) {
		return map.getKey(node);
	}

	public HashSet<MatrixLabelButton> getButtons(){
		return new HashSet<>(this.map.keySet());
	}

	public HashSet<MatrixLabelButton> getVisibleButtons(){
		ArrayList<String> cats = getCategories();
		HashSet<MatrixLabelButton> visButtons = new HashSet<>();
		for (String c:cats) {
			visButtons.addAll(visibleGroupMap.get(c));
		}
		return visButtons;
	}
}
