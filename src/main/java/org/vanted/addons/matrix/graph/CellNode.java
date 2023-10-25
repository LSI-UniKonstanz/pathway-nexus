package org.vanted.addons.matrix.graph;

import org.graffiti.graph.Graph;

/**
 * Represents a cell in the matrix.
 *
 * @author Benjamin Moser.
 */
public class CellNode extends EdgelessNode {
	
	private CompoundTextNode substance;
	private PathwayTextNode pathway;
	private boolean pwContainsCpd;					//is set true/false in CpdPwayGraph, right after construction
	
	 public CellNode(Graph graph) {
	        super(graph);
	 }
	
    public CellNode(Graph graph, CompoundTextNode substance, PathwayTextNode pathway) {
        super(graph);
        this.substance = substance;
        this.pathway = pathway;
    }
    
    public void setMatching(boolean match) {
    	this.pwContainsCpd = match;
    }
    
    public boolean getMatching() {
    	return this.pwContainsCpd;
    }

    public CompoundTextNode getCompoundNode() {
    	return this.substance;
    }
    
    public PathwayTextNode getPathwayNode() {
    	return this.pathway;
    }
}
