package org.vanted.addons.matrix.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.apache.batik.ext.swing.GridBagConstants;
import org.graffiti.editor.MainFrame;
import org.vanted.addons.matrix.graph.CellNode;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.graph.LabelNode;
import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.utils.GridBagUtils;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * a UI in which the user can specify on when a compound is considered regulated
 * and apply algorithms to give the pathways a simple rating based on these parameters
 * @author Philipp Eberhard
 *
 */
public class PathwayRatingUI extends JFrame{
	JComboBox<String> chooseAlgorithm;
	MatrixGraphPanel matrixGraphPanel;
	JPanel algorithmSettings = new JPanel();
	JPanel initPanel = new JPanel();
	
//constructor
	/**
	 * builds the UI components
	 * @param mgp
	 */
	public PathwayRatingUI(MatrixGraphPanel mgp) {
		JRootPane root = this.getRootPane();
		//root.setLayout(new BoxLayout(root, BoxLayout.PAGE_AXIS));
		root.setLayout(new GridBagLayout());
		initPanel.setLayout(new GridBagLayout());

		this.matrixGraphPanel = mgp;

		JLabel chooseAlgLabel = new JLabel("choose scoring algorithm: ");
		initPanel.add(chooseAlgLabel, GridBagUtils.setPosition(0, 0));
		
	    chooseAlgorithm = new JComboBox<String>( new String[]{" ", 
	    		"reg/unreg ratio", "amt. of regulated substances", "percentage of regulated substances", "MSEA"});
	    	
    	chooseAlgorithm.addActionListener(l -> {
    		JComboBox<String> cb = (JComboBox<String>) l.getSource();
            String selectedItem = (String) cb.getSelectedItem();
            switch(selectedItem) {
	            case "reg/unreg ratio":
	            	algorithmSettings.removeAll();
	            	algorithmSettings.setLayout(new BorderLayout());
	            	algorithmSettings.add(this.reg_unregAlgorithmPanel());
	            	this.rootPane.updateUI();
	            	this.pack();
	            	break;
	            case "amt. of regulated substances":
	            	algorithmSettings.removeAll();
	            	algorithmSettings.setLayout(new BorderLayout());
	            	algorithmSettings.add(this.amtRegSubstAlgorithmPanel());
	            	this.rootPane.updateUI();
	            	this.pack();
	            	break;
	            case "percentage of regulated substances":
	            	algorithmSettings.removeAll();
	            	algorithmSettings.setLayout(new BorderLayout());
	            	algorithmSettings.add(this.percentRegSubstAlgorithmPanel());
	            	this.rootPane.updateUI();
	            	this.pack();
	            	break;
	            case "MSEA":
	            	algorithmSettings.removeAll();
	            	algorithmSettings.setLayout(new BorderLayout());
	            	algorithmSettings.add(this.mSEAAlgorithmPanel());
	            	this.rootPane.updateUI();
	            	this.pack();
	            	break;
            }
    	});
    	initPanel.add(chooseAlgorithm, GridBagUtils.setPosition(0, 1)); 
    	
    	root.add(initPanel, GridBagUtils.setPosition(0, 0));
    	root.add(algorithmSettings, GridBagUtils.setPosition(0, 1));
	}
	
	/**
	 * panel for the deregulated/not deregulated algorithm
	 * @return
	 */
	private JPanel reg_unregAlgorithmPanel(){
		JPanel reg_unregAlgorithmPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints labelgbc = GridBagUtils.setPosition(0, 0);
		labelgbc.fill = GridBagConstraints.HORIZONTAL;
		labelgbc.weightx = 1;
		
		GridBagConstraints fieldgbc = GridBagUtils.setPosition(0 ,1);
		fieldgbc.fill = GridBagConstraints.HORIZONTAL;
		fieldgbc.weightx = 1;
		
		JFormattedTextField downRegulation = new JFormattedTextField(DecimalFormat.getInstance());
		downRegulation.setColumns(20);
	    JLabel downRegulationLabel = new JLabel("unregulated between:");
	    reg_unregAlgorithmPanel.add(downRegulationLabel, labelgbc);
	    reg_unregAlgorithmPanel.add(downRegulation, fieldgbc);
	
	    fieldgbc.gridx = labelgbc.gridx = 1;
	    
	    JFormattedTextField upRegulation = new JFormattedTextField(DecimalFormat.getInstance());
	    upRegulation.setColumns(20);
	    JLabel upRegulationLabel = new JLabel("and: ");
	    reg_unregAlgorithmPanel.add(upRegulationLabel, labelgbc);
	    reg_unregAlgorithmPanel.add(upRegulation,  fieldgbc);
	    
	    JButton calc = new JButton("do rating");
	    calc.addActionListener(l -> {
	    	//this.getRating(this.matrixGraphPanel);
	    	
	    	double lowerThreshold = 0.0;
			double upperThreshold = 0.0;
			try{
				lowerThreshold = ((Number) downRegulation.getValue()).doubleValue();
				upperThreshold = ((Number) upRegulation.getValue()).doubleValue();
			}catch(java.lang.NullPointerException e) {
				MainFrame.showMessageDialog( "please enter lower and  upper threshold!", "value missing!");

			}
	    	
	    	HashSet<MatrixLabelButton> pwButtonSet = this.matrixGraphPanel.getButtons(true);
			
			for(MatrixLabelButton pw: pwButtonSet) {
				//this.assignRating(pw);
				
				double score = this.reg_unregAlgorithm(pw, lowerThreshold, upperThreshold);
				
				DataPathway dpw = ((PathwayTextNode) pw.getLabelNode()).getPathway();
				dpw.setScore(score);
				
				//System.out.println(dpw.getTitle() + ": " + score);
			}
	    });
	    reg_unregAlgorithmPanel.add(calc,  GridBagUtils.setPosition(0,3, GridBagConstraints.REMAINDER, 1));
	    
	    return reg_unregAlgorithmPanel;
	}
	
