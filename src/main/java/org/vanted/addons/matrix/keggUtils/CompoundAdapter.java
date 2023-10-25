package org.vanted.addons.matrix.keggUtils;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import javax.ws.rs.core.MediaType;

import org.apache.batik.ext.swing.GridBagConstants;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.graph.CpdPwayGraph;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;
import org.vanted.addons.matrix.utils.EditableList;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

import org.vanted.addons.matrix.mapping.MapToMatrixProcessor;

public class CompoundAdapter extends JFrame {
	private final CompoundTextNode compoundNode;
	private final MatrixGraphPanel matrixPanel;
	private JPanel searchPanel;
	private JPanel infosAndResults;
	private JPanel toolBar;
	private JTextField currName;
	private JTextField keggName;
	private JTextField currId;
	private JTextField keggId;
	private JList<String> compoundSearch;
	private JTextField currSubstClass;
	private JList<String> keggSubstClass;
	private EditableList currAltNames;
	private JList<String> keggAltNames;
	private EditableList currPathways;
	private JList<String> keggPathways;
	
	public CompoundAdapter(CompoundTextNode cpdNode, MatrixGraphPanel mgp){
		this.compoundNode = cpdNode;
		this.matrixPanel = mgp;
		
	    //prepare components for this frame
		JRootPane content = new JRootPane();
		content.setLayout(new BorderLayout());
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		
		this.buildSearchPanel();
		this.searchPanel.setPreferredSize(new Dimension(500, 150));
		searchPanel.revalidate();
		this.buildInfosAndResults();
		this.infosAndResults.setPreferredSize(new Dimension(500, 550));
		infosAndResults.revalidate();
		this.buildToolbar();
		this.toolBar.setPreferredSize(new Dimension(200, 700));
		toolBar.revalidate();
		
		center.add(searchPanel);
		center.add(infosAndResults);
		
		content.add(BorderLayout.CENTER, center);
		content.add(BorderLayout.EAST, toolBar);
		this.setRootPane(content);
		
	    //extract information currently represented in matrix
		this.getInfosFromNode();
	}

//components	
	private void buildSearchPanel() {
		this.searchPanel = new JPanel(new GridBagLayout());
	//name	
		currName = new JTextField(" ");
		GridBagConstraints currNameGBC = this.setPosition(0, 0, 4, 1);
		currNameGBC.weightx = 1;
		currNameGBC.weighty = 0;
		searchPanel.add(currName, currNameGBC);
		
		keggName = new JTextField(" ");
		GridBagConstraints keggNameGBC = this.setPosition(6, 0, 4, 1);
		keggNameGBC.weightx = 1;
		keggNameGBC.weighty = 0;
		searchPanel.add(keggName, keggNameGBC);
		
		JButton adoptName = new JButton("<");
		adoptName.addActionListener(l -> {
			String name = keggName.getText();
			currName.setText(name);
		});
		GridBagConstraints adoptNameGBC = this.setPosition(4, 0, 2, 1);
		adoptNameGBC.weightx = 0;
		adoptNameGBC.weighty = 0;
		adoptNameGBC.fill = GridBagConstraints.NONE;
		searchPanel.add(adoptName, adoptNameGBC);	
		
	//ID	
		currId = new JTextField(" ");
		GridBagConstraints currIdGBC = this.setPosition(0, 1, 4, 1);
		currIdGBC.weightx = 1;
		currIdGBC.weighty = 0;
		searchPanel.add(currId, currIdGBC);

		keggId = new JTextField(" ");
		GridBagConstraints keggIdGBC = this.setPosition(6, 1, 4, 1);
		keggIdGBC.weightx = 1;
		keggIdGBC.weighty = 0;
		searchPanel.add(keggId, keggIdGBC);
		
		JButton adoptId = new JButton("<");
		adoptId.addActionListener(l -> {
			String id = keggId.getText();
			currId.setText(id);
		});
		GridBagConstraints adoptIdGBC = this.setPosition(4, 1, 2, 1);
		adoptIdGBC.weightx = 0;
		adoptIdGBC.weighty = 0;
		adoptIdGBC.fill = GridBagConstraints.NONE;
		searchPanel.add(adoptId, adoptIdGBC);	
		
	// display search results
		compoundSearch = new JList<>(new DefaultListModel<>());
		compoundSearch.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScrollPane = new JScrollPane(compoundSearch);
		GridBagConstraints listScrollPaneGBC = this.setPosition(0, 2, 10, GridBagConstraints.REMAINDER);
		listScrollPaneGBC.fill = GridBagConstraints.BOTH;
		listScrollPaneGBC.weighty = 1;
		searchPanel.add(listScrollPane,listScrollPaneGBC );
	}
	
