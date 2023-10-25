package org.vanted.addons.matrix.keggUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.ws.rs.core.MediaType;

import org.apache.batik.ext.swing.GridBagConstants;
import org.graffiti.editor.MainFrame;
import org.graffiti.util.Pair;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.graph.CpdPwayGraph;
import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.mapping.MapToMatrixProcessor;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

public class MasterAdapter extends JFrame {
// fields
	private boolean pathways = true;
	private boolean pwClass = true;
	private boolean pwAssocCompounds = true;
	private int pwCutoff = 1;

	private boolean compounds = true;
	private boolean cpdID = true;
	private boolean cpdClass = true;
	private boolean cpdAltNames = true;
	private boolean cpdAssocPws = true;
	
	private final CpdPwayGraph graph;

	public MasterAdapter(CpdPwayGraph g){
		this.graph = g;
		this.rootPane.setLayout(new GridBagLayout());
		Font generalCheckBoxFont = new Font(null, Font.BOLD, 10);

		int runningGridY = 0;
		GridBagConstraints checkBoxGBC = new GridBagConstraints();
		checkBoxGBC.gridx = 1;
		checkBoxGBC.gridwidth = GridBagConstraints.REMAINDER;
		checkBoxGBC.anchor = GridBagConstants.WEST;
		
		JLabel header = new JLabel("retrieve data for:");
		header.setFont(new Font(null, Font.BOLD, 14));
		GridBagConstraints headerGBC = new GridBagConstraints();
		headerGBC.gridx = 0;
		headerGBC.gridy = runningGridY++;
		headerGBC.gridwidth = GridBagConstraints.REMAINDER;
		this.rootPane.add(header, headerGBC);
		
		JLabel spacer = new JLabel(" ");
		GridBagConstraints spacerGBC = new GridBagConstraints();
		spacerGBC.gridx = 0;
		spacerGBC.gridy = runningGridY++;
		spacerGBC.gridwidth = GridBagConstraints.REMAINDER;
		this.rootPane.add(spacer, spacerGBC);
		
		// pathways
		JCheckBox pathwayBox = new JCheckBox("pathways: ");
		pathwayBox.setFont(generalCheckBoxFont);
		pathwayBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			pathways = box.isSelected();
		});
		pathwayBox.setSelected(true);
		GridBagConstraints pathwayBoxGBC = new GridBagConstraints();
		pathwayBoxGBC.gridx = 0;
		pathwayBoxGBC.gridy = runningGridY++;
		pathwayBoxGBC.anchor = GridBagConstants.WEST;
		pathwayBoxGBC.gridwidth = GridBagConstraints.REMAINDER;
		this.rootPane.add(pathwayBox, pathwayBoxGBC);
		
		JCheckBox pwClassBox = new JCheckBox("pathway class");
		pwClassBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			pwClass = box.isSelected();
		});
		pwClassBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(pwClassBox, checkBoxGBC);
		
		JCheckBox pwAssocCompoundsBox = new JCheckBox("associated compounds");
		pwAssocCompoundsBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			pwAssocCompounds = box.isSelected();
		});
		pwAssocCompoundsBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(pwAssocCompoundsBox, checkBoxGBC);

	//compounds	
		JCheckBox compoundsBox = new JCheckBox("compounds");
		compoundsBox.setFont(generalCheckBoxFont);
		compoundsBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			compounds = box.isSelected();
		});
		compoundsBox.setSelected(true);
		GridBagConstraints compoundsBoxGBC = new GridBagConstraints();
		compoundsBoxGBC.gridx = 0;
		compoundsBoxGBC.gridy = runningGridY++;
		compoundsBoxGBC.gridwidth = GridBagConstraints.REMAINDER;
		compoundsBoxGBC.anchor = GridBagConstants.WEST;
		this.rootPane.add(compoundsBox, compoundsBoxGBC);
		
		JCheckBox cpdIDBox = new JCheckBox("compound ID");
		cpdIDBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			cpdID = box.isSelected();
		});
		cpdIDBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(cpdIDBox, checkBoxGBC);
		
		JCheckBox cpdClassBox = new JCheckBox("compound class");
		cpdClassBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			cpdClass = box.isSelected();
		});
		cpdClassBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(cpdClassBox, checkBoxGBC);
		
		JCheckBox cpdAltNamesBox = new JCheckBox("alternative names");
		cpdAltNamesBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			cpdAltNames = box.isSelected();
		});
		cpdAltNamesBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(cpdAltNamesBox, checkBoxGBC);

		JCheckBox cpdAssocPwsBox = new JCheckBox("alternative names");
		cpdAssocPwsBox.addActionListener(l -> {
			JCheckBox box = (JCheckBox) l.getSource();
			cpdAssocPws = box.isSelected();
		});
		cpdAssocPwsBox.setSelected(true);
		checkBoxGBC.gridy = runningGridY++;
		this.rootPane.add(cpdAssocPwsBox, checkBoxGBC);		
	
		JLabel cutoffLabel1 = new JLabel(" retrieve pathways containing");
		GridBagConstraints cutoffLabel1GBC = new GridBagConstraints();
		cutoffLabel1GBC.gridx = 0;
		cutoffLabel1GBC.gridy = runningGridY++;
		cutoffLabel1GBC.gridwidth = GridBagConstraints.REMAINDER;
		cutoffLabel1GBC.anchor = GridBagConstraints.WEST;
		this.rootPane.add(cutoffLabel1, cutoffLabel1GBC);
	
		JFormattedTextField pwCutoffField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		pwCutoffField.setColumns(3);
		pwCutoffField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent event) {
				JFormattedTextField field = (JFormattedTextField) event.getSource();
				try {
				 if (field.isEditValid()) {
			          field.commitEdit();
			       }
				}
				catch(java.text.ParseException e) {
					e.printStackTrace();
				}
				pwCutoff = Integer.parseInt(field.getValue().toString());
			}
			
		});
		pwCutoffField.addActionListener(l -> {
			JFormattedTextField field = (JFormattedTextField) l.getSource();
			this.pwCutoff = Integer.parseInt(field.getValue().toString());
		});
		pwCutoffField.setText("" + pwCutoff);
		GridBagConstraints pwCutoffFieldGBC = new GridBagConstraints();
		pwCutoffFieldGBC.gridx = 0;
		pwCutoffFieldGBC.gridy = runningGridY;
		this.rootPane.add(pwCutoffField, pwCutoffFieldGBC);
		
		JLabel cutoffLabel2 = new JLabel("or more measured compounds");
		GridBagConstraints cutoffLabel2GBC = new GridBagConstraints();
		cutoffLabel2GBC.gridx = 1;
		cutoffLabel2GBC.gridy = runningGridY++;
		this.rootPane.add(cutoffLabel2, cutoffLabel2GBC);
		
		//buttons
		JLabel bottom = new JLabel("hit \"complete rebuild\" to include the new data in the matrix");
		bottom.setVisible(false);
		GridBagConstraints bottomGBC = new GridBagConstraints();
		bottomGBC.gridx = 0;
		bottomGBC.gridy = 11;
		bottomGBC.gridwidth = GridBagConstraints.REMAINDER;
		this.rootPane.add(bottom, bottomGBC);

		spacerGBC.gridy = runningGridY++;
		this.rootPane.add(spacer, spacerGBC);
		
		JButton search = new JButton("get Data");
		search.setForeground(Color.BLUE);
		search.addActionListener(l -> this.loadInfo(g));
		checkBoxGBC.gridy = runningGridY + 1;
		this.rootPane.add(search, checkBoxGBC);
	}

	// TODO: run function in separate thread
	private void loadInfo(CpdPwayGraph g){
		if(this.pathways) {	
			for(PathwayTextNode pwNode: g.getPathwayNodes()) {
				this.loadPathwayInfo(pwNode);
			}
		}
		
		ExperimentInterface md = g.getExperiment();
		ArrayList<DataPathway> newPathways = new ArrayList<>();
		HashSet<PathwayTextNode> pwControlList = new HashSet<>(g.getPathwayNodes());
		
		if(this.compounds) {
	    	for(CompoundTextNode cpdNode: g.getCompoundNodes()) {
				ArrayList<DataPathway> newPwsFromOneSubstance = this.loadCompoundInfo(g, cpdNode);
				if(newPwsFromOneSubstance != null && newPwsFromOneSubstance.size() > 0) {
					newPathways.addAll(newPwsFromOneSubstance);
				}
			}
		}

		//remove all pathways from newPathways that have been in the matrix before and thus are already represented by a LabelNode
		Iterator<DataPathway> iterator = newPathways.iterator();
		
		while(iterator.hasNext()) {
			DataPathway newPw = iterator.next();
			String newPwTitle = newPw.getTitle();
		
			for(PathwayTextNode node: pwControlList) {
				DataPathway nodePw = node.getPathway();

				if(nodePw != null && newPwTitle.equals(nodePw.getTitle())) {
					iterator.remove();
				}
			}
		}
				
		SwingUtilities.invokeLater(() -> {
			for(DataPathway pw: newPathways) {
				if(pw != null && pw.getSubstances().size() > 0 && pw.getSubstances().size() <= pwCutoff) {
					// for all substances of this pathway remove the pathway from the substance
					for(SubstanceWithPathways swp: pw.getSubstances()) {
						swp.removePathway(pw);
						//add the substance from new if it was present before
						if(md.contains(swp)) {
							md.remove(swp);
							md.add(swp);
						}
						else {
							md.add(swp);
						}
					}
					g.removePathway(pw);
				}
			}
		});
		
		SwingUtilities.invokeLater(() -> ExperimentDataProcessingManager.getInstance().processIncomingData(md,
				 (Class) MapToMatrixProcessor.class));
	}

	private void loadPathwayInfo(PathwayTextNode pwNode){
		
		DataPathway pw = pwNode.getPathway();
		if(pw != null) {
			String pwTitle = pw.getTitle();


			RestService restServiceFind = new RestService("https://rest.kegg.jp/" + "find/" + "pathway/");
			String keggResponse = (String) restServiceFind.makeRequest(pwTitle, MediaType.TEXT_PLAIN_TYPE, String.class);

			if(keggResponse != null && keggResponse.length() > 1) {
				String pwId = keggResponse.substring(keggResponse.indexOf("path:") + 5, keggResponse.indexOf("\t"));

				if(this.pwClass) {
					String keggGetResponse = PathwayAdapter.keggGet(pwId);
					ArrayList<String> pwClasses = ExtractFromString.superPwFromEntry(keggGetResponse);
					if(pwClasses != null && pwClasses.size() > 0) {
						pw.setSuperPathway(pwClasses.get(1));
					}
				}
				
				if(this.pwAssocCompounds) {
					//build up a lists in which the substances currently mapped to this pw are represented as Pair<String, String>
					ArrayList<Pair<String, String>> matrixCpdList = new ArrayList<>();
					ArrayList<Pair<String, String>> matrixCpdControlList = new ArrayList<>();

					for(CompoundTextNode cpdNode: pwNode.getCompoundTextNodes()){
						SubstanceWithPathways swp = (SubstanceWithPathways) cpdNode.getSubstance();
						String cpdKeggId = null;
						
						if(swp.getSynonymMap() != null) {			
							cpdKeggId = swp.getDbId("KEGG");
						}
						
						if(cpdKeggId == null) {
							cpdKeggId = "";
						}
						String cpdName = swp.getName();
						if(cpdName == null) {
							cpdName = "";
						}
						Pair<String, String> presentPair = new Pair<>(cpdKeggId, cpdName);
						
						matrixCpdList.add(presentPair);
						matrixCpdControlList.add(presentPair);
					}

					// is needed so that the correct network view can be retrieved		
					pw.setKeggPathway(keggResponse.replace("\t" , "     "));
					
					// now find matchings with compounds present in the matrix and add a respective mapping to the PathwayTextNode
					String[] compsWithIds;
					
					try{
						compsWithIds = PathwayAdapter.getCompoundsFromKegg(pwId);
					}catch(Exception e) {
						MainFrame.getInstance().showMessageDialog("The pathway " + pw.getTitle() + "contains too many compounds to retrieve them all");
						compsWithIds = new String[0];
					}
					
					for(String compound: compsWithIds) {
						String keggId = compound.substring(0, 6);
						String name = compound.substring(compound.indexOf("   ") + 3);
						Pair<String, String> dbPair = new Pair<>(keggId, name);
						
						boolean matchFound = false;
						
						for(Pair<String, String> presentPair: matrixCpdList){
						
							if(dbPair.getFst().equals(presentPair.getFst()) || dbPair.getSnd().equals(presentPair.getSnd())) {
								pwNode.addMapping(presentPair, dbPair);
								matrixCpdControlList.remove(presentPair);
								matchFound = true;
							}
						}
						if(!matchFound) {
							pwNode.addMapping(new Pair<>("", ""), dbPair);
						}
					}
					
					//empty mapping for those left in the control list
					for(Pair<String, String> matrixPair: matrixCpdControlList) {
						pwNode.addMapping(matrixPair, new Pair<>("", ""));
					}
				}
			}
		}
	}

	private ArrayList<DataPathway> loadCompoundInfo(CpdPwayGraph g, CompoundTextNode cpdNode){
		SubstanceWithPathways substance = (SubstanceWithPathways) cpdNode.getSubstance();
		ArrayList<DataPathway> newPathways = new ArrayList<>();

	// first request to KEGG	
		String query = substance.getDbId("KEGG");
		
		if(query == null || query.isEmpty()) {
			query = substance.getName();
		}
		
		RestService restServiceLink = new RestService("http://rest.kegg.jp/" + "find/" + "compound/");
		String findRequest = (String) restServiceLink.makeRequest(query, MediaType.TEXT_PLAIN_TYPE, String.class);
		//e.g. findRequest = cpd:C00186\t(S)-Lactate; L-Lactate; L-Lactic acid

		if(findRequest != null && findRequest.length() > 1) {

	//second request with response of first request as query		
			RestService restServiceGetLink = new RestService("http://rest.kegg.jp/" + "get/");
			String getRequest = (String) restServiceGetLink.makeRequest(findRequest, MediaType.TEXT_PLAIN_TYPE, String.class);
			
			if(getRequest != null && getRequest.length() > 1) {
		// set ID	
				if(this.cpdID) {
					substance.setDbId("KEGG", findRequest.substring(4, 10));
				}
		// set substance group		
				if(this.cpdClass) {
					String[] keggSubstClassData = ExtractFromString.extractSubstanceClasses(getRequest).toArray(new String[0]);
					if(keggSubstClassData.length > 1) {
						substance.setSubstancegroup(keggSubstClassData[1]);		
					}
				}
		// set alternative names		
				if(this.cpdAltNames) {
					String[] keggAltNamesData = ExtractFromString.extractCompoundNames(findRequest.replace("\t", "   ")).toArray(new String[0]);
					for(String altName: keggAltNamesData) {
						altName = altName.trim();
						if(!substance.getAlternativeNames().contains(altName)) {
							substance.addAlternativeName(altName);
						}
					}
				}
		//set associated Pathways	
				if(this.cpdAssocPws) {
	
					ArrayList<String>  keggPathwaysData = ExtractFromString.relatedPwsFromCompoundEntry(getRequest);
					
					
					if(keggPathwaysData.size() > 0) {
						
						newPathways = this.graph.checkAddReturnNewPws(keggPathwaysData);
				    	substance.addPathways(newPathways);
				    	
						for(DataPathway pw: newPathways) {
					    	pw.addSubstance(substance);
						}
				    	g.addPathways(newPathways);
					}
				}
			}
		}
		
		return newPathways;
	}	
}