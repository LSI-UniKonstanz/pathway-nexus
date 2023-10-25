package org.vanted.addons.matrix.reading;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.vanted.addons.matrix.mapping.DataPathway;

/**
 * @author Benjamin Moser.
 */
public class SubstanceWithPathways extends Substance {
	private HashMap<String, Integer> dbIds = new HashMap<>();
	private ArrayList<DataPathway> associatedPathways = new ArrayList<DataPathway>();
    private HashSet<String> alternativeNames = new HashSet<String>();
    
    /**
     * We define two SubstanceWithPathways to be equal exactly when their associated relevant information is equal
     * which is either the names or at least one of their IDs.
     * This is useful e.g. when looking up these elements in a graph data structure.
     * Note that thus two *different* objects can be equal in a hashmap.
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof SubstanceWithPathways)) {
            return false;
        }
        
        if(this.getName().equals(((SubstanceWithPathways) obj).getName())) { 
        	return true;
        }
        
        Set<Entry<Integer, String>> thisSynSet = null;
        Set<Entry<Integer, String>> objSynSet = null;

        try{
        	thisSynSet = this.getSynonymMap().entrySet();
        	objSynSet = ((SubstanceWithPathways)obj).getSynonymMap().entrySet();

        }
    	catch(java.lang.NullPointerException e) {
    		return false;
    	}
    	
        for(Entry<Integer, String> entry: objSynSet) {
        	if(thisSynSet != null &&	    		   
        			!entry.getValue().equals("") &&
        				entry.getValue() != (null) &&
        					thisSynSet.contains(entry)) {
	        		return true;
	    	   }
	    	}
        
        return false;
    }

	public void setDbId(String dbName, String dbId) {
		if (dbIds.containsKey(dbName))
			setSynonyme(dbIds.get(dbName), dbId);
		else {
			int index = dbIds.size();
			dbIds.put(dbName, index);
			setSynonyme(index, dbId);
		}
	}

	public String getDbId(String dbName) {
		if (dbIds.containsKey(dbName))
			return getSynonyme(dbIds.get(dbName));
		else
			return "";
	}
    
    public void setPathways(ArrayList<DataPathway> pathways) {
    	this.associatedPathways = pathways;
    }
    
    public void addPathway(Pathway pathway) {
    	if(!this.associatedPathways.contains(pathway)) {
    		this.associatedPathways.add((DataPathway) pathway);
    	}
    }
    
    public void addPathways(ArrayList<DataPathway> newPathways) {
    	for(DataPathway newPathway: newPathways) {
    		this.addPathway(newPathway);
    	}
	}
    
    public void removePathway(DataPathway pathway){
    	this.associatedPathways.remove(pathway);
    }
    
    public ArrayList<DataPathway> getPathways(){
    	return this.associatedPathways;
    }
    
    public ArrayList<String> getPathwaysAsStrings(){
    	ArrayList<String> pathwayList = new ArrayList<String>();
    	for(Pathway p: this.getPathways()) {
    		pathwayList.add(p.getTitle());
    	}
    	return pathwayList;
    }
    
    public boolean containsPathway(DataPathway pathway){
    	return this.associatedPathways.contains(pathway);
    }
    
    public void setAlternativeNames(HashSet<String> alternatives) {
    	this.alternativeNames = alternatives;
    }
    
    public void addAlternativeName(String alternative) {
    	if(this.alternativeNames.contains(alternative)) {
    		return;
    	}
    	else {
        	this.alternativeNames.add(alternative);
    	}
   	}

    public HashSet<String> getAlternativeNames() {
    	return this.alternativeNames;
    }
    
}