	private double reg_unregAlgorithm(MatrixLabelButton pw, double lowerThreshold, double upperThreshold){

		String timePoint = matrixGraphPanel.getTimePoint();
		int regulated = 0;
		int notRegulated = 0;
		
		LabelNode node = pw.getLabelNode();
		
		if(node instanceof PathwayTextNode) {
			PathwayTextNode ptn = (PathwayTextNode) node;
			Set<CellNode> cellSet = this.matrixGraphPanel.getGraph().getContent().getCol(ptn);
	
			for(CellNode cell: cellSet) {	
				if(cell.getMatching()) {
					CompoundTextNode cpdNode = cell.getCompoundNode();
					double mean = cpdNode.getSampleMeanFor(timePoint);
					if(mean < lowerThreshold || mean > upperThreshold) {
						regulated++;
					}else if(mean >= lowerThreshold && mean <= upperThreshold){
						notRegulated++;
					}
				}
			}
		}
		
		double score = regulated;
		if(notRegulated > 0) {
			score = ((double) regulated)/((double) notRegulated);
		}
		return score;
	}
	
	/**
	 * panel for the amount of deregulated substances algorithm
	 * @return
	 */
	private JPanel amtRegSubstAlgorithmPanel(){
		JPanel amtRegSubstAlgorithm = new JPanel(new GridBagLayout());
		
		GridBagConstraints labelgbc = GridBagUtils.setPosition(0, 0);
		labelgbc.fill = GridBagConstraints.HORIZONTAL;
		labelgbc.weightx = 1;
		
		GridBagConstraints fieldgbc = GridBagUtils.setPosition(0 ,1);
		fieldgbc.fill = GridBagConstraints.HORIZONTAL;
		fieldgbc.weightx = 1;
		
		JFormattedTextField downRegulation = new JFormattedTextField(DecimalFormat.getInstance());
		downRegulation.setColumns(20);
	    JLabel downRegulationLabel = new JLabel("unregulated between:");
	    amtRegSubstAlgorithm.add(downRegulationLabel, labelgbc);
	    amtRegSubstAlgorithm.add(downRegulation, fieldgbc);
	
	    fieldgbc.gridx = labelgbc.gridx = 1;
	    
	    JFormattedTextField upRegulation = new JFormattedTextField(DecimalFormat.getInstance());
	    upRegulation.setColumns(20);
	    JLabel upRegulationLabel = new JLabel("and: ");
	    amtRegSubstAlgorithm.add(upRegulationLabel, labelgbc);
	    amtRegSubstAlgorithm.add(upRegulation,  fieldgbc);
	    
	    JButton calc = new JButton("do rating");
	    calc.addActionListener(l -> {
	    	//this.getRating(this.matrixGraphPanel);
	    	
	    	double lowerThreshold = 0.0;
			double upperThreshold = 0.0;
			try{
				lowerThreshold = ((Number) downRegulation.getValue()).doubleValue();
				upperThreshold = ((Number) upRegulation.getValue()).doubleValue();
			}catch(java.lang.NullPointerException e) {
				MainFrame.showMessageDialog( "please enter lower and  upper threshold!", "value missing!");

			}
	    	
	    	HashSet<MatrixLabelButton> pwButtonSet = this.matrixGraphPanel.getButtons(true);
			
			for(MatrixLabelButton pw: pwButtonSet) {
				//this.assignRating(pw);
				
				double score = this.amtRegSubstAlgorithm(pw, lowerThreshold, upperThreshold);
				
				DataPathway dpw = ((PathwayTextNode) pw.getLabelNode()).getPathway();
				dpw.setScore(score);
				
				System.out.println(dpw.getTitle() + ": " + score);
			}
	    });
	    amtRegSubstAlgorithm.add(calc,  GridBagUtils.setPosition(0,3, 2, 1));
	    
	    return amtRegSubstAlgorithm;
	}
	
