package org.vanted.addons.matrix.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.graffiti.util.Pair;

public class MatrixCellButton extends MatrixButton {
	private JLabel label = new JLabel();
	private MatrixLabelButton compound;
	private MatrixLabelButton pathway;
	
//constructor
	public MatrixCellButton() {
		super();
		this.add(label);
	}
	
	public MatrixCellButton(MatrixLabelButton pw, MatrixLabelButton cpd) {
		super();
		this.compound = cpd;
		this.pathway = pw;
		cpd.addCell(this);
		pw.addCell(this);
		this.add(label);
	}
	
//methods
	/**
	 * since MatrixCellButtons are always squares they only need one size parameter
	 * @param size
	 */
	public void setPreferredSize(int size) {
		Dimension dim = new Dimension(size, size);
		super.setPreferredSize(dim);
	}
	
	/**
	 * sets color of this button
	 * @param color
	 */
	public void setColor(Color color) {
		this.setBackground(color);

	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public void de_Select() {
		if(compound.isSelected() || pathway.isSelected()) {
			this.setSelected(true);
		}
		else {
			this.setSelected(false);
		}
	}

	public void setContent(String text){
		this.label.setText(text);

		this.label.revalidate();

	}
	
	public MatrixLabelButton getCompoundButton() {
		return this.compound;
	}
	
	public MatrixLabelButton getPwayButton() {
		return this.pathway;
	}
}
