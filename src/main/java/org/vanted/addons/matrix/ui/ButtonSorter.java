package org.vanted.addons.matrix.ui;

import java.util.Comparator;

import org.vanted.addons.matrix.graph.PathwayTextNode;

public class ButtonSorter implements Comparator<MatrixLabelButton>{

	private String property;
	private String timePoint;
	
	public ButtonSorter(String property, String timePoint) {
		this.property = property;
		this.timePoint = timePoint;
	}
	
	@Override
	public int compare(MatrixLabelButton o1, MatrixLabelButton o2) {
		switch(property) {
		case "name":
			return o1.toString().compareTo(o2.toString());
		
		case "fold change":
			return o1.meanValue(timePoint).compareTo(o2.meanValue(timePoint));
		
		case "standard deviation":
			return o1.stdDev(timePoint).compareTo(o2.stdDev(timePoint));
		
		case "contained compounds":
			PathwayTextNode node1 = (PathwayTextNode) o1.getLabelNode();
			PathwayTextNode node2 = (PathwayTextNode) o2.getLabelNode();
			
			Integer size1 = node1.getPathway().getSubstances().size();
			Integer size2 = node2.getPathway().getSubstances().size();

			return (size2.compareTo(size1));
			
		case "score":
			PathwayTextNode scoreNode1 = (PathwayTextNode) o1.getLabelNode();
			PathwayTextNode scoreNode2 = (PathwayTextNode) o2.getLabelNode();
			
			Double score1 = scoreNode1.getPathway().getScore();
			Double score2 = scoreNode2.getPathway().getScore();
			
			return(score2.compareTo(score1));
		default:
			return 0;
		}
	}
}
