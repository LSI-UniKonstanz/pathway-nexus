package org.vanted.addons.matrix.keggUtils;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.ws.rs.core.MediaType;

import org.graffiti.editor.MainFrame;
import org.graffiti.util.Pair;
import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;
import org.vanted.addons.matrix.graph.CompoundTextNode;

import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

public class PathwayAdapter extends JFrame {
	private JPanel searchPanel;
	private JList<String> pathwaySearch;
	private MatchingTable compoundTable;
	private final MatrixGraphPanel matrixGraphPanel;
	private final PathwayTextNode pathwayNode;
	private final DataPathway matrixPathway;
	private MatchingTableMap compoundMap = new MatchingTableMap();
	private final HashMap<Pair<String, String>, CompoundTextNode> matrixCompoundMap = new HashMap<>();
	private String keggPathway;
	private String organism = "ko";
	private final JComboBox<String> dBSuperPws = new JComboBox<>();
	private JTextField matrixSuperPw;
	private final HashMap<String, String> keggCodeMap = new HashMap<>();
	
	public PathwayAdapter(PathwayTextNode pathway, MatrixGraphPanel mgPanel) {
		super("Pathway Adapter");
		keggCodeEntries();

		this.matrixGraphPanel = mgPanel;
		this.pathwayNode = pathway;
		this.matrixPathway = pathway.getPathway();

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(this.buildSearchPanel());
		content.add(this.buildPwInfoPanel());
		content.add(this.buildResultPanel());
		this.setContentPane(content);
		this.addMatrixCompoundsToTable();
	}

	private void keggCodeEntries() {
		keggCodeMap.put("KEGG orthology", "ko");
		keggCodeMap.put("homo sapiens", "hsa");
		keggCodeMap.put("mus musculus", "mmu");
		keggCodeMap.put("drosophila melanogaster", "dme");
		keggCodeMap.put("saccharomyces cerevisiae", "sce");
	}
	
	//building components of this window
	private JPanel buildSearchPanel(){
		searchPanel = new JPanel(new GridBagLayout());
		//searching	
		RestService restServiceFind = new RestService("https://rest.kegg.jp/" + "find/" + "pathway/");
			
		JTextField searchField = new JTextField(matrixPathway.getTitle());
		searchPanel.add(searchField, this.setPosition(0, 0));
		searchField.addActionListener(l -> {
			String pwTitle = searchField.getText();
			String keggResponse = (String) restServiceFind.makeRequest(pwTitle, MediaType.TEXT_PLAIN_TYPE, String.class);
			DefaultListModel<String> model = (DefaultListModel<String>) this.pathwaySearch.getModel();
			model.removeAllElements();
			model.addAll(this.getPathwayArray(keggResponse));
			pathwaySearch.repaint();
			searchPanel.updateUI();
			this.pack();
		});

		JButton searchKegg = new JButton("search Kegg");
		searchKegg.addActionListener(l -> {
			String pwTitle = searchField.getText();
			String keggResponse = (String) restServiceFind.makeRequest(pwTitle, MediaType.TEXT_PLAIN_TYPE, String.class);
			DefaultListModel<String> model = (DefaultListModel<String>) this.pathwaySearch.getModel();
			model.removeAllElements();
			for(String pw: getPathwayArray(keggResponse)) {
				model.addElement(pw);
			}
			pathwaySearch.repaint();
			searchPanel.updateUI();
			this.pack();
		});
		searchPanel.add(searchKegg, setPosition(1,0));

		searchPanel.add(new JLabel("Organism:"), setPosition(0, 1));
		JComboBox<String> keggCode = new JComboBox<> (keggCodeMap.keySet().toArray(new String[0]));
		keggCode.setSelectedItem(keggCode.getItemAt(0));
		keggCode.addActionListener(l -> organism = keggCodeMap.get((String) keggCode.getSelectedItem()));
		searchPanel.add(keggCode, setPosition(1,1));
			
		//first order results (list of pathways)
		pathwaySearch = new JList<>(new DefaultListModel<>());

		pathwaySearch.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScrollPane= new JScrollPane(pathwaySearch);
		listScrollPane.setPreferredSize(new Dimension(400, 50));
		listScrollPane.setMinimumSize(new Dimension(400, 50));
		listScrollPane.setMaximumSize(new Dimension(400, 50));
		searchPanel.add(listScrollPane, new GridBagConstraints(0,
				2,
				2,
				1,
				0.5,
				0.5,
				GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));

		if(this.matrixPathway.getKeggPathway() != null) {
			DefaultListModel<String> model = (DefaultListModel<String>) this.pathwaySearch.getModel();
			model.addElement(matrixPathway.getKeggPathway());
			this.pathwaySearch.setSelectedIndex(0);
			this.keggPathway = this.matrixPathway.getKeggPathway();
		}
		return searchPanel;
	}

