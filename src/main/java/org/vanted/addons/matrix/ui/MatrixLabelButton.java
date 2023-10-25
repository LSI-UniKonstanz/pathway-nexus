package org.vanted.addons.matrix.ui;

import java.awt.Font;
import java.awt.Insets;
import javax.swing.SpringLayout;
import java.util.ArrayList;

import org.AttributeHelper;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.graph.LabelNode;
import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;


public class MatrixLabelButton extends MatrixButton{
//fields
	private MatrixLabel label;
	private boolean isVertical;
	private boolean isPathway;
	private LabelNode labelNode;
	private ArrayList<MatrixCellButton> cells = new ArrayList<MatrixCellButton>();

//constructor
	/**
	 * only for category Buttons
	 * TODO: decide whether there should be an extra class for category Buttons
	 * @param catLabel
	 * @param vertical
	 */
	public MatrixLabelButton(MatrixLabel catLabel, boolean vertical) {
		super();
		isVertical = vertical;
		label = catLabel;
		
		if(isVertical) {
			verticalLabelLayout();
		}
		this.add(label);
	}

	public MatrixLabelButton(LabelNode node, boolean vertical, boolean isPathway) {
		super();

		this.labelNode = node;
		this.isVertical = vertical;
		this.isPathway = isPathway;
		label = new MatrixLabel(AttributeHelper.getLabel(node, "not found"));
		
		if(this.isPathway) {
			PathwayTextNode ptNode = (PathwayTextNode) this.labelNode;
			String source = ptNode.getPathway().getSource();
			if(source != null && source.equals("KEGG")){
				Font font = new Font("Tahoma", Font.BOLD, 10);
				
				this.label.setFont(font);
			}
		}
		
		if(isVertical) {
			verticalLabelLayout();
		}
		this.add(label);
	}

	public MatrixLabelButton(LabelNode node, int x, int y, boolean vertical, boolean isPathway) {
		this(node, vertical, isPathway);
		
		constraints.gridx = x;
		constraints.gridy = y;
		this.add(label);
	}

//methods
	
	/**
	 * adds a MatrixLabel to this Button
	 * @param name
	 */
	public void setLabel(MatrixLabel name) {
		label = name;
		this.add(label);
	}

	public MatrixLabel getContent() {
		return this.label;
	}
	
	/**
	 *returns the text that this button displays as String
	 */
	public String toString() {
		return label.getText();
	}

	/**
	 * sets whether this button shall be displayed vertical
	 * @param vertical
	 */
	public void setVertical(boolean vertical) {
		this.isVertical = vertical;
		
		if(vertical) {
			this.setMargin(new Insets(5,1,5,1));
			this.label.setRotation(-1);
		}
		else {
			this.label.setRotation(0);
			this.setMargin(new Insets(1,5,1,5));

		}
		
	}
	
	/**
	 * returns whether this button will be displayed vertical
	 * @return boolean isVertical
	 */
	public boolean isVertical() {
		return this.isVertical;
	}

	/**
	 * determines whether this button contains a pathway
	 * @param pathway
	 */
	public void setIsPathway(boolean pathway) {
		this.isPathway = pathway;
	}
	
	/**
	 * returns true if this buttons displays a pathway
	 * @return
	 */
	public boolean isPathway() {
		return this.isPathway;
	}

	/**
	 * associated a cellButton with this LabelButton
	 * i.e. adds it to a List held by this Button
	 * @param cell
	 */
	public void addCell(MatrixCellButton cell) {
		cells.add(cell);
	}
	
	/**
	 * selects/deselects this LabelButton
	 * also calls the respective methods for all associated cellButtons 
	 */
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		for(MatrixCellButton cell: cells) {
			cell.de_Select();
		}
	}

	/**
	 * returns the sample average for the given time Point
	 * @param timePoint
	 * @return
	 */
	public Double meanValue(String timePoint) {
		return ((CompoundTextNode) this.labelNode).getSampleMeanFor(timePoint);
	}
	
	/**
	 * returns the standard deviation for the sample average for the given time point
	 * @param timePoint
	 * @return
	 */
	public Double stdDev(String timePoint) {
		return ((CompoundTextNode) this.labelNode).getStdDevFor(timePoint);
	}
	
	public void test() {
		
	}
		
	public LabelNode getLabelNode() {
		return this.labelNode;
	}
	
//for use in HashMaps	
	/**
	 *two MatrixLabelButtons are equal if they display the same text
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof MatrixLabelButton)) {
	        return false;
	    }
	    // compare label
	    String label = this.toString();
	    return label.equals(((MatrixLabelButton) obj).toString());
	}
	
	/**
	 * hashcode is created from the label (String)
	 * note that two buttons without labels are also equal
     * @return
     * @see #equals(Object)
     */
    public int hashCode() {
        String label = this.toString();
        return label.hashCode();
    }

	/**
	 * Set layout for vertical labels so that they are aligned bottom center.
	 */
	private void verticalLabelLayout() {
		SpringLayout sl = new SpringLayout();
		sl.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH, this);
		sl.putConstraint(SpringLayout.HORIZONTAL_CENTER, label, 0, SpringLayout.HORIZONTAL_CENTER, this);
		setLayout(sl);
		label.setRotation(-1);
	}

	public void hideCells() {
		this.setVisible(false);
		this.setSelected(false);
		for (MatrixCellButton cell : cells) {
			cell.setVisible(false);
			cell.setSelected(false);
		}
	}

	/**
	 * Make all MatrixCellButtons belonging to this MatrixLabelButton visible unless it is also hidden
	 */
	public void showCells() {
		this.setVisible(true);
		if (isPathway) {
			for (MatrixCellButton cell : cells) {
				if (cell.getCompoundButton().isVisible())
					cell.setVisible(true);
			}
		}
		else {
			for (MatrixCellButton cell : cells) {
				if (cell.getPwayButton().isVisible())
					cell.setVisible(true);
			}
		}
	}
}