	private double amtRegSubstAlgorithm(MatrixLabelButton pw, double lowerThreshold, double upperThreshold) {
		String timePoint = matrixGraphPanel.getTimePoint();
		int regulated = 0;
		
		LabelNode node = pw.getLabelNode();
		
		if(node instanceof PathwayTextNode) {
			PathwayTextNode ptn = (PathwayTextNode) node;
			Set<CellNode> cellSet = this.matrixGraphPanel.getGraph().getContent().getCol(ptn);
	
			for(CellNode cell: cellSet) {	
				if(cell.getMatching()) {
					CompoundTextNode cpdNode = cell.getCompoundNode();
					double mean = cpdNode.getSampleMeanFor(timePoint);
					if(mean < lowerThreshold || mean > upperThreshold) {
						regulated++;
					}
				}
			}
		}	
		return regulated;
	}
	
	/**
	 * panel for the the percentage of deregulated substances algorithm
	 * @return
	 */
	private JPanel percentRegSubstAlgorithmPanel(){
		JPanel percentRegSubstAlgorithmPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints labelgbc = GridBagUtils.setPosition(0, 0);
		labelgbc.fill = GridBagConstraints.HORIZONTAL;
		labelgbc.weightx = 1;
		
		GridBagConstraints fieldgbc = GridBagUtils.setPosition(0 ,1);
		fieldgbc.fill = GridBagConstraints.HORIZONTAL;
		fieldgbc.weightx = 1;
		
		JFormattedTextField downRegulation = new JFormattedTextField(DecimalFormat.getInstance());
		downRegulation.setColumns(20);
	    JLabel downRegulationLabel = new JLabel("unregulated between:");
	    percentRegSubstAlgorithmPanel.add(downRegulationLabel, labelgbc);
	    percentRegSubstAlgorithmPanel.add(downRegulation, fieldgbc);
	
	    fieldgbc.gridx = labelgbc.gridx = 1;
	    
	    JFormattedTextField upRegulation = new JFormattedTextField(DecimalFormat.getInstance());
	    upRegulation.setColumns(20);
	    JLabel upRegulationLabel = new JLabel("and: ");
	    percentRegSubstAlgorithmPanel.add(upRegulationLabel, labelgbc);
	    percentRegSubstAlgorithmPanel.add(upRegulation,  fieldgbc);
	    
	    JButton calc = new JButton("do rating");
	    calc.addActionListener(l -> {
	    	//this.getRating(this.matrixGraphPanel);
	    	
	    	double lowerThreshold = 0.0;
			double upperThreshold = 0.0;
			try{
				lowerThreshold = ((Number) downRegulation.getValue()).doubleValue();
				upperThreshold = ((Number) upRegulation.getValue()).doubleValue();
			}catch(java.lang.NullPointerException e) {
				MainFrame.showMessageDialog( "please enter lower and  upper threshold!", "value missing!");

			}
	    	
	    	HashSet<MatrixLabelButton> pwButtonSet = this.matrixGraphPanel.getButtons(true);
			
			for(MatrixLabelButton pw: pwButtonSet) {
				//this.assignRating(pw);
				
				double score = this.percentRegSubstAlgorithm(pw, lowerThreshold, upperThreshold);
				
				DataPathway dpw = ((PathwayTextNode) pw.getLabelNode()).getPathway();
				dpw.setScore(score);
				
				System.out.println(dpw.getTitle() + ": " + score);
			}
	    });
	    percentRegSubstAlgorithmPanel.add(calc,  GridBagUtils.setPosition(0,3, 2, 1));
	    
	    return percentRegSubstAlgorithmPanel;
	}
	
	private double percentRegSubstAlgorithm(MatrixLabelButton pw, double lowerThreshold, double upperThreshold) {
		String timePoint = matrixGraphPanel.getTimePoint();
		double regulated = 0;
		double totalSubst = 0;
		
		LabelNode node = pw.getLabelNode();
		
		if(node instanceof PathwayTextNode) {
			PathwayTextNode ptn = (PathwayTextNode) node;
			Set<CellNode> cellSet = this.matrixGraphPanel.getGraph().getContent().getCol(ptn);
	
			for(CellNode cell: cellSet) {	
				if(cell.getMatching()) {
					totalSubst++;
					CompoundTextNode cpdNode = cell.getCompoundNode();
					double mean = cpdNode.getSampleMeanFor(timePoint);
					if(mean < lowerThreshold || mean > upperThreshold) {
						regulated++;
					}
				}
			}
		}
				
		return 100*regulated/totalSubst;
	}
	
