package org.vanted.addons.matrix.pinBoard;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;
import org.AttributeHelper;
import org.apache.batik.ext.swing.GridBagConstants;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.graffiti.graph.*;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;
import org.vanted.addons.matrix.ui.MatrixLabelButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color.LabelColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.*;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

import javax.swing.JFrame;

public class PinBoardPanel extends JScrollPane {
	private static final long serialVersionUID = -7118256068888830654L;
	private final BidiMap<CompoundTextNode, JPanel> nodeToChartMap = new DualHashBidiMap<>();
	private BidiMap<ArrayList<CompoundTextNode>, JPanel> mergedChartPanels = new DualHashBidiMap<>();
	private final ArrayList<JComboBox<String>> pathwayBoxes = new ArrayList<>();
	private ArrayList<JPanel> selectedChartPanels = new ArrayList<>();
	private final Color[] colorsForMergedCharts = {
			new Color(0x000000),
			new Color(0xe6194b),
			new Color(0x3cb44b),
			new Color(0xffe119),
			new Color(0x4363d8),
			new Color(0xf58231),
			new Color(0x911eb4),
			new Color(0xf032e6),
			new Color(0xbfef45),
			new Color(0xfabed4),
			new Color(0x469990),
			new Color(0x9A6324),
			new Color(0x800000),
			new Color(0xaaffc3),
			new Color(0x808000),
			new Color(0x000075),
	};
	
	private final JPanel content = new JPanel();
	private final JPanel toolbar;
	private boolean showAdditionalInfo = false;
	private boolean showStdDev = true;
	private boolean useCustomChartRange = false;
	private double customChartRangeMin = 0;
	private double customChartRangeMax = 300;
	private final double autoStepRange;
	private double customStepRange = 1.0;
	private String chartType = "chart2d_type1";
	private Dimension panelPref = new Dimension(300, 120);
	private Dimension chartPref = new Dimension(200, 100);
	private final Dimension addInfoPref = new Dimension(100, 50);
	private final PinBoardTab tab;
	private boolean detached;
	private JFrame frame;
	private MatrixGraphPanel matrixPanel;

	public PinBoardPanel(ArrayList<CompoundTextNode> selectedNodes, MatrixGraphPanel mgp, PinBoardTab pbTab) {
		super();
		this.detached = false;
		this.tab = pbTab;
		this.matrixPanel = mgp;
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		this.setViewportView(content);
		this.getVerticalScrollBar().setUnitIncrement(16);
		this.getHorizontalScrollBar().setUnitIncrement(16);
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		this.toolbar = this.buildToolbar();
		toolbar.setPreferredSize(new Dimension(300, 60));
		this.setColumnHeaderView(this.toolbar);
		double maxValue = Double.NEGATIVE_INFINITY;
		for(CompoundTextNode n: selectedNodes) {
			doMapping(n);
			maxValue = Math.max(maxValue, n.getSubstance().getAverage());
		}
		this.autoStepRange = (double) (Math.round(maxValue*10))/50;
		for(Entry<CompoundTextNode, JPanel> entry: nodeToChartMap.entrySet()) {
			content.add(entry.getValue());
		}
		//calls resize() which adjusts sizes of JPanel and JFrame
		content.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
					resize();
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}
 	
