package org.vanted.addons.matrix.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.graffiti.graph.Graph;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.ui.MatrixLabelButton;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

/**
 * Represents a label describing a compound.
 *
 * @author Benjamin Moser.
 */
public class CompoundTextNode extends LabelNode {
//fields for SwingView
	SubstanceInterface substance;
	ConditionInterface condition;
	LinkedHashMap<String, SampleInterface> timeSampleMap; 
	
//constructors	
    public CompoundTextNode(Graph graph) {
        super(graph);
    }

    public CompoundTextNode(MatrixGraph g, String labelText) {
        super(g, labelText);
    }
      
//methods for Swing View    
    /**
     * assigns a Substance to this Node
     * extracts condition and maps time points to Samples
     * @param subst
     */
    public void setSubstance(SubstanceInterface subst) {
    	this.substance = subst;
    	this.setCondition(subst);	
  }
    
    /**
     * extracts the ConditionInterface from subst
     * @param subst
     */
    private void setCondition(SubstanceInterface subst) {
    	ConditionInterface cond = (ConditionInterface) subst.getConditions(null).toArray()[0];
    	this.condition = cond;
    	
    	this.setSamples(cond);
    }
    
    /**
     * extracts the List of SampleInterface from condition
     * and maps them to their timepoints
     * @param condition
     */
    private void setSamples(ConditionInterface condition) {
    	timeSampleMap = new LinkedHashMap<String, SampleInterface>();
    	
    	Iterator<SampleInterface> samps = condition.iterator();
    	while(samps.hasNext()) {
    		Sample s = (Sample) samps.next();
    		
    		String timePoint = s.getSampleTime();
    		timeSampleMap.put(timePoint, s);
    	}
    }
    
    /**
     * returns the SubstanceInterface assigned to this Node
     * @return
     */
    public SubstanceInterface getSubstance() {
    	return substance;
    }
    
    public ConditionInterface getCondition() {
    	return this.condition;
    }
    
    /**
     * returns the mean value of the measurements of the specified time
     * @param timePoint
     * @return
     */
    public Double getSampleMeanFor(String timePoint) {
    	
    	Double d = 0.0;
    	try {
    		d = timeSampleMap.get(timePoint).getSampleAverage().getValue();
    	}catch(java.lang.NullPointerException e){
    		return d;
    	}
    	
    	return d;
    
    }
    
    /**
     * returns the standard deviation 
     * of the SampleAverage for the specified time point
     * @param timePoint
     * @return
     */
    public Double getStdDevFor(String timePoint) {
    	Double d = 0.0;
    	try {
    		d = timeSampleMap.get(timePoint).getSampleAverage().getStdDev();
    	}catch(java.lang.NullPointerException e){
    		return d;
    	}
    	
    	return d;
    }
 
    public Double getDoubleProperty(String property, String timePoint) {
		switch (property) {
			case "standard deviation":
				return this.getStdDevFor(timePoint);
			case "fold change":
				return this.getSampleMeanFor(timePoint);
			default:
				return 0.0;
		}
    }
    
    public String getStringProperty(String category) {
    	if(category == null)
    		return null;
    	
		SubstanceWithPathways substance = (SubstanceWithPathways) this.getSubstance();
		switch (category) {
			case "pathway":
				return substance.getPathways().get(0).getTitle();
			case "substance class":
				return substance.getSubstancegroup();
			case "cluster":
				return substance.getClusterId();
			default:
				return null;
		}
	}

    /**
     * returns the desired ID from the substance associated to this node
     * @param db
     * @return
     */
    public String getID(String db) {
    	HashMap<Integer, String> map =  this.substance.getSynonymMap();
    	
    	switch (db) {
    		case "CAS":
    			return map.get(0);
    		case "pubchem":
    			return map.get(1);
    		case "chemspider":
    			return map.get(2);
    		case "kegg":
    			return map.get(3);
    		case "hmdb":
    			return map.get(4);
    		default:
    			return null;
    	}
    }
}