	private JPanel buildPwInfoPanel(){
		JPanel pwInfoPanel = new JPanel(new GridBagLayout());
		
		matrixSuperPw = new JTextField(matrixPathway.getSuperPathway());
		pwInfoPanel.add(matrixSuperPw, this.setPosition(0, 0));
		
		pwInfoPanel.add(dBSuperPws, this.setPosition(2, 0));
		
		JButton adopt = new JButton("<-");
		adopt.addActionListener(l -> {
			if(dBSuperPws.getSelectedItem() != null) {
				String newSuperPw = dBSuperPws.getSelectedItem().toString();
				matrixSuperPw.setText(newSuperPw);
				this.matrixPathway.setSuperPathway(newSuperPw);
			}
		});
		pwInfoPanel.add(adopt, this.setPosition(1, 0));		
		return pwInfoPanel;
	}
	
	private JPanel buildResultPanel(){
		JPanel resultPanel = new JPanel();
		
		resultPanel.add(this.getCompoundTable());
		
		JPanel functionPanel = new JPanel();
		functionPanel.setLayout(new BoxLayout(functionPanel, BoxLayout.Y_AXIS));
		
		//save currently displayed results to the DataPathway
		JButton test =  new JButton("save results");
		test.addActionListener(l -> saveToMatrix());
		functionPanel.add(test);
		

		JButton keggData = new JButton("get data from DB");
		keggData.addActionListener(l -> {
			String selection = this.pathwaySearch.getSelectedValue();
			if(selection != null) {
				this.keggPathway = selection;
				selection = selection.replace("     ", "\t");
				String pwId = selection.substring(selection.indexOf("path:") + 5, selection.indexOf("\t"));
				String[] compsWithIds;
				
				try{
					compsWithIds = getCompoundsFromKegg(pwId);
				}catch(Exception e) {
					MainFrame.getInstance().showMessageDialog("This Kegg Pathway contains too many substances");
					compsWithIds = new String[0];
				}
				//compounds
				compoundMap.deleteAllScnd();
				addDBCompoundsToTable(compsWithIds);
				
				//super pathway
				String keggGetResponse = keggGet(pwId);
				ArrayList<String> pwClasses = ExtractFromString.superPwFromEntry(keggGetResponse);

				dBSuperPws.removeAllItems();
				for(String s: pwClasses) {
					dBSuperPws.addItem(s);
				}
				// since the last class retrieved is in most cases the most specific one
				dBSuperPws.setSelectedIndex(pwClasses.size() - 1);
			}
		});
		functionPanel.add(keggData);
		
		JButton swap = new JButton("dissolve mapping");
		swap.addActionListener(l -> {
			Pair<String, String> leftSideSelection = compoundTable.getFirstSelected();
			if(leftSideSelection == null)
				return;
			
			compoundMap.dissolveMapping(compoundTable.getFirstSelected());
			this.compoundTable.setData(compoundMap.toArray());
		});
		functionPanel.add(swap);
		
		JButton map = new JButton("map together");
		map.addActionListener(l -> {
			Pair<String, String> leftSideSelection = compoundTable.getFirstSelected();
			Pair<String, String> rightSideSelection = compoundTable.getSecondSelected();
			if(leftSideSelection == null || rightSideSelection == null)
				return;
			
			compoundMap.map(leftSideSelection, rightSideSelection);
			this.compoundTable.setData(compoundMap.toArray());
		});
		functionPanel.add(map);
		
		JButton revalidate = new JButton("revalidate");
		revalidate.addActionListener(l -> this.compoundTable.setData(compoundMap.toArray()));
		functionPanel.add(revalidate);
		
		JButton network = new JButton("network View");
		network.addActionListener(l -> {			
			String selection = this.pathwaySearch.getSelectedValue();
			if (selection == null)
				return;
			selection = selection.replace("     ", "\t");
			String pwName = selection.substring(selection.indexOf("\t") + 1);
			String pwId = selection.substring(selection.indexOf("path:") + 5, selection.indexOf("\t"));
			
			this.openNetworkView(pwId, pwName);
		});
		if (this.pathwaySearch.getSelectedValue() == null)
			network.setEnabled(false);
		this.pathwaySearch.addListSelectionListener(l -> network.setEnabled(this.pathwaySearch.getSelectedValue() != null));
		functionPanel.add(network);
		
		resultPanel.add(functionPanel);		
		return resultPanel;
	}
	
