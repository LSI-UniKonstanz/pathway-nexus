package org.vanted.addons.matrix.graph;


import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.utils.EqPair;
import org.vanted.addons.matrix.utils.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a (Compound x Pathway)-Matrix.
 *
 * @author Benjamin Moser.
 */
public class CpdPwayGraph extends MatrixGraph<CompoundTextNode, PathwayTextNode, CellNode> {

//fields
    private ArrayList<String> pwCats = new ArrayList<String>(Arrays.asList("", "super Pathway"));						//only Strings contained in pwCatControl can be added to pwCats
    private ArrayList<String> cpdCats = new ArrayList<String>(Arrays.asList("", "pathway", "substance class", "cluster"));						//simultaneously to pwCats, see above
    private ArrayList<DataPathway> pathways = new ArrayList<DataPathway>();
    
//methods
    public Object copy() {
        // TODO
        return null;
    }

    public Set<CompoundTextNode> getCompoundNodes() {
        return this.contents.getYLabels();
    }

    public Set<PathwayTextNode> getPathwayNodes() {
        return this.contents.getXLabels();
    }

    @Override
    public Table<CompoundTextNode, PathwayTextNode, CellNode> extractModelFromData(ExperimentInterface experimentData) throws Exception {
        Table<CompoundTextNode, PathwayTextNode, CellNode> table = new Table<>();

        Set<PathwayTextNode> pways = extractPathways(experimentData);
         
        // obtain compounds. Because it is needed later, also with a reference to the corresponding portion of
        // the experiment data.
        Map<CompoundTextNode, SubstanceInterface> cpds = extractCompounds(experimentData);

        for (CompoundTextNode cpdNode : cpds.keySet()) {
            for (PathwayTextNode pwayNode : pways) {
                CellNode cell = new CellNode(this, cpdNode, pwayNode);
                if (matchCpdPway(cpdNode, pwayNode, cpds)) {
                	
                	
                    // add mapping data to cell
                    MappingManager.addExperimentDataToCell(cpds.get(cpdNode), cell);
                    cell.setMatching(true);
                    pwayNode.addCompoundTextNode(cpdNode);
                    DataPathway pw = pwayNode.getPathway();
                    SubstanceWithPathways subst = (SubstanceWithPathways) cpdNode.getSubstance();
                   
               // make sure that pw contains substance and vice versa
                    pw.addSubstance(subst);
                    subst.addPathway(pw);
 
                } else {
                	cell.setMatching(false);
                }

                table.upsertCell(new EqPair<>(cpdNode, pwayNode), cell);
                
            }
        }

        return table;
    }

    public Table<CompoundTextNode, PathwayTextNode, CellNode> extractModelFromDataWithExternalPathways(ExperimentInterface experimentData) throws Exception {
//    	System.out.println("start");
        Table<CompoundTextNode, PathwayTextNode, CellNode> table = new Table<>();

        Set<PathwayTextNode> pways = new HashSet<>();
        
        for(DataPathway pw: this.pathways) {
        	PathwayTextNode pwtNode = new PathwayTextNode(this, pw.getTitle());
    		pwtNode.setPathway(pw);
    		pwtNode.setExperiment(experimentData);
    		pways.add(pwtNode);
        }
                 
        // obtain compounds. Because it is needed later, also with a reference to the corresponding portion of
        // the experiment data.
        Map<CompoundTextNode, SubstanceInterface> cpds = extractCompounds(experimentData);

        for (CompoundTextNode cpdNode : cpds.keySet()) {
            for (PathwayTextNode pwayNode : pways) {
                CellNode cell = new CellNode(this, cpdNode, pwayNode);
                if (matchCpdPway(cpdNode, pwayNode, cpds)) {
                	
//System.out.println(cpdNode.getSubstance().getName() + " - " + pwayNode.getPathway().getTitle());
                	
                    // add mapping data to cell
                    MappingManager.addExperimentDataToCell(cpds.get(cpdNode), cell);
                    cell.setMatching(true);
                    pwayNode.addCompoundTextNode(cpdNode);
                    DataPathway pw = pwayNode.getPathway();
                    SubstanceWithPathways subst = (SubstanceWithPathways) cpdNode.getSubstance();
                   
               // make sure that pw contains substance and vice versa
                    pw.addSubstance(subst);
                    subst.addPathway(pw);
 
                } else {
                	cell.setMatching(false);
                }

                table.upsertCell(new EqPair<>(cpdNode, pwayNode), cell);
                
            }
        }

        return table;
    }
    
    /**
     * Determine whether a given compound appears in a given pathway
     */
    private boolean matchCpdPway(CompoundTextNode cpd, PathwayTextNode pwaynode, Map<CompoundTextNode, SubstanceInterface> cpds2subst) {

    	DataPathway pway = pwaynode.getPathway();
    	SubstanceWithPathways substance = (SubstanceWithPathways) cpds2subst.get(cpd);
    	if(pway.containsSubstance(substance) || substance.containsPathway(pway)) {
    		return true;
    	}
    	return false;
	}
	    
