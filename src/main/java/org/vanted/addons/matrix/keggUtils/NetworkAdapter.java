package org.vanted.addons.matrix.keggUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.util.Pair;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color.LabelColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

public class NetworkAdapter {
	
	/**
	 * download the specified pathway from KEGG
	 * assigns the substances from the CpdTextNodes to the nodes of the pathway
	 * according to the mapping
	 * @param id KEGG pathway ID
	 * @param name pathway name
	 * @param compoundMap MatchingTableMap
	 * @param matrixCompoundMap Hash map from compound name and ID to compoundTextNode
	 * @param mgPanel reference to the matrix graph panel
	 */
	public static void openNetwork(String id,
								   String name,
								   MatchingTableMap compoundMap,
								   HashMap<Pair<String, String>, CompoundTextNode> matrixCompoundMap,
								   MatrixGraphPanel mgPanel) {
		AdjListGraph graph = downloadPathway(id, name);
		if (graph == null) {
			System.out.println("Download failed.");
			return;
		}
		setAttributesToGraph(graph);
		replaceNodes(graph, compoundMap, matrixCompoundMap, mgPanel);
	}
	
	/**
	 * this method is an adapted version of the method TabKegg.downloadPathway() in the 
	 * package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg
	 * @param id: ko ID or species spcific ID of a Kegg Pathway (IDs that start with "map" connot be processed)
	 * @param name pathway name
	 * @return Graph representation of the given pathway
	 */
	private static AdjListGraph downloadPathway(String id, String name){
		URL url;
		try{
			url = new URL("https://rest.kegg.jp/get/" + id + "/kgml");
		}
		catch(MalformedURLException e) {
			System.out.println("MalformedURLException");
			return null;
		}
		
		final AdjListGraph graph;			
		graph = (AdjListGraph) MainFrame.getInstance().getGraph(name + ".xml", url);
		
		BackgroundTaskHelper.issueSimpleTask("Retrieve Pathway", "Please wait (Download in progress)...",
				() -> {
					try {
						ArrayList<Node> nodeList = (ArrayList<Node>) graph.getNodes();
						HashMap<String, String> keggIDToEntry = new HashMap< >();
						StringBuilder labels = new StringBuilder();
						ArrayList<String> labelList = new ArrayList<>();
						int count = 0;
						for (Node n : nodeList) {
							ArrayList<String> ids = getKeggIDsFromNode(n);
							if (count + ids.size() > 100) {
								count = 0;
								labelList.add(labels.toString());
								labels = new StringBuilder();
							}
							// ignore path, rc, rp
							for (String s : ids)
								if (!s.startsWith("path") && !s.startsWith("rc") && !s.startsWith("rp")
										&& !s.equals("undefined")) {
									labels.append("+").append(s);
									count += ids.size();
								}
						}
						labelList.add(labels.toString());
						labels = new StringBuilder();
						RestService restService = new RestService("https://rest.kegg.jp/" + "list/");
						for (String l : labelList)
							labels.append(restService.makeRequest(l, MediaType.TEXT_PLAIN_TYPE, String.class));
						do {
							String entry;
							String id1 = labels.substring(0, labels.indexOf("\t"));
							String currNode;
							if (labels.toString().contains("\n"))
								currNode = labels.substring(labels.indexOf("\t") + 1, labels.indexOf("\n"))
										.trim();
							else
								currNode = labels.substring(labels.indexOf("\t") + 1).trim();
							if (labels.toString().startsWith("ec") && !currNode.startsWith("Deleted entry")) {
								if (currNode.toLowerCase().startsWith("transferred to ")) {
									currNode = currNode.replace("transferred to ", "");
									currNode = currNode.replace(" and", ";");
								}
							}
							entry = currNode;
							if (entry.length() > 0)
								keggIDToEntry.put(id1, entry);
							if (labels.toString().contains("\n"))
								labels = new StringBuilder(labels.substring(labels.indexOf("\n") + 1));
						} while (labels.toString().contains("\n"));
						for (Node n : nodeList) {
							AttributeHelper.setLabel(1, n, AttributeHelper.getLabel(n, ""), null,
									AlignmentSetting.HIDDEN.toGMLstring());
							ArrayList<String> ids = getKeggIDsFromNode(n);
							for (String s : ids) {
								String entry = keggIDToEntry.get(s);
								// for EC numbers like 1.1.1.- KEGG can't provide information, entry is null
								// some EC numbers have been removed from KEGG ("Deleted entry"), entry is null
								// as well
								if (entry != null) {
									// names are separated by ';'
									// but it can happen that information is provided in parentheses with
									// separation by ';'
									// replace all ';' in '(...; ...)' by ','
									if (entry.contains("(")) {
										int fromIndex = 0;
										int length = entry.length();
										while (fromIndex < length) {
											int idx1 = entry.indexOf("(", fromIndex);
											int idx2 = entry.indexOf(";", fromIndex);
											int idx3 = entry.indexOf(")", fromIndex);
											if (idx2 > idx1 && idx2 < idx3)
												entry = entry.substring(0, idx2) + ","
														+ entry.substring(idx2 + 1);
											if (idx1 != -1 && idx3 != -1)
												fromIndex = idx3 + 1;
											else
												fromIndex = length;
										}
									}
									int k = 2;
									while (entry.contains("; ")) {
										AttributeHelper.setLabel(k, n, entry.substring(0, entry.indexOf("; ")),
												null, AlignmentSetting.HIDDEN.toGMLstring());
										entry = entry.substring(entry.indexOf("; ") + 2);
										k++;
									}
									AttributeHelper.setLabel(k, n, entry, null,
											AlignmentSetting.HIDDEN.toGMLstring());
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						ErrorMsg.addErrorMessage("Could not download KEGG pathway!\n" + e.getMessage());
					}
				}, () -> {
					MainFrame.getInstance().showGraph(graph, null);
					for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes()) {
						try{
							if (KeggGmlHelper.getKeggId(n).startsWith("cpd")) {
								AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));			//use names instead of IDs for compound nodes in network
							}
							else if (!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
								AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));			//use gene names instead of IDs for enzyme nodes in network
						}
						catch(NullPointerException e) {
							//do nothing, nodes without ID get their names as labels automatically
						}
					}
				});
	
