package org.vanted.addons.matrix.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class MatrixButton extends JButton {
	
//Fields
	GridBagConstraints constraints;
	
//constructor
	/**
	 * inititalizes button with default GridBagConstraints
	 */
	public MatrixButton() {
		super();
		for (int i = 0; i < super.getAncestorListeners().length; i++) {
			super.removeAncestorListener(super.getAncestorListeners()[i]);
		}
		constraints =  new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		
		setBorder(BorderFactory.createLineBorder(Color.white));
		setOpaque(true);
	}
	
//methods
	/**
	 *sets position in a GridBagLayout
	 */
	public void setPosition(int x, int y) {
		constraints.gridx = x;
		constraints.gridy = y;
	}

	/**
	 * returns the GridBagConstraints of this button
	 * @return
	 */
	public GridBagConstraints getConstraints() {
		return constraints;
	}
	
	/**
	 *sets the amount of rows an columns, this button contains
	 */
	public void setSizeConstraints(int width, int height) {
		constraints.gridwidth = width;
		constraints.gridheight = height;
	}

	/**
	 *sets the weight constraints for this button
	 */
	public void setWeightConstraints(double x, double y) {
		constraints.weightx = x;
		constraints.weighty = y;
	}

	public void setFillConstraints(int fill) {
		constraints.fill = fill;
	}
	
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if(selected == false) {
			this.setBorder(BorderFactory.createLineBorder(Color.white));
		}
		else {
			this.setBorder(BorderFactory.createLineBorder(Color.black));
		}
	}
}
