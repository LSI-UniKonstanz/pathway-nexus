package org.vanted.addons.matrix.mapping;

import java.util.ArrayList;
import java.util.Collection;

import org.vanted.addons.matrix.reading.SubstanceWithPathways;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapNumber;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapOrg;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Url;

/**
 * a structure to save and manage pathway related data
 * @author Philipp Eberhard
 *
 */
public class DataPathway extends Pathway {
    private String superPathway;
    private String keggPathway;
    private String source;
    private double score;
    private ArrayList<SubstanceWithPathways> containedSubstances = new ArrayList<SubstanceWithPathways>();
    private ArrayList<String> relatedPathways = new ArrayList<String>();

	public DataPathway(KeggId name, MapOrg org, MapNumber number, String title, Url image, Url link,
			Collection<Entry> entries, Collection<Reaction> reactions, Collection<Relation> relations) {
		super(name, org, number, title, image, link, entries, reactions, relations);
	}
	
    
    public DataPathway(String superPathway, String title) {
        super(null, null, null, title, null, null, null, null, null);
        this.superPathway = superPathway;
    }
    
    
    public DataPathway(String title) {
        super(null, null, null, title, null, null, null, null, null);
    }
    
    
    public void setSubstances(ArrayList<SubstanceWithPathways> substances) {
    	this.containedSubstances = substances;
    }
    
    
    public void addSubstance(SubstanceWithPathways substance) {
    	if(this.containedSubstances.contains(substance)){
    		return;
    	}
		this.containedSubstances.add(substance);
    }
    
   
    public boolean containsSubstance(SubstanceWithPathways substance) {
    	return this.containedSubstances.contains(substance);
    }
    
    
    public  ArrayList<SubstanceWithPathways> getSubstances(){
    	return containedSubstances;
    }
    
    
    public  ArrayList<String> getSubstancesAsString(){
    	ArrayList<String> substancesString = new ArrayList<String>(); 
    	for(SubstanceWithPathways swp: containedSubstances) {
    		substancesString.add(swp.getName());
    	}
    	return substancesString;
    }
    
    
    public String getSuperPathway() {
    	return superPathway;
    }
    
    
    public void setSuperPathway(String newSuperPathway) {
    	this.superPathway = newSuperPathway;
    }
    
    
    public void setRelatedPathways(ArrayList<String> relatedPways) {
    	this.relatedPathways = relatedPways;
    }
    
    
    public void addRelatedPathway(String relatedPway) {
    	this.relatedPathways.add(relatedPway);
    }
    
    
    public ArrayList<String> getRelatedPathways() {
    	return this.relatedPathways;
    }
    
    
    public void setKeggPathway(String newKEGG){
    	this.keggPathway = newKEGG;
    }
    
    
    public String getKeggPathway(){
    	return this.keggPathway;
	}
    
    
    public void setSource(String newSource){
    	this.source = newSource;
    }
    
    
    public String getSource(){
    	return this.source;
    }
    
    public void setScore(double newScore){
    	this.score = newScore;
    }
    
    public double getScore() {
    	return this.score;
    }
    
    /**
     * We define two DataPathways to be equal exactly when their associated relevant information is equal.
     * This is useful e.g. when looking up these elements in a graph data structure.
     * Note that thus two *different* objects can be equal in a hashmap.
     *
     * @param obj
     * @return
     */
     public boolean equals(Object obj) {
        if (!(obj instanceof DataPathway)) {
            return false;
        }
        // compare names
        String name = this.getTitle();
        return name.equals(((DataPathway) obj).getTitle());
    }
}
