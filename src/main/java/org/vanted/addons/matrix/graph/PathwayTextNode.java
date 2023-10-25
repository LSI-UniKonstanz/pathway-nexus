package org.vanted.addons.matrix.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.AttributeHelper;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Graph;
import org.graffiti.util.Pair;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;

/**
 * Represents a label describing a Pathway,
 *
 * @author Benjamin Moser/Philipp Eberhard
 */
public class PathwayTextNode extends LabelNode {
	private DataPathway pathway;
	private ArrayList<CompoundTextNode> compoundNodes = new ArrayList<CompoundTextNode>();
	
	// for mapping of matrix compounds to network compounds of the resp data base representation of this pathway    
	private BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> mapping;	
		
//constructors	
    public PathwayTextNode(Graph graph) {
        super(graph);
    }

    public PathwayTextNode(MatrixGraph g, String labelText) {
        super(g, labelText);
    }

//methods
    /**
     * assigns a Pathway to this Node
     * extracts subPW and superPw and adds them as Attributes to this Node
     * @param pw
     */
    public void setPathway(DataPathway pw) {
    	this.pathway = pw;
    }
    
    /**
     * returns the Pathway assigned to this Node
     * @return
     */
    public DataPathway getPathway() {
    	return pathway;
    }

    public String getStringProperty(String category) {
    	if(category == null)
    		return null;
    	
    	else if(category.equals("super Pathway")) {
    		return this.pathway.getSuperPathway();
    	}
    	
    	return null;
	}
    
    public Double getDoubleProperty(String property, String timePoint) {
    	switch(property) {
    	case "contained compounds":
    		return (double) this.compoundNodes.size();
    	
    	case "score":
    		return this.pathway.getScore();
    		
    	default:
    		return 0.0;
    	}
    }
    
    /**
     * adds a CompoundTextNode to this Node's list
     * should be invoked simultaneously when a substance is added to the respective pathway
     * @param cpdNode
     */
    public void addCompoundTextNode(CompoundTextNode cpdNode){
    	if(!compoundNodes.contains(cpdNode)) {
    		this.compoundNodes.add(cpdNode);
    	}
    	
    	SubstanceWithPathways swp = (SubstanceWithPathways) cpdNode.getSubstance();
    	String keggID = swp.getDbId("KEGG");
    	if(keggID == null) {
			keggID = "";
    	}
    	String name = swp.getName();
    	if(name == null) {
    		name = "";
    	}
    	
    	

    	Pair<String, String> matrixSubstance = new Pair<String, String>(keggID, name);
    	this.addMapping(matrixSubstance, new Pair<String, String>("", ""));
	}
    
    /**
     * returns the list of CompoundTextNodes, that are mapped to this PathwayTextNode
     * @return
     */
    public ArrayList<CompoundTextNode> getCompoundTextNodes(){
    	return this.compoundNodes;
    }

    public void addMapping(Pair<String, String> matrixSubstance, Pair<String, String> dbSubstance){
    	
    	if(this.mapping == null) {
    		this.mapping = new DualHashBidiMap<ArrayList<Pair<String, String>>, Pair<String, String>>();
    	}
    	
    	if(this.mapping.containsValue(dbSubstance) && matrixSubstance != null && !isEmptyPair(matrixSubstance)) {
    		ArrayList<Pair<String, String>> currentlyMapped = mapping.getKey(dbSubstance);
    		currentlyMapped.add(matrixSubstance);
    	}
    	else {
    		ArrayList<Pair<String, String>> newList = new ArrayList<Pair<String, String>>();
    		newList.add(matrixSubstance);
    		this.mapping.put(newList, dbSubstance);
    	}   	
    }
    
    private boolean isEmptyPair(Pair<String, String> pair){
    	if(pair.getFst().equals("") && pair.getSnd().equals("")) {
    		return true;
    	}
    	return false;
    }
    
    public void setMapping(BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> newMapping){
    	this.mapping = newMapping;
    }
    
    public BidiMap<ArrayList<Pair<String, String>>, Pair<String, String>> getMapping(){
    	return this.mapping;
    }
   
    public boolean hasMapping(){
    	if(this.mapping != null && mapping.size() > 0) {
    		return true;
    	}
    	return false;
    }
}