	private JScrollPane getCompoundTable(){
		compoundTable = new MatchingTable();
		return new JScrollPane(compoundTable);
	}
	
	private void openNetworkView(String id, String name) {
		String pwId = id.replace("map", this.organism);	//cannot retrieve xml file with id of the form mapXXXXX
		NetworkAdapter.openNetwork(pwId, name, this.compoundMap, this.matrixCompoundMap, this.matrixGraphPanel);
	}
	
	/**
	 * takes the KEGG response to a .../find/pathway request
	 * which is a list of pathways along with their IDs
	 * an ArrayList<String> is returned where each String contains a pathway along with its ID
	 * @param keggResponse KEGG response to a .../find/pathway request
	 * @return List of pathways with IDs
	 */
	private ArrayList<String> getPathwayArray(String keggResponse) {
		String[] pathwayArray = keggResponse.split("\n");
		ArrayList<String> pathwayList = new ArrayList<>();
		for(String s: pathwayArray) {
			s = s.replace("\t", "     ");
			pathwayList.add(s);
		}
		return pathwayList;
	}
	
	/**
	 * takes the KEGG ID of a pathway and returns its related compounds
	 * as ArrayList<SubstanceWithPathways>
	 * assigns the names of those compounds to EditableList relCompoundsResult to display it to the user
	 * @param pwId KEGG pathway ID
	 * @return list of related compounds
	 */
	protected static String[] getCompoundsFromKegg(String pwId) throws Exception {
		ArrayList<String> substancesStrings = new ArrayList<>();
		
		if(pwId != null){
			RestService restServiceLink = new RestService("https://rest.kegg.jp/" + "link/" + "compound/");
			String response = (String) restServiceLink.makeRequest(pwId, MediaType.TEXT_PLAIN_TYPE, String.class);
			substancesStrings = ExtractFromString.extractCompounds(response);
		}
		return substancesStrings.toArray(new String[0]);
	}
	
	private void addDBCompoundsToTable (String[] compsWithIds) {
		ArrayList<Pair<String, String>> dbCpdList = new ArrayList<>();
		for(String compound: compsWithIds) {
			String keggId = compound.substring(0, 6);
			String name = compound.substring(compound.indexOf("   ") + 3);
			Pair<String, String> pair = new Pair<>(keggId, name);
			dbCpdList.add(pair);
		}
		compoundMap.setSecPositionValues(dbCpdList);
		compoundMap.match();
		compoundTable.setData(compoundMap.toArray());
		compoundTable.sort();
	}
	
	private void addMatrixCompoundsToTable(){
		for(CompoundTextNode cpdNode: pathwayNode.getCompoundTextNodes()) {
			SubstanceWithPathways swp = (SubstanceWithPathways) cpdNode.getSubstance();
			String keggId = null;

			if(swp.getSynonymMap() != null) {
				keggId = swp.getSynonyme(3);
			}

			if(keggId == null) {
				keggId = "";
			}
			String name = swp.getName();
			if(name == null) {
				name = "";
			}
			Pair<String, String> pair = new Pair<>(keggId, name);
			this.matrixCompoundMap.put(pair, cpdNode);
		}

		if(pathwayNode.hasMapping()) {
			compoundMap = new MatchingTableMap(pathwayNode.getMapping());
		}
		else {
			ArrayList<Pair<String, String>> matrixCpdList = new ArrayList<>(matrixCompoundMap.keySet());
			compoundMap.setFirstPositionValues(matrixCpdList);
		}
		compoundTable.setData(compoundMap.toArray());
		compoundTable.sort();
	}
	
	/**
	 * retrieves the entry to the given pathway ID
	 * @param pwId KEGG pathway ID
	 */
	protected static String keggGet(String pwId){
		RestService restServiceLink = new RestService("https://rest.kegg.jp/get/");
		return (String) restServiceLink.makeRequest(pwId, MediaType.TEXT_PLAIN_TYPE, String.class);
	}
	
	private void saveToMatrix(){
		this.matrixPathway.setSuperPathway(this.matrixSuperPw.getText());
		this.matrixPathway.setKeggPathway(this.keggPathway);
		this.pathwayNode.setMapping(compoundMap.getMap());
	}

	/**
     * returns an instance of GridBagConstraints with default values and x and y as position
     * @param x x-position
     * @param y y-position
     * @return GridBagConstraints
     */
    private GridBagConstraints setPosition(int x, int y) {
    	return new GridBagConstraints(x, y, 1, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
    }
}