	/**
	 * panel for the the MSEA algorithm
	 * @return
	 */
	private JPanel mSEAAlgorithmPanel(){
		JPanel mSEAAlgorithmPanel = new JPanel(new GridBagLayout());

	    JButton calc = new JButton("do rating");
	    calc.addActionListener(l -> {
	    	//this.getRating(this.matrixGraphPanel);
	    	
	    	HashSet<MatrixLabelButton> pwButtonSet = this.matrixGraphPanel.getButtons(true);
			
			for(MatrixLabelButton pw: pwButtonSet) {
				//this.assignRating(pw);
				
				double score = this.mSEAAlgorithm(pw);
				
				DataPathway dpw = ((PathwayTextNode) pw.getLabelNode()).getPathway();
				dpw.setScore(score);
				
				System.out.println(dpw.getTitle() + ": " + score);
			}
	    });
	    mSEAAlgorithmPanel.add(calc,  GridBagUtils.setPosition(0,3, 2, 1));
	    
	    return mSEAAlgorithmPanel;
	}
	
	private double mSEAAlgorithm(MatrixLabelButton pw){

		PathwayTextNode node = (PathwayTextNode) pw.getLabelNode();
		DataPathway dpw = node.getPathway();
		double containedSubstances = 0;
		
		String timePoint = matrixGraphPanel.getTimePoint();
		
	//create ranked list of substances the treeMap will automatically sort according to the key
		HashSet<MatrixLabelButton> cpdButtons = matrixGraphPanel.getVisibleButtons(false);
		TreeMap<Double, ArrayList<SubstanceWithPathways>> valueSubstanceMap = new TreeMap<Double, ArrayList<SubstanceWithPathways>>();
		for(MatrixLabelButton button: cpdButtons) {
			CompoundTextNode cTnode = (CompoundTextNode) button.getLabelNode();
			SubstanceWithPathways swp = (SubstanceWithPathways) cTnode.getSubstance();
			Double value = cTnode.getSampleMeanFor(timePoint);
			if(valueSubstanceMap.containsKey(value)) {
				valueSubstanceMap.get(value).add(swp);
			} else {
				ArrayList<SubstanceWithPathways> swpList = new ArrayList<SubstanceWithPathways>();
				swpList.add(swp);
				valueSubstanceMap.put(value, swpList);
			}
			if(dpw.containsSubstance(swp)) {
				containedSubstances++;
			}
		}
		
		
		LinkedList<SubstanceWithPathways> orderedSubstanceList = new LinkedList<SubstanceWithPathways>();
		while(valueSubstanceMap.size() > 0) {
			Entry<Double, ArrayList<SubstanceWithPathways>> entry = valueSubstanceMap.pollFirstEntry();
			for(SubstanceWithPathways swp: entry.getValue()) {
				orderedSubstanceList.addLast(swp);
				/* test whether order is correct
				if(dpw.getTitle() != null && dpw.getTitle().equals("Polyunsaturated Fatty Acid (n3 and n6)")){
						System.out.println(swp.getName() + "\t" + entry.getKey());
				}
				*/
			}
		}
		
		double totalSubs = orderedSubstanceList.size();
		double notContainedSubs = totalSubs - containedSubstances;
		
		double decrement = Math.sqrt(containedSubstances/notContainedSubs);
		double increment = Math.sqrt(notContainedSubs/containedSubstances);

		double enrScore = this.calculateRunningSum(dpw, orderedSubstanceList, increment, decrement);
		
		double permutCount = 0;
		for(int seed = 1; seed <= 1000; seed++) {
			LinkedList<SubstanceWithPathways> originalList = new LinkedList<SubstanceWithPathways>();
			for(SubstanceWithPathways swp: orderedSubstanceList) {
				originalList.add(swp);
			}
			Collections.shuffle(originalList, new Random(seed));
			double randomEnrScore = this.calculateRunningSum(dpw, originalList, increment, decrement);
			if(randomEnrScore >= enrScore) {
				permutCount++;
			}
		}
		
		double pValue = permutCount/1000d;
		
		return pValue;
	}
	
	private double calculateRunningSum(DataPathway dpw, LinkedList<SubstanceWithPathways> subsList, double increment, double decrement){
		double runningSum = 0;
		double rsMax = Double.NEGATIVE_INFINITY;

			for(int rank = 0; rank < subsList.size(); rank++) {
				SubstanceWithPathways subst = subsList.get(rank); // entryList.get(rank).getValue();				
				if(subst.containsPathway(dpw) || dpw.containsSubstance(subst)) {
					runningSum = runningSum + increment;	
				} else {
					runningSum = runningSum - decrement;
				}
				rsMax = Math.max(runningSum, rsMax);
			}
		return rsMax;
	}
}