	return graph;
	}
	
	private static ArrayList<String> getKeggIDsFromNode(Node node) {

		ArrayList<String> ids = new ArrayList<>();
		String id = KeggGmlHelper.getKeggId(node);
		
		if(id == null) {
			//System.out.println(node.toString());
			return new ArrayList<>();
		}else {
		while (id.trim().contains(" ")) {
			ids.add(id.trim().substring(0, id.indexOf(" ")));
			id = id.trim().substring(id.indexOf(" ") + 1);
		}
		ids.add(id.trim());
		return ids;
		}
	}

	private static void replaceNodes(Graph graph,
									 MatchingTableMap compoundMap,
									 HashMap<Pair<String, String>, CompoundTextNode> matrixCompoundMap,
									 MatrixGraphPanel mgPanel){
				
		List<Node> nodeList = graph.getNodes();
		
		Set<Pair<String, String>> scndPairs = new HashSet<>();
		for(Pair<String, String> scndPair: compoundMap.getSecondValuesWithMatchings()) {		// -> scndPairs contains only those pairs from the matching table
			if(!scndPair.getSnd().isEmpty()) {														//that match to a compound of the matrix
				scndPairs.add(scndPair);
			}
		}

		ArrayList<CompoundTextNode> controlList = new ArrayList<>(matrixCompoundMap.values());
		
		for(Node node: nodeList) {		
			String id = node.toString();
			if(id.contains(", C")) {						// only nodes that refer to compounds
				id = id.substring(id.indexOf(", C") + 2);
				for(Pair<String, String> scndPair: scndPairs) {
					if(scndPair.getFst().equals(id)) {		//search right side compounds from matchingTable for correct compound
						for(Pair<String, String> firstPair: compoundMap.getFirstValues(scndPair)) { //get the matrix compounds matched to it
							CompoundTextNode cpdNode = stringLookUp(matrixCompoundMap, firstPair);
							if(cpdNode != null) {
							
								SubstanceInterface substance = cpdNode.getSubstance();
								
								Experiment2GraphHelper.addMappingData2Node(substance, node);
		
								setAttributesToNode(node);
								
								//LabelAttribute labelAttr2 = new NodeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS + "10",  substance.getName());						
								//AttributeHelper.setAttribute(node, "" , GraphicAttributeConstants.LABELGRAPHICS + "10", labelAttr2);
								//node.addAttribute(labelAttr2, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
							// -> both work but: label is initially visible and centered next to original label. find way to get correct index
							// -> dropped; since it does not seem to be possible to retrieve the number of labels already present i can't add a new label with the correct index 	
								
								transferColorCode(node, cpdNode, mgPanel);
								
								controlList.remove(cpdNode);
							}
						}
					}
				}
			}		
		}
		
		ArrayList<SubstanceInterface> substanceList = new ArrayList<>();
		HashSet<GraphElement> nodeSet = new HashSet<>();
		double x = 50;
		double y = 800;
		
	//add nodes with no mapping to a new node of the KEGG pathway	
		for(CompoundTextNode cpdNode: controlList) {
			Node newNode = graph.addNodeCopy(cpdNode);
			nodeSet.add(newNode);
			setAttributesToNode(newNode);
			transferColorCode(newNode, cpdNode, mgPanel);
			
			AttributeHelper.setPosition(newNode, x, y);

			x = x + 100;	
			if(x >= 1050) {
				x = 50;
				y = 900;		// -> new line after 10 nodes
			}
			substanceList.add(cpdNode.getSubstance());
		}
		
		Experiment exp = new Experiment(substanceList);
		Experiment2GraphHelper help = new Experiment2GraphHelper();
	 		help.mapDataToGraphElements(true, exp, nodeSet, graph,
	 				false, "chart2d_type1", 1, 1, false, false, true);

	}
	
	/**
	 * makes lookup in HashMap<Pair<String, String>> by comparing the Strings
	 * this is necessary because sometimes the references change when the MatchingTable was manipulated by the user 
	 * @param matrixCompoundMap Hash map that is queried
	 * @param key Pair of compound id and name
	 * @return The desired CompoundTextNode or null, if lookup fails
	 */
	private static CompoundTextNode stringLookUp(HashMap<Pair<String, String>, CompoundTextNode> matrixCompoundMap, Pair<String, String> key){
		String id = key.getFst();
		String name = key.getSnd();
		
		for(Pair<String, String> pair: matrixCompoundMap.keySet()) {
			if(id.equals(pair.getFst()) && name.equals(pair.getSnd())) {
				return matrixCompoundMap.get(pair);
			}
			
		}
		return null;
	}
	
	private static void setAttributesToGraph(AdjListGraph graph) {
 	    AttributeHelper.setAttribute(graph, "", "node_showCategoryAxis", true);					// timePoints are displayed under x-Axes
 		AttributeHelper.setAttribute(graph, "", "node_showRangeAxis", true); 					// y-axes are tagged with numerical values and unit (eg fold change...)
 		AttributeHelper.setAttribute(graph, "", "node_showGridRange", true);					// lines are also drawn for y-axes
 		AttributeHelper.setAttribute(graph, "", "node_plotAxisFontSize", 10);	// font for axis labels
 		AttributeHelper.setAttribute(graph, "", "node_plotAxisSteps", 3d);		// gap between labels of x-axes
 		
 		AttributeHelper.setAttribute(graph, "", "node_useCustomRangeSteps", true);
 		AttributeHelper.setAttribute(graph, "", "node_customRangeSteps", 50d);
 		
		LabelColorAttribute axisColorAtt = new LabelColorAttribute(GraphicAttributeConstants.AXISCOLOR);
		axisColorAtt.setColor(Color.BLACK);
 		AttributeHelper.setAttribute(graph, "", "axis_color", axisColorAtt);						// color for axes = black -> better contrast		
	
		LabelColorAttribute gridColorAtt = new LabelColorAttribute(GraphicAttributeConstants.GRIDCOLOR);
		gridColorAtt.setColor(Color.DARK_GRAY);
		AttributeHelper.setAttribute(graph, "", "grid_color", gridColorAtt);		
	}

	/**
	 * sets attributes to a node
	 * these will become the default settings for the chart
	 * @param n Node for which attributes are set
	 */
	private static void setAttributesToNode(Node n) {
		AttributeHelper.setAttribute(n, "charting", "useCustomRangeSteps", true);				
		AttributeHelper.setAttribute(n, "charting", "rangeStepSize", 50d);					//gap between labels of y-axis
		AttributeHelper.setAttribute(n, "charting", "rangeAxis", "");						//empty String -> no label is created -> more space for chart

		DimensionAttribute dimAttr = new DimensionAttribute(GraphicAttributeConstants.DIMENSION);
		dimAttr.setDimension(new Dimension(50, 50));
		AttributeHelper.setAttribute(n, "graphics", "dimension", dimAttr);					// sets size of the node so that chart is visible
		
		NodeGraphicAttribute nga = (NodeGraphicAttribute) n.getAttribute(NodeGraphicAttribute.GRAPHICS);
		nga.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);						// gives node a rectangular shape

	}
	
	private static void transferColorCode(Node n, CompoundTextNode cpdNode,  MatrixGraphPanel mgPanel) {
		ColorAttribute fillAttr = new ColorAttribute(GraphicAttributeConstants.FILLCOLOR);
		Color color = mgPanel.getColorMapping(cpdNode);
		fillAttr.setColor(color);
		AttributeHelper.setAttribute(n, "graphics", "fill", fillAttr);
	}
}