	private void buildInfosAndResults() {
		this.infosAndResults = new JPanel(new GridBagLayout());
		GridBagConstraints labelGBC = this.setPosition(0, 0, 10, 1);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;
		labelGBC.weighty = 0;
		
	    //substance class
		JLabel substClassLabel = new JLabel("substance class");
		substClassLabel.setBackground(Color.LIGHT_GRAY);
		substClassLabel.setOpaque(true);
		infosAndResults.add(substClassLabel, labelGBC);
		
		currSubstClass = new JTextField(" ");
		GridBagConstraints currSubstClassGBC = this.setPosition(0, 1, 4, 1);
		currSubstClassGBC.fill = GridBagConstraints.HORIZONTAL;
		currSubstClassGBC.weightx = 1;
		currSubstClassGBC.weighty = 0;
		infosAndResults.add(currSubstClass, currSubstClassGBC);

		keggSubstClass = new JList<>();
		keggSubstClass.setListData(new String[] {" "});
		GridBagConstraints keggSubstClassGBC = this.setPosition(5, 1, 4, 1);
		keggSubstClassGBC.fill = GridBagConstraints.BOTH;
		keggSubstClassGBC.weightx = 1;
		keggSubstClassGBC.weighty = 1;
		JScrollPane keggSubstClassScrolling = new JScrollPane(keggSubstClass);
		infosAndResults.add(keggSubstClassScrolling, keggSubstClassGBC);
		
		JButton adoptClass = new JButton("<");
		adoptClass.addActionListener(l -> {
			String id = keggSubstClass.getSelectedValue();
			currSubstClass.setText(id);
		});
		GridBagConstraints adoptClassGBC = this.setPosition(4, 1, 1, 1);
		adoptClassGBC.weightx = 0;
		adoptClassGBC.weighty = 0;
		adoptClassGBC.fill = GridBagConstraints.HORIZONTAL;
		infosAndResults.add(adoptClass, adoptClassGBC);
		
	    //alternative names
		JLabel altNamesLabel = new JLabel("alternative names");
		altNamesLabel.setBackground(Color.LIGHT_GRAY);
		altNamesLabel.setOpaque(true);
		labelGBC.gridy = 2;
		infosAndResults.add(altNamesLabel, labelGBC);
		
		currAltNames = new EditableList(new String[] {}, true, true);
		GridBagConstraints currAltNamesGBC = this.setPosition(0, 3, 4, 2);
		currAltNamesGBC.fill = GridBagConstraints.BOTH;
		currAltNamesGBC.weightx = 1;
		currAltNamesGBC.weighty = 1;	
		infosAndResults.add(currAltNames, currAltNamesGBC);

		keggAltNames = new JList<>();
		keggAltNames.setListData(new String[] {" "});
		keggAltNames.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints keggAltNamesGBC = this.setPosition(5, 3, 4, 2);
		keggAltNamesGBC.fill = GridBagConstraints.BOTH;
		keggAltNamesGBC.weightx = 1;
		keggAltNamesGBC.weighty = 1;
		JScrollPane keggAltNamesScrolling = new JScrollPane(keggAltNames);
		infosAndResults.add(keggAltNamesScrolling, keggAltNamesGBC);
		
		JButton adoptSelName = new JButton("<");
		adoptSelName.addActionListener(l -> {
			String[] values =  keggAltNames.getSelectedValuesList().toArray(new String[0]);
			currAltNames.addValues(values);
		});
		GridBagConstraints adoptSelNameGBC = this.setPosition(4, 3, 1, 1);
		adoptSelNameGBC.weightx = 0;
		adoptSelNameGBC.weighty = 0;
		adoptSelNameGBC.fill = GridBagConstraints.HORIZONTAL;
		infosAndResults.add(adoptSelName, adoptSelNameGBC);
		
		JButton removeAltNames = new JButton("remove");
		removeAltNames.addActionListener(l -> currAltNames.removeSelectedValues());
		GridBagConstraints adoptAllNamesGBC = this.setPosition(4, 4, 1, 1);
		adoptAllNamesGBC.weightx = 0;
		adoptAllNamesGBC.weighty = 0;
		adoptAllNamesGBC.fill = GridBagConstraints.NONE;
		infosAndResults.add(removeAltNames, adoptAllNamesGBC);
		
	    //associated pathways
		JLabel assocPathways = new JLabel("pathways");
		assocPathways.setBackground(Color.LIGHT_GRAY);
		assocPathways.setOpaque(true);
		labelGBC.gridy = 5;
		infosAndResults.add(assocPathways, labelGBC);
		
		currPathways = new EditableList(new String[] {}, true, true);
		GridBagConstraints currPathwaysGBC = this.setPosition(0, 6, 4, 2);
		currPathwaysGBC.fill = GridBagConstraints.BOTH;
		currPathwaysGBC.weightx = 1;
		currPathwaysGBC.weighty = 1;
		infosAndResults.add(currPathways, currPathwaysGBC);

		keggPathways = new JList<>();
		keggPathways.setListData(new String[] {" "});
		keggPathways.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints keggPathwaysGBC = this.setPosition(5, 6, 4, 2);
		keggPathwaysGBC.fill = GridBagConstraints.BOTH;
		keggPathwaysGBC.weightx = 1;
		keggPathwaysGBC.weighty = 1;
		JScrollPane keggPathwaysScrolling = new JScrollPane(keggPathways);
		infosAndResults.add(keggPathwaysScrolling, keggPathwaysGBC);
		
		JButton adoptSelPathway = new JButton("<");
		adoptSelPathway.addActionListener(l -> {
			String[] values = keggPathways.getSelectedValuesList().toArray(new String[0]);
			if(values.length > 0) {
				currPathways.addValues(values);
			}
		});
		GridBagConstraints adoptSelPathwayGBC = this.setPosition(4, 6, 1, 1);
		adoptSelPathwayGBC.weightx = 0;
		adoptSelPathwayGBC.weighty = 0;
		adoptSelPathwayGBC.fill = GridBagConstraints.NONE;
		infosAndResults.add(adoptSelPathway, adoptSelPathwayGBC);	
		
		JButton removePathways = new JButton("remove");
		removePathways.addActionListener(l -> currPathways.removeSelectedValues());
		GridBagConstraints adoptAllPathwaysGBC = this.setPosition(4, 7, 1, 1);
		adoptAllPathwaysGBC.weightx = 0;
		adoptAllPathwaysGBC.weighty = 0;
		adoptAllPathwaysGBC.fill = GridBagConstraints.HORIZONTAL;
		infosAndResults.add(removePathways, adoptAllPathwaysGBC);
	}
	
