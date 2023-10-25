package org.vanted.addons.matrix.graph;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * Represents a label in the matrix graph.
 *
 * @author Benjamin Moser.
 */
public abstract class LabelNode extends EdgelessNode {
//fields for SwingView
	ExperimentInterface experiment;
	private boolean visible = true;
	
/////////	
	LabelNode(Graph graph) {
        super(graph);
    }

    LabelNode(Graph g, String labelText) {
        super(g);
        this.setLabelText(labelText);
    }

    protected void setLabelText(String labelText) {
        AttributeHelper.setLabel(this, labelText);
        setString("name", labelText);
    }

    /**
     * We define two nodes to be equal exactly when their associated relevant information is equal.
     * This is useful e.g. when looking up these elements in a graph data structure.
     * Note that thus two *different* objects can be equal in a hashmap.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LabelNode)) {
            return false;
        }
        // compare label
        String label = AttributeHelper.getLabel(this, "n/a");
        return label.equals(AttributeHelper.getLabel((LabelNode) obj, "n/a"));
    }

    /**
     * @return
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        String label = AttributeHelper.getLabel(this, "n/a");
        return label.hashCode();
        // TODO: two nodes without labels would be equal in this respect. OK?
    }

    /**
     * returns the attribute to the given path as its String representation
     * @param category
     * @return
     */
    public abstract String getStringProperty(String category);
    
    public abstract Double getDoubleProperty(String category, String timePoint);
    
    /**
     * assigns an instance of ExperimentInterface to this node
     * @param ei
     */
    public void setExperiment(ExperimentInterface ei) {
    	this.experiment = ei;
    }

    /**
     * sets value of boolean visible
     * this is used as info whether this element shall be in the matrix
     * @param vis
     */
    public void setVisible(boolean vis) {
    	this.visible = vis;
    }
    
    /**
     * returns current value of boolean visible
     * this is used as info whether this element shall be in the matrix
     * @param vis
     */
    public boolean isVisible() {
    	return this.visible;
    }
}    
    