	/**
	 * returns the toolbar for this pin board as a JPanel
	 * @return The toolbar for the pin board
	 */
	private JPanel buildToolbar() {
		JPanel toolpanel = new JPanel(new GridBagLayout());
		JCheckBox additionalInfo = new JCheckBox("show Infos");
		additionalInfo.setSelected(false);
		additionalInfo.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				showAdditionalInfo = false;
				for(JComboBox<String> info: pathwayBoxes)
					info.setVisible(false);
			}
			else if(e.getStateChange() == ItemEvent.SELECTED) {
				showAdditionalInfo = true;
				for(JComboBox<String> info: pathwayBoxes)
					info.setVisible(true);
			}
			content.updateUI();
		});

		additionalInfo.setToolTipText("show additional Infos");
		toolpanel.add(additionalInfo, this.setPosition(0, 0, 2, 1));
				
		JCheckBox stdDev = new JCheckBox("show stdDev");
		stdDev.setSelected(true);
		stdDev.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				showStdDev = false;
				rebuild();
			}
			else if(e.getStateChange() == ItemEvent.SELECTED) {
				showStdDev = true;
				rebuild();
			}
			content.updateUI();
		});
		stdDev.setToolTipText("show standard deviation");
		toolpanel.add(stdDev, this.setPosition(2, 0, 2, 1));
				
		String[] chartTypes = {"Line chart", "Bar chart (3D)", "Bar chart (flat)", "Pie chart", "No chart"};
		JComboBox<String> selectChartType = new JComboBox<> (chartTypes);
		selectChartType.addActionListener(e -> {
//        JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String ct = (String) selectChartType.getSelectedItem();
			if (ct == null){
				this.chartType = this.chartTypeTranslator("Line chart");
			} else {
				this.chartType = this.chartTypeTranslator(ct);
			}
			this.rebuild();
		});
		toolpanel.add(selectChartType, this.setPosition(0, 1, 2, 1));
				
		String[] sizes = {"large", "medium", "small"};
		JComboBox<String> selectSize = new JComboBox<> (sizes);
		selectSize.setSelectedIndex(1);
		selectSize.addActionListener(e -> {
//			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String s = (String) selectSize.getSelectedItem();
			if (s != null)
				this.setSizes(s);
			else
				this.setSizes("medium");
			this.doResizing();
			if(this.detached)
				this.frame.pack();
		});
		toolpanel.add(selectSize, this.setPosition(2, 1, 2, 1));
				
		JButton de_attach = new JButton("de-/attach");
		de_attach.addActionListener(l -> this.detach_attach());
		de_attach.setToolTipText("de-/attach");
		toolpanel.add(de_attach, this.setPosition(4, 0, 2, 1));
			
		JButton merge = new JButton("merge selection");
		merge.addActionListener(l -> {
			if(this.selectedChartPanels.size() > 1) {
				this.mergeSelectedCharts();
				this.updateUI();
			}
		});
		merge.setToolTipText("show selected charts in one chart");
		toolpanel.add(merge, this.setPosition(5, 2, 3, 1));
		
		JButton de_selectAll = new JButton("select all");
		de_selectAll.addActionListener(l -> {
			if(selectedChartPanels.size() != this.nodeToChartMap.values().size())
				this.selectedChartPanels.clear();
			for(JPanel singleChartPanel: this.nodeToChartMap.values())
				this.select(singleChartPanel);
		});
		de_selectAll.setToolTipText("select all");
		toolpanel.add(de_selectAll, this.setPosition(4, 1, 2, 1));
		
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		NumberFormatter nformatter = new NumberFormatter(numberFormat);
		nformatter.setValueClass(Double.class);
		nformatter.setAllowsInvalid(true);			// -> else user cant delete last digit, since "" would be an invalid input
		nformatter.setCommitsOnValidEdit(false);	// committ value on each keystroke instead of focus lost
		
		JFormattedTextField enterChartRangeMin = new JFormattedTextField(nformatter);
		enterChartRangeMin.setValue(this.customChartRangeMin);
		enterChartRangeMin.setColumns(4);
		enterChartRangeMin.addPropertyChangeListener(l ->{
			JFormattedTextField textField = (JFormattedTextField) l.getSource();
			this.customChartRangeMin = (double) textField.getValue();
			rebuild();
		});
		enterChartRangeMin.setToolTipText("chart range minimum");
		toolpanel.add(enterChartRangeMin, this.setPosition(2, 2, 1, 1));
		
		JFormattedTextField enterChartRangeMax = new JFormattedTextField(nformatter);
		enterChartRangeMax.setValue(this.customChartRangeMax);
		enterChartRangeMax.setColumns(4);
		enterChartRangeMax.addPropertyChangeListener(l ->{
			JFormattedTextField textField = (JFormattedTextField) l.getSource();
			this.customChartRangeMax = (double) textField.getValue();
			rebuild();
		});
		enterChartRangeMax.setToolTipText("chart range maximum");
		toolpanel.add(enterChartRangeMax, this.setPosition(3, 2, 1, 1));
		
		JCheckBox chartRange = new JCheckBox("custom chart range");
		chartRange.setSelected(false);
		chartRange.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				useCustomChartRange = false;
				rebuild();
			}
			else if(e.getStateChange() == ItemEvent.SELECTED) {
				useCustomChartRange = true;
				rebuild();
			}
			content.updateUI();
		});
		chartRange.setToolTipText("use custom chart range");
		toolpanel.add(chartRange, this.setPosition(0, 2, 2, 1));
		
		JFormattedTextField enterChartRangeStepSize = new JFormattedTextField(nformatter);
		enterChartRangeStepSize.setValue(this.customStepRange);
		enterChartRangeStepSize.setColumns(4);
		enterChartRangeStepSize.addPropertyChangeListener(l ->{
			JFormattedTextField textField = (JFormattedTextField) l.getSource();
			this.customStepRange = (double) textField.getValue();
			rebuild();
		});
		enterChartRangeStepSize.setToolTipText("chart range step size");
		toolpanel.add(enterChartRangeStepSize, this.setPosition(4, 2, 1, 1));

		JButton close = new JButton("X");
			close.addActionListener(l -> this.close());
		close.setBackground(Color.RED);
		GridBagConstraints closeGBC = this.setPosition(7, 0, 1, 1);
		closeGBC.fill = GridBagConstants.NONE;
		close.setToolTipText("close");
		toolpanel.add(close, closeGBC);
			
		return toolpanel;
	}

	/**
	 * sets sizes to chart Panels and charts separately
	 * since automatic resizing by the layout did not achieve the desired result
	 */
	private void doResizing() {
		for(JPanel chartPanel: this.mergedChartPanels.values()) {
			 chartPanel.setPreferredSize(panelPref);
			 chartPanel.setMinimumSize(panelPref);
			 chartPanel.setMaximumSize(panelPref);
			 for(Component c: chartPanel.getComponents()) {
				 if(c instanceof XmlDataChartComponent) {
					c.setPreferredSize(chartPref);
				    c.setMinimumSize(chartPref);
				    c.setMaximumSize(chartPref); 
				 }
			 }
		}
		
		for(JPanel chartPanel: nodeToChartMap.values()) {
			 chartPanel.setPreferredSize(panelPref);
			 chartPanel.setMinimumSize(panelPref);
			 chartPanel.setMaximumSize(panelPref);
			 for(Component c: chartPanel.getComponents()) {
				 if(c instanceof XmlDataChartComponent) {
					c.setPreferredSize(chartPref);
				    c.setMinimumSize(chartPref);
				    c.setMaximumSize(chartPref); 
				 }
			 }
		}
		content.revalidate();
	}

	/**
	 * translates the string selected by the user to the respective size settings
	 * for chart and chartPanel
	 * @param s size
	 */
	private void setSizes(String s) {
		switch (s) {
		case "large":
			toolbar.setPreferredSize(new Dimension(400, 60));
			toolbar.revalidate();
			this.panelPref = new Dimension(400, 170);
			this.chartPref = new Dimension(300, 150);
			return;
		case "medium":
			toolbar.setPreferredSize(new Dimension(300, 60));
			toolbar.revalidate();
			this.panelPref = new Dimension(300, 120);
			this.chartPref = new Dimension(200, 100);
			return;
		case "small":
			toolbar.setPreferredSize(new Dimension(250, 60));
			toolbar.revalidate();
			this.panelPref = new Dimension(250, 95);
			this.chartPref = new Dimension(150, 75);
		}
	}

	/**
	 * builds chart panel, maps it to their CompoundTextNode
	 * and returns it
	 * @param n CompoundTextNode
	 */
	private JPanel doMapping(CompoundTextNode n) {
		JPanel chartPanel = createChartPanel(n);
		this.nodeToChartMap.put(n,  chartPanel);
		return chartPanel;
	}
	
	/**
	 * returns the XmlDataChartComponent to the given CompoundTextNode
	 * @param n CompoundTextNode
	 * @return XmlDataChartComponent
	 */
	private XmlDataChartComponent buildChart(CompoundTextNode n) {
		AdjListGraph graph = new AdjListGraph();
		graph.addNodeCopy(n);
		
    	this.setAttributesToGraph(graph);
    	Experiment exp = new Experiment(n.getSubstance());
    	
  	    HashSet<GraphElement> nodeSet = new HashSet<>();
  	    AdjListNode alNode = (AdjListNode) graph.getNodes().get(0);//(GraphElement) graph.getNodes().get(0);
  	    nodeSet.add(alNode);
  	    
 		Experiment2GraphHelper help = new Experiment2GraphHelper();
 		help.mapDataToGraphElements(true, exp, nodeSet, graph,
 				false, "chart2d_type1", 1, 1, false, false, true);
 		
		setAttributesToNode(alNode);
		XmlDataChartComponent xdcc = new XmlDataChartComponent(this.chartType, graph, alNode);
	    xdcc.setPreferredSize(chartPref);
	    xdcc.setMinimumSize(chartPref);
	    xdcc.setMaximumSize(chartPref);
	    return xdcc;
	}
		
	private void select(JPanel chartPanel){
		if(selectedChartPanels.contains(chartPanel)) {
			 chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			 selectedChartPanels.remove(chartPanel);
		 }
		 else {
			 Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
			 chartPanel.setBorder(border);
			 selectedChartPanels.add(chartPanel);
		 }
	}
		
	/**
	 * all chartPanels are removed (from contentPanel and nodeToChartMap) and then 
	 * rebuilt with their current settings
	 * and added to contentPanel and nodeToChartMap again
	 */
	private void rebuild() {
		content.removeAll();
		Set<CompoundTextNode> nodeSet = new HashSet<>(this.nodeToChartMap.keySet());
		ArrayList<JPanel> newSelectionList = new ArrayList<>();
		
		//rebuild single chart Panels
		for(CompoundTextNode node: nodeSet) {
			JPanel oldChartPanel = nodeToChartMap.get(node);
			boolean visible = oldChartPanel.isVisible();
			boolean selected = this.selectedChartPanels.contains(oldChartPanel);
			nodeToChartMap.remove(node);
			doMapping(node);
			JPanel newChartPanel = nodeToChartMap.get(node);
			newChartPanel.setVisible(visible);
			if(selected) {
				newSelectionList.add(newChartPanel);
				newChartPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
			}
		}
		
		for(Entry<CompoundTextNode, JPanel> entry: nodeToChartMap.entrySet())
			content.add(entry.getValue());
		this.selectedChartPanels = newSelectionList;
	
		//rebuild single chart Panels
		BidiMap<ArrayList<CompoundTextNode>, JPanel> newMergedPanels = new DualHashBidiMap<>();
		for(Entry <ArrayList<CompoundTextNode>, JPanel> entry: this.mergedChartPanels.entrySet()) {
			JPanel mergedChartPanel = this.createMergedChartPanel(entry.getKey());
			newMergedPanels.put(entry.getKey(), mergedChartPanel);
			content.add(mergedChartPanel, 0);
		}
		this.mergedChartPanels = newMergedPanels;
		this.updateUI();
	}

	/**
	 * first checks whether the according node and chartPanel already exist
	 * if necessary creates chartPanel for the given button
	 * adds it to content panel and maps the resp node to the chartPanel
	 * @param button MatrixLabelButton
	 */
	public void addChart(MatrixLabelButton button) {
		CompoundTextNode node = (CompoundTextNode) button.getLabelNode();
		if(nodeToChartMap.containsKey(node))
			nodeToChartMap.get(node).setVisible(true);
		else
			content.add(this.doMapping(node), 0);  //adds new chart panel at the top of content panel
		content.updateUI();
	}
	
	/**
	 * removes this panel from the PinBoardTab and puts it in a new JFrame
	 * or vice versa
	 */
	private void detach_attach() {
		if(this.detached) {
			frame.remove(this);
			frame.setVisible(false);
			this.tab.add(this);
			tab.setVisible(true);
			tab.repaint();
		}
		else {
			this.tab.remove(this);
			tab.setVisible(false);
			tab.revalidate();
			tab.repaint();
			frame = new JFrame();
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {}

			    public void windowClosing(WindowEvent e) {
			        close();
			    }
			});
			frame.add(this);
			frame.pack();
			frame.setVisible(true);
		}
		detached = !detached;
	}


	/**
	 * assembles the components for a chart panel
	 * that contains a label, XmlDataChartComponent and labels for additional infos
	 * @param n CompoundTextNode
	 * @return a chart panel
	 */
	private JPanel createChartPanel(CompoundTextNode n) {
		JPanel chartPanel = new JPanel(new GridBagLayout());
 		
 		//add title = substance name
		JLabel substanceName = new JLabel(n.getSubstance().getName());
		Font font = new Font(null, Font.BOLD, 16);
		substanceName.setFont(font);
		GridBagConstraints substanceNameGBC = this.setPosition(0, 0, 1, 1);
		substanceNameGBC.weightx = 0.95;
		substanceNameGBC.weighty = 0;
		chartPanel.add(substanceName, substanceNameGBC);
		
		//options	
		JButton options = new JButton(">");
		options.addActionListener(l -> {
			JButton button = (JButton) l.getSource();
			PopupMenu popupmenu = singleChartsMenu(n, chartPanel);
			button.add(popupmenu);
			popupmenu.show(button, button.getWidth()/2, button.getHeight()/2);
		});
		 		
		GridBagConstraints optionsGBC = this.setPosition(1, 0, 1, 1);
		optionsGBC.weightx = 0.05;
		optionsGBC.weighty = 0;
		chartPanel.add(options, optionsGBC);
			
		//add additional info
		ArrayList<String> pwArrayList = new ArrayList<>();

		for(Pathway p: ((SubstanceWithPathways) n.getSubstance()).getPathways()) {
			JLabel pwLabel = new JLabel(p.getTitle());
			pwLabel.setToolTipText(pwLabel.getText());
			pwLabel.setPreferredSize(addInfoPref);
			pwLabel.setMaximumSize(addInfoPref);
			pwLabel.setMinimumSize(addInfoPref);
			pwArrayList.add(p.getTitle());
		}
		 		
		String[] pwArray = new String[pwArrayList.size()];
		for(int i = 0; i < pwArrayList.size(); i++)
			pwArray[i] =  pwArrayList.get(i);
		 		
		JComboBox<String> pathwayBox = new JComboBox<>(pwArray);
		this.pathwayBoxes.add(pathwayBox);
		if(!showAdditionalInfo)
			pathwayBox.setVisible(false);
		pathwayBox.setPreferredSize(addInfoPref);
		pathwayBox.setMaximumSize(addInfoPref);
		pathwayBox.setMinimumSize(addInfoPref);
		GridBagConstraints addInfoGbc = setPosition(1, 1, 1, 1);
		addInfoGbc.weightx = 0.25;
		chartPanel.add(pathwayBox, addInfoGbc);
		 		
		 //add chart		
		XmlDataChartComponent chart = this.buildChart(n);
		GridBagConstraints chartGbc = this.setPosition(0, 1, 1, 1); //GridBagConstants.REMAINDER );
		chartGbc.fill = GridBagConstants.BOTH;
		chartGbc.weightx = 1;
		chartGbc.weighty = 1;
		chartPanel.add(chart, chartGbc);
	 		
		//add mouse listener
		chartPanel.addMouseListener(this.chartPanelMouseListener());

		//add border and size
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chartPanel.setPreferredSize(panelPref);
		chartPanel.setMinimumSize(panelPref);
		chartPanel.setMaximumSize(panelPref);
		return chartPanel;
}

	private MouseAdapter chartPanelMouseListener(){
		return new MouseAdapter() {
			 public void mouseClicked(MouseEvent e) {
				JPanel chartPanel = (JPanel) e.getSource();
				 if (e.getButton() == 3) { 	
					 chartPanel.setVisible(false);
					 resize();	//bc setVisible(false) doesn't seem to fire a component event
				 }
				 else
					 select(chartPanel);
			 }
		 };
	}

	private PopupMenu singleChartsMenu(CompoundTextNode cpdTNode, JPanel chartPanel){
		PopupMenu popUpMenu = new PopupMenu();

		// bigger window
		MenuItem moveToNewFrame = new MenuItem("extra window");
		moveToNewFrame.addActionListener(l -> this.moveCahrtToNewFrame(this.createChartPanel(cpdTNode)));
		popUpMenu.add(moveToNewFrame);

		//export
		Menu exportAs = new Menu("export as >");
		MenuItem jpeg = new MenuItem("JPEG");
		jpeg.addActionListener(e -> exportChart(chartPanel, "jpg"));
		exportAs.add(jpeg);
		MenuItem png = new MenuItem("PNG");
		png.addActionListener(e -> exportChart(chartPanel, "png"));
		exportAs.add(png);
        popUpMenu.add(exportAs);
	    return popUpMenu;
	}

	// methods for merging charts
	private JPanel createMergedChartPanel(ArrayList<CompoundTextNode> cpdTNodeList) {
 		JPanel chartPanel = new JPanel(new GridBagLayout());
 		
 		JTextField title = new JTextField();
 		title.setBackground(null);
 		title.setBorder(null);
 		Font font = new Font(null, Font.BOLD, 16);
 		title.setFont(font);
 		chartPanel.add(title, setPosition(0, 0, 1, 1));
 		title.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				title.setBackground(Color.WHITE);
			}
			public void focusLost(FocusEvent e) {
				title.setBackground(null);	
			}
 		});
 		
 		JButton options = new JButton(">");
 		options.addActionListener(l -> {
 			JButton button = (JButton) l.getSource();
 			PopupMenu popupmenu = mergedChartsMenu(cpdTNodeList, chartPanel, title);
 			button.add(popupmenu);
 			popupmenu.show(button, button.getWidth()/2, button.getHeight()/2);
 		});
 		
 		GridBagConstraints optionsGBC = this.setPosition(1, 0, 1, 1);
 		optionsGBC.weightx = 0.05;
 		chartPanel.add(options, optionsGBC);
	
		GridBagConstraints chartGbc = this.setPosition(0, 1, GridBagConstants.REMAINDER, GridBagConstants.REMAINDER );
		chartGbc.weightx = 0.75;
		chartGbc.fill = GridBagConstants.BOTH;
 		chartPanel.add(this.buildMergedChart(cpdTNodeList), chartGbc);
 		
	 	//add mouse listener
		chartPanel.addMouseListener(this.mergedChartPanelMouseListener());
		 
	 	//add border and size
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chartPanel.setPreferredSize(panelPref);
		chartPanel.setMinimumSize(panelPref);
		chartPanel.setMaximumSize(panelPref);

		return chartPanel;
}

	private XmlDataChartComponent buildMergedChart(ArrayList<CompoundTextNode> cpdTNodeList) {
		ArrayList<SubstanceInterface> substList = new ArrayList<>();
 		for(CompoundTextNode cpdTNode: cpdTNodeList)
 			substList.add(cpdTNode.getSubstance());
	 		
		//add chart
		AdjListGraph graph = new AdjListGraph();
		graph.addNode();
		AdjListNode alNode = (AdjListNode) graph.getNodes().get(0);
		AttributeHelper.setPosition(alNode, 1, 1);  // this is to prevent an exception in Experiment2GraphHelper.mapDataToGraphElements(...)
		
		Substance dummySubstance = new Substance();
		dummySubstance.setName("dummySubstance");
		for(SubstanceInterface substInt: substList) {
			Substance subst = (Substance) substInt;
			for(Object object: subst.toArray()) {
				Condition dummyCondition = (Condition) object;
				dummyCondition.setGenotype(subst.getName());
				dummyCondition.setSpecies(" ");		// the species appears in the legend description as "unspecified"
				dummySubstance.add(dummyCondition);
			}
		}
		
		AttributeHelper.setAttribute(alNode, "charting", "show_legend", Boolean.TRUE);
		
		Experiment2GraphHelper.addMappingData2Node(dummySubstance, alNode);
	
		this.setAttributesToGraph(graph);
		
		ChartColorAttribute chartColorAtt = new ChartColorAttribute("chart_colors");
		chartColorAtt.ensureMinimumColorSelection(16);	// builds an ArrayList of colors, with the given int as size 
		for(int i = 0; i < 16; i++) {					// defines the colors for series up to ten elements
			chartColorAtt.setSeriesColor(i, this.colorsForMergedCharts[i]);
		}
		
		AttributeHelper.setAttribute(graph, "", "chart_colors", chartColorAtt); 

		Experiment exp = new Experiment(dummySubstance);
		
	    HashSet<GraphElement> nodeSet = new HashSet<>();
	    nodeSet.add(alNode);
		
	    Experiment2GraphHelper help = new Experiment2GraphHelper();
		help.mapDataToGraphElements(true, exp, nodeSet, graph,
				false, "chart2d_type1", 1, 1, false, false, true);
		
		setAttributesToNode(alNode);
		XmlDataChartComponent xdcc = new XmlDataChartComponent(this.chartType, graph, alNode);
	    xdcc.setPreferredSize(chartPref);
	    xdcc.setMinimumSize(chartPref);
	    xdcc.setMaximumSize(chartPref);
	    
	    return xdcc;
	}

	private MouseAdapter mergedChartPanelMouseListener(){
		return new MouseAdapter() {
			 
			 public void mouseClicked(MouseEvent e) {
				JPanel chartPanel = (JPanel) e.getSource();
				 if (e.getButton() == 3) { 	
					 content.remove(chartPanel);
					 mergedChartPanels.remove(mergedChartPanels.getKey(chartPanel), chartPanel);
					 resize();	//bc setVisible(false) doesn't seem to fire a component event
				 }
				 else {
					 if(selectedChartPanels.contains(chartPanel)) {
						 chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						 selectedChartPanels.remove(chartPanel);
					 }
					 else {
						 Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
						 chartPanel.setBorder(border);
						 selectedChartPanels.add(chartPanel);

					 }
				 }
			 }
		 };
	}

	/**
	 * creates a Panel with a chart in which the data from the currently selected charts are merged together in on chart
	 * maps the new panel to the respective CompoundTextNodes and adds it to the content Panel
	 */
	private void mergeSelectedCharts(){
		ArrayList<CompoundTextNode> cpdTNodeList = new ArrayList<>();
		
		for(JPanel chartPanel: this.selectedChartPanels) {
			cpdTNodeList.add(this.nodeToChartMap.getKey(chartPanel));
		}
		
		JPanel mergedChartPanel = this.createMergedChartPanel(cpdTNodeList);
		
		this.mergedChartPanels.put(cpdTNodeList, mergedChartPanel);
		content.add(mergedChartPanel, 0);
	}

	private PopupMenu mergedChartsMenu(ArrayList<CompoundTextNode> cpdTNodeList, JPanel mChartPanel, JTextField currentTitle){
		PopupMenu popUpMenu = new PopupMenu();
		ArrayList<CompoundTextNode> editCpdTNodeList = new ArrayList<>(cpdTNodeList);

	 	//remove substances from chart
		Menu substances = new Menu("remove substances >");
		for(CompoundTextNode cpdTNode: cpdTNodeList) {
			MenuItem substance = new MenuItem(cpdTNode.getSubstance().getName());
			substance.addActionListener(e -> {
				content.remove(mChartPanel);
				mergedChartPanels.remove(mergedChartPanels.getKey(mChartPanel), mChartPanel);
				if(cpdTNodeList.size() > 2) {
					editCpdTNodeList.remove(cpdTNode);
					content.add(createMergedChartPanel(editCpdTNodeList), 0);
				}
				content.updateUI();
			});
			substances.add(substance);
		}
		popUpMenu.add(substances);
		 
		//change title
		MenuItem changeTitle = new MenuItem("change Title");
		changeTitle.addActionListener(l -> currentTitle.grabFocus());
		popUpMenu.add(changeTitle);
	
		// bigger window
		MenuItem moveToNewFrame = new MenuItem("extra window");
		moveToNewFrame.addActionListener(l -> this.moveCahrtToNewFrame(this.createMergedChartPanel(editCpdTNodeList)));
		popUpMenu.add(moveToNewFrame);

		//export
		Menu exportAs = new Menu("export as >");
		MenuItem jpeg = new MenuItem("JPEG");
		jpeg.addActionListener(e -> exportChart(mChartPanel, "jpg"));
		exportAs.add(jpeg);
		MenuItem png = new MenuItem("PNG");
		png.addActionListener(e -> exportChart(mChartPanel, "png"));
		exportAs.add(png);
		popUpMenu.add(exportAs);
         
		return popUpMenu;
	}