    /**
     * Extract pathways from incoming data.
     * @return
     * @param experimentData
     */
    private Set<PathwayTextNode> extractPathways(ExperimentInterface experimentData) throws Exception {

        Set<PathwayTextNode> pways = new HashSet<>();
        
        for (SubstanceInterface subst : experimentData) {
            if (!(subst instanceof SubstanceWithPathways)) {
                throw new Exception("experimentData must supply substances with associated pathways");
            }
            SubstanceWithPathways substPways = (SubstanceWithPathways) subst;
  
          for (Pathway pway : substPways.getPathways()) {
	            	PathwayTextNode pwtNode = new PathwayTextNode(this, pway.getTitle());
	        		pwtNode.setPathway((DataPathway) pway);
	        		pwtNode.setExperiment(experimentData);
        			pways.add(pwtNode);
            }
        }
        return pways;
    }

    private Map<CompoundTextNode, SubstanceInterface> extractCompounds(ExperimentInterface experimentData) {
        Map<CompoundTextNode, SubstanceInterface> cpds = new HashMap<>();
        for (SubstanceInterface subst : experimentData) {
            CompoundTextNode cpdNode = new CompoundTextNode(this, subst.getName());
            
            cpdNode.setExperiment(experimentData);
            cpdNode.setSubstance(subst);
         
            cpds.put(cpdNode, subst); 
        }
        return cpds;
    }

    /**
     * Overwrites cells if value differs.
     * @param compoundTextNodePathwayTextNodePair
     * @param c11
     */
    public void upsertCell(EqPair<CompoundTextNode, PathwayTextNode> compoundTextNodePathwayTextNodePair, CellNode c11) {
        this.contents.upsertCell(compoundTextNodePathwayTextNodePair, c11);
    }
    
    @Override
    public CellNode create() {
        return new CellNode(this);
    }

    /**
     * checks whether the pathways with the given Strings as title are already present in the matrix
     * if one is not present, that pathway is created and added to the matrix
     * the pathways that were already present plus the created pathways are returned in an ArrayList
     * @param pathwaysToCheck
     * @return
     */
    public ArrayList<DataPathway> checkAddReturnNewPws(ArrayList<String> pathwaysToCheck){
    	//CpdPwayGraph cpdPwayGraph = (CpdPwayGraph) this.graph;
    	ArrayList<DataPathway> presentPws = this.getPathways();
    	ArrayList<DataPathway> newPathways = new ArrayList<DataPathway>();
    	
    	HashMap<String, DataPathway> presentPwsString= new HashMap<String, DataPathway>();
    	for(DataPathway pathway: presentPws) {
    		presentPwsString.put(pathway.getTitle(), pathway);
    	}

    	for(String pathwayToCheck: pathwaysToCheck) {
    		if(presentPwsString.keySet().contains(pathwayToCheck)) {
    			DataPathway pw = presentPwsString.get(pathwayToCheck);
    			newPathways.add(pw);
    		}
    		else {
    			DataPathway newPathway = new DataPathway(pathwayToCheck);
    			System.out.println("new PW: " + pathwayToCheck);
    			//cpdPwayGraph.addPathway(newPathway);
    			newPathway.setSource("KEGG");
    			newPathways.add(newPathway);
    		}
    	}
    	return newPathways;
    }


    /**
     * returns an ArrayList<String> containing the categories for the specified elements (pathways or compounds)
     * @param forPathways
     * @return
     */
    public ArrayList<String> getCategories(boolean forPathways){
    	if(forPathways) {
    		return pwCats;
    	}
    	return cpdCats;
    			
    }

    /**
     * adds a pathway to the list of pathways in this graph
     * @param pathway
     */
    public void addPathway(DataPathway pathway) {
    	if(!this.pathways.contains(pathway)) {
    		this.pathways.add(pathway);
    	}
    }
    
    public void addPathways(ArrayList<DataPathway> pathways) {
    	for(DataPathway pw: pathways) {
    		this.addPathway(pw);
    	}
    }
    
    public void removePathway(DataPathway pathway){	
    	this.pathways.remove(pathway);
    }
    
    public ArrayList<DataPathway> getPathways(){
    	return this.pathways;
    }
    
    public ArrayList<String> getPathwaysAsStrings(){
    	ArrayList<String> pwsAsStrings = new ArrayList<String>();
    	for(DataPathway pw: this.pathways) {
    		pwsAsStrings.add(pw.getTitle());
    	}
    	
    	return pwsAsStrings;
    }
    
    /**
     * TODO: only for testing
     */
     public void printPathways() {
    	int i = 1;
    	for(DataPathway p:pathways) {
    		ArrayList<SubstanceWithPathways> containedSubstances =  p.getSubstances();
    		System.out.println(i++ + p.getTitle() + ":");
    		
    		for(SubstanceInterface s:containedSubstances) {
    			System.out.print(s.getName() + ", ");
    			
    		}
    		System.out.println();
    	}
    }

     /**
     * the graph has to be instantiated with some content
     * if there is no content available at that time it is instantiated with some dummy content
     * which can later cause problems (e.g. a PathwayTextNode w/o Pathway)
     */
    public void deleteDummies() {
     	
     	Iterator<PathwayTextNode> pwIterator = this.contents.getXLabels().iterator();
     	while(pwIterator.hasNext()) {
     		PathwayTextNode pw = (PathwayTextNode) pwIterator.next();
     		if(pw.getPathway() == null) {
     			pwIterator.remove();
     		}
     	}
     	
     	Iterator<CompoundTextNode> cpdIterator = this.contents.getYLabels().iterator();
     	while(cpdIterator.hasNext()) {
     		CompoundTextNode cpd = (CompoundTextNode) cpdIterator.next();
     		if(cpd.getSubstance() == null) {
     			cpdIterator.remove();
     		}
     	}
     }
     
}