	private void buildToolbar() {
		this.toolBar = new JPanel();
		toolBar.setLayout(new GridBagLayout());
		
		GridBagConstraints buttonsGBC = this.setPosition(0);
		buttonsGBC.fill = GridBagConstants.HORIZONTAL;
		buttonsGBC.weighty = 0;
		
		JButton searchByName = new JButton("search by name");
		searchByName.addActionListener(l -> {
			String currentName = currName.getText();
			if(currentName.length() > 0) {
				this.getSubstanceFromKegg(currentName);
			}
		});
		toolBar.add(searchByName, buttonsGBC);
		
		JButton searchById = new JButton("search by ID");
		searchById.addActionListener(l -> {
			String currentId = currId.getText();
			if(currentId.length() > 0) {
				this.getSubstanceFromKegg(currentId);
			}
		});
		buttonsGBC.gridy = 1;
		toolBar.add(searchById, buttonsGBC);
		
	    //get data for kegg result
		JButton getKeggData = new JButton("get data for result");
		getKeggData.addActionListener(l -> {
			String keggSubstance = this.compoundSearch.getSelectedValue();
			if(keggSubstance != null && keggSubstance.length() > 0) {
				this.getKeggData(keggSubstance);
			}
		});
		buttonsGBC.gridy = 2;
		toolBar.add(getKeggData, buttonsGBC);
		
	    //get Pathways from matrix
		JLabel matrixPwListLabel = new JLabel("pathways in matrix: ");
		matrixPwListLabel.setBackground(Color.LIGHT_GRAY);
		matrixPwListLabel.setOpaque(true);
		GridBagConstraints matrixPwListLabelGBC = this.setPosition(0, 3, 1, 1);
		matrixPwListLabelGBC.weighty = 0;
		matrixPwListLabelGBC.fill = GridBagConstants.HORIZONTAL;
		toolBar.add(matrixPwListLabel, matrixPwListLabelGBC); 
		
		CpdPwayGraph cpdPwayGraph = (CpdPwayGraph) this.compoundNode.getGraph();
		String[] matrixPws = cpdPwayGraph.getPathwaysAsStrings().toArray(new String[0]);
		EditableList matrixPwList = new EditableList(matrixPws, false, false);
		matrixPwList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		matrixPwList.sort();
		GridBagConstraints matrixPwListGBC = this.setPosition(0, 4, 1, 1);
		matrixPwListGBC.fill = GridBagConstants.BOTH;
		matrixPwListGBC.weighty = 1;
		toolBar.add(matrixPwList, matrixPwListGBC);
		
		JButton addSelectedMatrixPws = new JButton("add pathways to list");
		addSelectedMatrixPws.addActionListener(l -> {
			HashSet<String> pwSet = new HashSet<>(matrixPwList.getSelectedValuesList());
			for(String pw: pwSet) {
				if(!Arrays.asList(this.currPathways.getCurrentValues()).contains(pw)) {
					this.currPathways.addValues(new String[] {pw});
				}
			}
		});
		buttonsGBC.gridy = 5;
		toolBar.add(addSelectedMatrixPws, buttonsGBC);
		
	    //save to matrix
		JLabel space = new JLabel();
		toolBar.add(space, this.setPosition(6));
		
		JButton saveToNode = new JButton("save data to matrix");
		saveToNode.addActionListener(l -> this.saveToNode());
		buttonsGBC.gridy = 7;
		toolBar.add(saveToNode, buttonsGBC);
	}
	