//auxiliary methods
	/**
	 * returns the string used by XmlDataChartComponent to build the desired chart type
	 * @param type chart type: Line chart, Bar chart (3D), Bar chart (flat), Pie chart
	 * @return xml key word for the chart type
	 */
	private String chartTypeTranslator(String type) {
		switch (type) {
		case "Line chart":
			return "chart2d_type1";
		case "Bar chart (3D)":
			return "chart2d_type2";
		case "Bar chart (flat)":
			return "chart2d_type3";
		case "Pie chart":
			return "chart2d_type4";
		default:
			return "hidden";
		}
	}

	private void exportChart(JPanel chartPanel, String filetype){
		String path = "";
		String filename = "image";
		
	// leave out button for the image
		JButton button = null;
			for(Component c: chartPanel.getComponents()){
				if(c instanceof JButton) {
					button = (JButton) c;
					button.setVisible(false);					
				}else if(c instanceof JLabel) {
					filename = ((JLabel) c).getText();
				}else if(c instanceof JTextField) {
					filename = ((JTextField) c).getText();
				}
			}
		chartPanel.updateUI();
	
		// user dialog to choose directory
		JFrame directoryDialog = new JFrame();
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("choose directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// disable the "All files" option.
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		  path = chooser.getSelectedFile().toString();
		else
		  System.out.println("No Selection ");
		directoryDialog.add(chooser);

		
		//create image
		BufferedImage image = new BufferedImage(chartPanel.getWidth(), chartPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        chartPanel.printAll(g);
        g.dispose();
        File file = new File(path + "\\" + filename + "." + filetype);
        
    	//save image
        try {
            ImageIO.write(image, filetype, file);
        } catch (IOException exp) {
            exp.printStackTrace();
        }
		if (button != null)
        	button.setVisible(true);
		chartPanel.updateUI();
	}

	private void moveCahrtToNewFrame(JPanel chartPanel){
		JFrame frame = new JFrame();
		chartPanel.removeMouseListener(chartPanel.getMouseListeners()[0]);
		
			 chartPanel.setPreferredSize(null);
			 chartPanel.setMinimumSize(null);
			 chartPanel.setMaximumSize(null);
			 for(Component c: chartPanel.getComponents()) {
				 if(c instanceof XmlDataChartComponent) {
					c.setPreferredSize(null);
				    c.setMinimumSize(null);
				    c.setMaximumSize(null); 
				 }
			 }
		chartPanel.updateUI();
		frame.add(chartPanel);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * sets attributes to a graph 
	 * these will become the default settings for the charts
	 * @param graph AdjListGraph
	 */
	private void setAttributesToGraph(AdjListGraph graph) {
		// timePoints are displayed under x-Axes
 	    AttributeHelper.setAttribute(graph, "", "node_showCategoryAxis", true);
		// y-axes are tagged with numerical values and unit (eg fold change...)
 		AttributeHelper.setAttribute(graph, "", "node_showRangeAxis", true);
		// lines are also drawn for y-axes
 		AttributeHelper.setAttribute(graph, "", "node_showGridRange", true);
		// font for axis labels
 		AttributeHelper.setAttribute(graph, "", "node_plotAxisFontSize", 20);
		// gap between labels of x-axes
 		AttributeHelper.setAttribute(graph, "", "node_plotAxisSteps", 5d);
		// thickness of graph line
 		AttributeHelper.setAttribute(graph, "", "node_outlineBorderWidth", 2d);
 		AttributeHelper.setAttribute(graph, "", "node_useCustomRangeSteps", true);
 		AttributeHelper.setAttribute(graph, "", "node_customRangeSteps", 50d);
		LabelColorAttribute colorAtt = new LabelColorAttribute(GraphicAttributeConstants.AXISCOLOR);
		colorAtt.setColor(Color.BLACK);
		// color for axes = black -> better contrast
 		AttributeHelper.setAttribute(graph, "", "axis_color", colorAtt);
		// show/hide StdDev
 		AttributeHelper.setAttribute(graph, "", "node_lineChartShowStdDevRangeLine", this.showStdDev);
	}

	/**
	 * sets attributes to a node
	 * these will become the default settings for the chart
	 * @param n Node to which the attributes are set to
	 */
	private void setAttributesToNode(Node n) {
		AttributeHelper.setAttribute(n, "charting", "useCustomRangeSteps", true);				
		AttributeHelper.setAttribute(n, "charting", "rangeAxis", "");						//empty String -> no label is created -> more space for chart
	
		if(this.useCustomChartRange) {
			AttributeHelper.setAttribute(n, "charting", "rangeStepSize", customStepRange);					//gap between labels of y-axis
		} else {
			AttributeHelper.setAttribute(n, "charting", "rangeStepSize", autoStepRange);					//gap between labels of y-axis

		}
		AttributeHelper.setAttribute(n, "charting", "useCustomRange", useCustomChartRange);
		AttributeHelper.setAttribute(n, "charting", "minRange", this.customChartRangeMin);
		AttributeHelper.setAttribute(n, "charting", "maxRange", this.customChartRangeMax);
	}

	public void setMatrixPanel(MatrixGraphPanel mgp) {
		this.matrixPanel = mgp;
	}

	/**
     * returns an instance of GridBagConstraints with default values and x and y as position
     * and size constraints
     * @param x x position
     * @param y y position
     * @param width: number of columns
     * @param height: number of rows
     * @return instance of GridBagConstraints
     */
    private GridBagConstraints setPosition(int x, int y, int width, int height) {
    	return new GridBagConstraints(x, y, width, height, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
    }

    /**
     * resizes this.getParent() according to content 
     * also invokes pack() on the JFrame to make it resize as well 
     * mainly invoked by content's componentListener
     */
    private void resize(){
    	if(this.detached) {
    		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();					//get size of screen
    	    	
	    	this.frame.setPreferredSize(null);													//without this invocation the frame doesnt resize properly once a pref size is set
	    	this.frame.pack();
	    	this.frame.repaint();
	    	this.frame.revalidate();
	    	
			if(this.frame.getHeight() > screen.height-30) {
				int width = this.panelPref.width + this.getVerticalScrollBar().getWidth();
				int height = screen.height-30;
				this.frame.setPreferredSize(new Dimension(width, height));
				this.frame.pack();
				this.frame.repaint();
				this.frame.revalidate();
			}
    	}
    }  

    /**
     * calls close and/or dispose methods of the container(s)
     * which display this panel
     */
    public void close() {
    	this.matrixPanel.setPinBoard(null);
    	if(detached) {
    		frame.dispose();
    	}
    		tab.close();
    }
}