	 /**
	 * retrieves the values currently assigned to the substance of this CompoundTextNode
	 * makes the respective structures display this information
	 */
	private void getInfosFromNode(){
	    	SubstanceWithPathways substance = (SubstanceWithPathways) this.compoundNode.getSubstance();
	    	currName.setText(substance.getName());
	    	
	    	currId.setText(substance.getSynonyme(3));
	    	
	    	currSubstClass.setText(substance.getSubstancegroup());
	    	
	    	String[] altNames = substance.getAlternativeNames().toArray(new String[0]);
	    	currAltNames.setValues(altNames);
	    	
	    	String[] pathways = substance.getPathwaysAsStrings().toArray(new String[0]);
	    	currPathways.setValues(pathways);
	    }

	/**
	 * returns an instance of GridBagConstraints with default values and a y position
     */
    private GridBagConstraints setPosition(int y) {
    	return new GridBagConstraints(0, y, 1, 1, 0.5,
				0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0);
    }
    
    /**
     * returns an instance of GridBagConstraints with default values and x and y as position
     * and size constraints
     * @param width: number of columns
     * @param height: number of rows
     */
    private GridBagConstraints setPosition(int x, int y, int width, int height) {
    	return new GridBagConstraints(x, y, width, height, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
    }
    
    /**
     * makes a kegg request that retrieves substances that fit to the given query
     * enters their names in the JList compoundSearch
     */
    private void getSubstanceFromKegg(String query){
    	RestService restServiceLink = new RestService("https://rest.kegg.jp/" + "find/" + "compound/");
		String request = (String) restServiceLink.makeRequest(query, MediaType.TEXT_PLAIN_TYPE, String.class);
   
		String[] substances = ExtractFromString.extractAlternativeCompounds(request);
		
		compoundSearch.setListData(substances);
	}
    
    /**
     * makes kegg requests to retrieve the following data to the given substance:
     * substance class, id, alternative names, related pathways
     * enters the received data to the respective text fields and JLists of this CompoundFinder
     */
    private void getKeggData(String keggSubstance){
    	
    	RestService restServiceLink = new RestService("https://rest.kegg.jp/" + "get/");
		String request = (String) restServiceLink.makeRequest(keggSubstance, MediaType.TEXT_PLAIN_TYPE, String.class);

		keggId.setText(keggSubstance.substring(0, 6));
		
		String[] keggSubstClassData = ExtractFromString.extractSubstanceClasses(request).toArray(new String[0]);
		if(keggSubstClassData.length == 0) {
			keggSubstClassData = new String[] {" "};
		}
		this.keggSubstClass.setListData(keggSubstClassData);
		
		String[] keggAltNamesData = ExtractFromString.extractCompoundNames(keggSubstance).toArray(new String[0]);
		this.keggAltNames.setListData(keggAltNamesData);
		this.keggName.setText(keggAltNamesData[0]);
		
		String[] keggPathwaysData = ExtractFromString.relatedPwsFromCompoundEntry(request).toArray(new String[0]);
		if(keggPathwaysData.length == 0) {
			keggPathwaysData = new String[] {" "};
		}
		this.keggPathways.setListData(keggPathwaysData);

		this.getRootPane().updateUI();
		this.pack();
    }

    /**
     * assigns the data currently displayed in this CompoundFinder to its CompoundTextNode
     * i.e. to the substance assigned to it
     */
    private void saveToNode(){
    	SubstanceWithPathways substance = (SubstanceWithPathways) this.compoundNode.getSubstance();
    	
    	String name = currName.getText();
    	substance.setName(name);
    	
    	String id = currId.getText();
		substance.setDbId("KEGG", id);

    	String substClass = currSubstClass.getText();
    	substance.setSubstancegroup(substClass);

		String[] altNameArray = currAltNames.getCurrentValues();
		HashSet<String> altNames = new HashSet<>(Arrays.asList(altNameArray).subList(0, altNameArray.length - 1));
    	substance.setAlternativeNames(altNames);
    	
    	ArrayList<String> pathways = new ArrayList<> (Arrays.asList(currPathways.getCurrentValues()));
    	
    	CpdPwayGraph graph = matrixPanel.getGraph();
    	
    	ArrayList<DataPathway> newPathways = graph.checkAddReturnNewPws(pathways);
    	substance.setPathways(newPathways);

    	ExperimentInterface md = graph.getExperiment();

		ExperimentDataProcessingManager.getInstance().processIncomingData(md,
				(Class) MapToMatrixProcessor.class);

    }
}
