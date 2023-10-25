package org.vanted.addons.matrix.ui;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import org.FolderPanel;
import org.GuiRow;
import org.JLabelJavaHelpLink;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.vanted.addons.matrix.datavis.ColorCode;
import org.vanted.addons.matrix.graph.*;
import org.vanted.addons.matrix.keggUtils.MasterAdapter;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.pinBoard.PinBoardTab;
import org.vanted.addons.matrix.clustering.TimeSeriesClustering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Benjamin Moser.
 */
public class SidebarTab extends InspectorTab {

    JScrollPane scrollPane;
    JPanel scrollViewport;
    FolderPanel settingsFolderPanel;
    FolderPanel colorCodePanel;
    FolderPanel analysisFolderPanel;
    TimeSeriesClustering tsClustering;
    private final Color highlightColor = new Color(0, 160, 0);

    public SidebarTab() {
        scrollViewport = new JPanel();
        scrollViewport.setLayout(new BoxLayout(scrollViewport, BoxLayout.PAGE_AXIS));

        scrollPane = new JScrollPane(scrollViewport);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar()
                .setPreferredSize(new Dimension(10, scrollPane.getVerticalScrollBar().getHeight()));
        scrollPane.getHorizontalScrollBar()
                .setPreferredSize(new Dimension(scrollPane.getHorizontalScrollBar().getWidth(), 10));

        this.buildLoadPanel();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        this.add(scrollPane);
        this.updateUI();
    }

    @Override
    public boolean visibleForView(View v) {
        return true;
    }

    public String getTitle() {
        return "Pathway Nexus";
    }

    private void buildLoadPanel() {

        ActionListener o1 = JLabelJavaHelpLink.getHelpActionListener("inputformats");

        FolderPanel fileLoadPanel = new FolderPanel("Load Input File",
                                                    false,
                                                    true,
                                                    false, o1);
        fileLoadPanel.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 5);
        fileLoadPanel.setBackground(null);

        JButton loadExperiments = new JButton("Load Experiment Data");

        loadExperiments.addActionListener(e -> SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Logger logger = Logger.getLogger(this.getClass().getName());

                try {
                    MappingManager.experimentLoadRoutine(SidebarTab.this);
                } catch (FileFormatException e) {
                    logger.severe("Could not read file.");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // No file picked. Do nothing and abort.
                }
            }
        }));
        fileLoadPanel.addGuiComponentRow(null, loadExperiments, false);
        scrollViewport.add(fileLoadPanel);
    }

    public void build() {
        this.buildMatrixSettings();
        this.buildColorCodePanel();
        this.buildAnalysisPanel();
    }

    private void buildMatrixSettings() {
        ActionListener o1 = JLabelJavaHelpLink.getHelpActionListener("inputformats");

        if (this.settingsFolderPanel == null) {
            this.settingsFolderPanel = new FolderPanel("Matrix settings",
                                                       true,
                                                       true,
                                                       false, o1);
            scrollViewport.add(settingsFolderPanel);
        } else {
            for (GuiRow row : this.settingsFolderPanel.getAllGuiRows()) {
                this.settingsFolderPanel.removeGuiComponentRow(row, false);
            }
        }
        settingsFolderPanel.setFrameColor(new JTabbedPane().getBackground(),
                                          Color.BLACK, 0, 5);
        settingsFolderPanel.setBackground(null);

        JPanel settingsContentPanel = new JPanel(new GridBagLayout());
        settingsFolderPanel.addGuiComponentRow(null, settingsContentPanel, false);

        scrollViewport.updateUI();

        JButton rebuild = new JButton("apply");

        JLabel selectTimeLabel = new JLabel("Time Point:");
        GridBagConstraints selTlabelCons = setPosition(2, 0, 2, 1);
        selTlabelCons.anchor = GridBagConstraints.SOUTHWEST;
        settingsContentPanel.add(selectTimeLabel, selTlabelCons);

        CpdPwayGraph matrixGraph = (CpdPwayGraph)MappingManager.getMatrixView().getGraph();
        Experiment exp = (Experiment)matrixGraph.getExperiment();
        String[] timePoints = Experiment.getTimes(exp);

        JComboBox<String> selectTime = new JComboBox<>(timePoints);
        selectTime.addActionListener(e -> {
            String selectedTime = (String)selectTime.getSelectedItem();
            MatrixGraphPanel mgp = this.getMatrixPanel();
            if (mgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            mgp.setTimePoint(selectedTime);
        });
        settingsContentPanel.add(selectTime, setPosition(2, 1, 2, 1));

        JButton transpose = new JButton("flip matrix");
        transpose.setForeground(highlightColor);
        transpose.addActionListener(l -> {

            MatrixGraphPanel mgp = getMatrixPanel();
            if (mgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            mgp.transpose();
        });
        settingsContentPanel.add(transpose, setPosition(0, 1, 2, 1));

        JLabel cellContentLabel = new JLabel("cell content:");
        GridBagConstraints cellClabelCons = setPosition(2, 2, 2, 1);
        cellClabelCons.anchor = GridBagConstraints.SOUTHWEST;
        settingsContentPanel.add(cellContentLabel, cellClabelCons);

        String[] cellContentArray = {"standard deviation", "fold change"};
        JComboBox<String> cellContent = new JComboBox<>(cellContentArray);
        cellContent.setSelectedItem("standard deviation");
        cellContent.addActionListener(e -> {
            String selectedString  = (String)cellContent.getSelectedItem();
            MatrixGraphPanel mgp = this.getMatrixPanel();
            if (mgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            mgp.setCellContent(selectedString);
        });
        settingsContentPanel.add(cellContent, setPosition(2, 3, 2, 1));


//get MatrixGraphPanel to work on
//        MainFrame mf = MainFrame.getInstance();

        MatrixGraphPanel mgp = this.getMatrixPanel();
        if (mgp == null) {
            MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
            return;
        }

//for grouping    	
        JLabel groupPwLabel = new JLabel("group Pathways by");
        JLabel groupCpdLabel = new JLabel("group Compounds by");
        GridBagConstraints groupLabelCons = setPosition(0, 4, 2, 1);
        cellClabelCons.anchor = GridBagConstraints.SOUTHWEST;
        settingsContentPanel.add(groupPwLabel, groupLabelCons);
        groupLabelCons.gridx = 2;
        settingsContentPanel.add(groupCpdLabel, groupLabelCons);

        JComboBox<String> groupPwBy = new JComboBox<>(mgp.getCats(true));
        groupPwBy.addActionListener(e -> {
            String category = (String)groupPwBy.getSelectedItem();

            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            currMgp.setPwCat(category);
        });
        settingsContentPanel.add(groupPwBy, setPosition(0, 5, 2, 1));


        JComboBox<String> groupCpdsBy = new JComboBox<>(mgp.getCats(false));
        groupCpdsBy.addActionListener(e -> {
            String category = (String)groupCpdsBy.getSelectedItem();

            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            currMgp.setCpdCat(category);
        });
        settingsContentPanel.add(groupCpdsBy, setPosition(2, 5, 2, 1));

//for sorting    	
        JLabel sortPwByLabel = new JLabel("sort Pathways by");
        JLabel sortCpdByLabel = new JLabel("sort Compounds by");
        GridBagConstraints sortLabelCons = setPosition(0, 6, 2, 1);
        sortLabelCons.anchor = GridBagConstraints.SOUTHWEST;
        settingsContentPanel.add(sortPwByLabel, sortLabelCons);
        sortLabelCons.gridx = 2;
        settingsContentPanel.add(sortCpdByLabel, sortLabelCons);

        JComboBox<String> sortPwCategory = new JComboBox<>(new String[]{
                "", "name", "contained compounds", "score"});
        sortPwCategory.addActionListener(e -> {
            String property = (String)sortPwCategory.getSelectedItem();

            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            currMgp.sortBy(property, true);
        });
        settingsContentPanel.add(sortPwCategory, setPosition(0, 7, 2, 1));


        JComboBox<String> sortCpdCategory = new JComboBox<>(new String[]{
                "", "name", "fold change", "standard deviation"});
        sortCpdCategory.addActionListener(e -> {
            String property = (String)sortCpdCategory.getSelectedItem();

            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }
            currMgp.sortBy(property, false);
        });

        settingsContentPanel.add(sortCpdCategory, setPosition(2, 7, 2, 1));

//apply changes
        JLabel spacer = new JLabel(" ");
        settingsContentPanel.add(spacer, setPosition(0, 8, 4, 1));

        rebuild.setForeground(highlightColor);
        rebuild.addActionListener(e -> mgp.rebuild());
        rebuild.setBackground(Color.DARK_GRAY);
        rebuild.setOpaque(false);
        settingsContentPanel.add(rebuild, setPosition(0, 9, 2, 1));
    }

    private void buildColorCodePanel() {

        ActionListener o1 = JLabelJavaHelpLink.getHelpActionListener("inputformats");

        if (this.colorCodePanel == null) {
            this.colorCodePanel = new FolderPanel("Color Code",
                                                  true,
                                                  true,
                                                  false,
                                                   o1);
            scrollViewport.add(colorCodePanel);
        } else {
            for (GuiRow row : this.colorCodePanel.getAllGuiRows()) {
                this.colorCodePanel.removeGuiComponentRow(row, false);
            }
        }
        colorCodePanel.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 5);
        colorCodePanel.setBackground(null);

        MatrixGraphPanel mgp = this.getMatrixPanel();
        if (mgp == null) {
            MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
            return;
        }
        JPanel colorCodeSettings = new JPanel(new GridBagLayout());

        JComboBox<String> colorCodeCategory = new JComboBox<>(new String[]{"fold change", "standard deviation"});
        colorCodeCategory.setSelectedIndex(0);
        JLabel cCodeCategoryLabel = new JLabel("Color Code: ");
        colorCodeSettings.add(cCodeCategoryLabel, setPosition(0, 0));
        colorCodeSettings.add(colorCodeCategory, setPosition(0, 1));

        JFormattedTextField minimum = new JFormattedTextField(DecimalFormat.getInstance());
        // minimum.setColumns(4);
        double defaultMin = mgp.getMinimum((String)colorCodeCategory.getSelectedItem());
        minimum.setValue((double)Math.round(defaultMin * 100) / 100);
        JLabel cCodeMinimumLabel = new JLabel("minimum");
        colorCodeSettings.add(cCodeMinimumLabel, setPosition(1, 0));
        colorCodeSettings.add(minimum, setPosition(1, 1));

        JFormattedTextField maximum = new JFormattedTextField(DecimalFormat.getInstance());
        // maximum.setColumns(4);
        double defaultMax = mgp.getMaximum((String)colorCodeCategory.getSelectedItem());
        maximum.setValue((double)Math.round(defaultMax * 100) / 100);
        JLabel cCodeMaximumLabel = new JLabel("maximum");
        colorCodeSettings.add(cCodeMaximumLabel, setPosition(3, 0));
        colorCodeSettings.add(maximum, setPosition(3, 1));

        JFormattedTextField base = new JFormattedTextField(DecimalFormat.getInstance());
        // base.setColumns(4);
        double defaultBase = (mgp.getMaximum((String)colorCodeCategory.getSelectedItem())
                + mgp.getMinimum((String)colorCodeCategory.getSelectedItem())) / 2;
        base.setValue((double)Math.round(defaultBase * 100) / 100);
        JLabel cCodeBaseLabel = new JLabel("base");
        colorCodeSettings.add(cCodeBaseLabel, setPosition(2, 0));
        colorCodeSettings.add(base, setPosition(2, 1));

        JComboBox<Integer> colorCodeGranularity = new JComboBox<>(new Integer[]{3, 4, 5, 6, 7, 8, 9, 10});
        colorCodeGranularity.setSelectedItem(10);
        JLabel cCodegranularityLabel = new JLabel("granularity");
        colorCodeSettings.add(cCodegranularityLabel, setPosition(4, 0));
        colorCodeSettings.add(colorCodeGranularity, setPosition(4, 1));

        JPanel colorCodeDisplay = new JPanel();
        this.displayColorCode(colorCodeDisplay);

        JButton defaultValues = new JButton("get default");
        defaultValues.addActionListener(l -> {
            String category = (String)colorCodeCategory.getSelectedItem();
            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }

            Double min = currMgp.getMinimum(category);
            Double max = currMgp.getMaximum(category);
            minimum.setValue(min);
            maximum.setValue(max);
            base.setValue((min + max) / 2);
            colorCodePanel.updateUI();
        });
        colorCodeSettings.add(defaultValues, setPosition(0, 3));

        JButton applyColorCode = new JButton("apply");
        applyColorCode.setForeground(highlightColor);
        applyColorCode.addActionListener(l -> {
            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }

            currMgp.createColorCode((String)colorCodeCategory.getSelectedItem(),
                    ((Number)minimum.getValue()).doubleValue(),
                    ((Number)maximum.getValue()).doubleValue(),
                    ((Number)base.getValue()).doubleValue(),
                    (Integer)colorCodeGranularity.getSelectedItem());
            currMgp.rebuild();
            displayColorCode(colorCodeDisplay);
            colorCodePanel.updateUI();
        });
        colorCodeSettings.add(applyColorCode, setPosition(2, 3));

        colorCodePanel.addGuiComponentRow(null, colorCodeSettings, false);
        colorCodePanel.addGuiComponentRow(null, colorCodeDisplay, false);

        //scrollViewport.add(colorCodePanel);
    }

    /**
     * Combo Boxes where user can choose, by which category pathways or compounds shall be grouped show only available
     * categories
     *
     */
    private void buildAnalysisPanel() {
        ActionListener o1 = JLabelJavaHelpLink.getHelpActionListener("inputformats");

        if (this.analysisFolderPanel == null) {
            this.analysisFolderPanel = new FolderPanel("Analysis",
                                                       true,
                                                       true,
                                                       false, o1);
            scrollViewport.add(analysisFolderPanel);
        } else {
            for (GuiRow row : this.analysisFolderPanel.getAllGuiRows()) {
                this.analysisFolderPanel.removeGuiComponentRow(row, false);
            }
        }

        analysisFolderPanel.setFrameColor(new JTabbedPane().getBackground(),
                                          Color.BLACK, 0, 5);
        analysisFolderPanel.setBackground(null);

        JPanel analysisContentPanel = new JPanel(new GridBagLayout());
        analysisFolderPanel.addGuiComponentRow(null, analysisContentPanel, false);


        scrollViewport.updateUI();

        analysisContentPanel.removeAll();
        analysisContentPanel.setLayout(new GridBagLayout());

        MatrixGraphPanel mgp = this.getMatrixPanel();
        if (mgp == null) {
            MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
            return;
        }
// 1. row
        JButton masterAdapter = new JButton("get DB data");
        masterAdapter.addActionListener(l -> {
            MasterAdapter ma = new MasterAdapter(mgp.getGraph());
            ma.pack();
            ma.setVisible(true);
        });
        analysisContentPanel.add(masterAdapter, setPosition(0, 0, 2, 1));

        JButton completeRebuild = new JButton("complete rebuild");
        completeRebuild.setForeground(highlightColor);
        completeRebuild.addActionListener(l -> mgp.completeRebuild());
        analysisContentPanel.add(completeRebuild, setPosition(2, 0, 2, 1));

        JLabel spacer = new JLabel(" ");
        analysisContentPanel.add(spacer, setPosition(0, 1, 4, 1));

//2.row: selecting
        //pathways
        JFormattedTextField pwThresholdField = new FormattedNumberField(DecimalFormat.getInstance());
        pwThresholdField.setColumns(10);
        analysisContentPanel.add(pwThresholdField, setPosition(1, 4, 1, 1));

        JComboBox<String> pwGreaterSmaller = new JComboBox<>(new String[]{"<", ">"});
        analysisContentPanel.add(pwGreaterSmaller, setPosition(0, 4, 1, 1));

        JLabel selectPwBy = new JLabel("select Pathways by");
        GridBagConstraints selpbCons = setPosition(0, 2, 2, 1);
        selpbCons.anchor = GridBagConstraints.SOUTHWEST;
        analysisContentPanel.add(selectPwBy, selpbCons);

        JComboBox<String> selectSelPwCategory = new JComboBox<>(new String[]{
                "", "has no match", "amnt. contained compounds", "score"});
        analysisContentPanel.add(selectSelPwCategory, setPosition(0, 3, 2, 1));

        //compounds
        JFormattedTextField cpdThresholdField = new FormattedNumberField(DecimalFormat.getInstance());
        cpdThresholdField.setColumns(10);
        analysisContentPanel.add(cpdThresholdField, setPosition(3, 4, 1, 1));

        JComboBox<String> cpdGreaterSmaller = new JComboBox<>(new String[]{"<", ">"});
        analysisContentPanel.add(cpdGreaterSmaller, setPosition(2, 4, 1, 1));

        JLabel selectCpdBy = new JLabel("select Compounds by");
        GridBagConstraints selcbCons = setPosition(2, 2, 2, 1);
        selcbCons.anchor = GridBagConstraints.SOUTHWEST;
        analysisContentPanel.add(selectCpdBy, selcbCons);
        JComboBox<String> selectSelCpdCategory = new JComboBox<>(new String[]{
                "", "standard deviation", "fold change"});
        analysisContentPanel.add(selectSelCpdCategory, setPosition(2, 3, 2, 1));

        // Button to trigger the selection
        JButton select = new JButton("select");
        select.setForeground(highlightColor);
        select.addActionListener(l -> {
            MatrixGraphPanel currMgp = this.getMatrixPanel();
            if (currMgp == null) {
                MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
                return;
            }

            String pwCategory = (String)selectSelPwCategory.getSelectedItem();
            if (pwCategory != null && !pwCategory.equals("")) {

                switch (pwCategory) {
                    case "has no match":
                        currMgp.selectEmptyPways();
                        break;
                    case "amnt. contained compounds":
                        double threshold;
                        try {
                            threshold = ((Number) pwThresholdField.getValue()).doubleValue();
                        } catch (NullPointerException exc) {
                            MainFrame.showMessageDialog("Please enter a threshold", "no threshold");
                            return;
                        }
                        currMgp.selectBy("contained compounds",
                                          threshold,
                                         true,
                                          (String) pwGreaterSmaller.getSelectedItem());
                        break;
                    case "score":
                        double scoreThreshold;
                        try {
                            scoreThreshold = ((Number) pwThresholdField.getValue()).doubleValue();
                        } catch (NullPointerException exc) {
                            MainFrame.showMessageDialog("Please enter a threshold", "no threshold");
                            return;
                        }
                        currMgp.selectBy("score", scoreThreshold,
                                         true, (String) pwGreaterSmaller.getSelectedItem());
                        break;
                }
            }
            //select compounds
            String category = (String)selectSelCpdCategory.getSelectedItem();
            if (category != null && !category.equals("")) {
                double threshold;
                try {
                    threshold = ((Number)cpdThresholdField.getValue()).doubleValue();
                } catch (java.lang.NullPointerException exc) {
                    MainFrame.showMessageDialog("Please enter a threshold", "no threshold");
                    return;
                }

                currMgp.selectBy(category, threshold, false, (String)cpdGreaterSmaller.getSelectedItem());
            }
        });
        analysisContentPanel.add(select, setPosition(0, 5, 4, 1));

// 3. row:
        JLabel selectedButtons = new JLabel("selected Buttons:");
        GridBagConstraints selButtonsCons = setPosition(0, 6, 2, 1);
        selButtonsCons.anchor = GridBagConstraints.SOUTHWEST;
        analysisContentPanel.add(selectedButtons, selButtonsCons);

        String[] selOptionsArray = {"remove Buttons", "move to top"};
        JComboBox<String> selectionOptions = new JComboBox<>(selOptionsArray);
        //analysisContentPanel.add(selectionOptions, setPosition(0 ,3, 2, 1));

        JButton goSelection = new JButton("Go!");
        goSelection.setForeground(highlightColor);
        goSelection.addActionListener(l -> {
            String selectedOption = (String)selectionOptions.getSelectedItem();
            if (selectedOption == null)
                return;
            if (selectedOption.equals("remove Buttons")) {
                mgp.removeSelection();
            } else if (selectedOption.equals("move to top")) {
                mgp.de_prioritizeSelection();
                mgp.rebuild();
            }
        });

        JPanel selectionOptionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cBoxConstraints = setPosition(0, 0);
        cBoxConstraints.weightx = 0.8;
        selectionOptionsPanel.add(selectionOptions, cBoxConstraints);
        GridBagConstraints buttonConstraints = setPosition(1, 0);
        buttonConstraints.weightx = 0.2;
        selectionOptionsPanel.add(goSelection, buttonConstraints);
        analysisContentPanel.add(selectionOptionsPanel, setPosition(0, 7, 2, 1));


        JButton showDeleted = new JButton("show deleted Buttons");
        showDeleted.addActionListener(e -> {
            ButtonRestoreWindow brw = new ButtonRestoreWindow(mgp);
            brw.setVisible(true);
        });
        analysisContentPanel.add(showDeleted, setPosition(2, 7, 2, 1));

//4. row    	
        //pin board
        JButton pinBoard = new JButton("pin board");
        pinBoard.addActionListener(l -> this.buildPinBoard());
        analysisContentPanel.add(pinBoard, setPosition(0, 8, 2, 1));

        // save as excel
        JButton saveAsExcel = new JButton("save as excel");
        saveAsExcel.addActionListener(l -> mgp.writeToExcelFile());
        analysisContentPanel.add(saveAsExcel, this.setPosition(2, 8, 2, 1));

// 5. row
        JButton updateScore = new JButton("update score");
        updateScore.addActionListener(l -> {
            PathwayRatingUI prui = new PathwayRatingUI(mgp);
            prui.pack();
            prui.setVisible(true);
        });
        analysisContentPanel.add(updateScore, this.setPosition(0, 9, 2, 1));

        JButton clusterButton = new JButton("Cluster");
        clusterButton.addActionListener(l -> {
            if (tsClustering == null || !tsClustering.isRunning())
                tsClustering = new TimeSeriesClustering(mgp.getGraph());
            else {  // Clustering is still running
                MainFrame.showMessageDialog("Cluster calculation is currently running. Wait or stop.",
                                            "Clustering running");
                return;
            }
            ClusteringWindow cw = new ClusteringWindow(tsClustering);
            cw.setVisible(true);
        });
        analysisContentPanel.add(clusterButton, this.setPosition(2, 9, 2, 1));
    }

    /**
     * builds a panel that displays the colorCode of the current matrix
     *
     * @param colorCodeDisplay any JPanel
     */
    private void displayColorCode(JPanel colorCodeDisplay) {
        colorCodeDisplay.removeAll();
        colorCodeDisplay.setLayout(new GridBagLayout());

        MatrixGraphPanel currMgp = this.getMatrixPanel();
        if (currMgp == null) {
            MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
            return;
        }

        ColorCode cCode = currMgp.getColorCode();

        LinkedHashMap<Double, Color> colorMap = cCode.getMap();
        int x = 0;
        for (Entry<Double, Color> entry : colorMap.entrySet()) {
            double value = (double)Math.round(entry.getKey() * 10) / 10;
            JLabel valueLabel = new JLabel("< " + value, SwingConstants.RIGHT);
            JPanel col = new JPanel(new GridLayout());
            col.setBackground(entry.getValue());
            col.add(valueLabel);
            colorCodeDisplay.add(col, setPosition(x++, 0));
        }
    }

    /**
     * if the current view contains a matrixGraphPanel, it is returned by this method if not, it returns null. if there
     * is no active view pops up a message to the user
     *
     * @return matrixGraphPanel for the current view or null
     */
    private MatrixGraphPanel getMatrixPanel() {
        Component c;

        try {
            c = MappingManager.getMatrixView().getViewComponent().getComponent(0);
        } catch (java.lang.NullPointerException e) {
            MainFrame.getInstance().showMessageDialog("no active view!");
            return null;
        }

        if (c instanceof MatrixGraphPanel) {
            return (MatrixGraphPanel)c;
        } else {
            return null;
        }
    }

    public void buildPinBoard() {
        MatrixGraphPanel mgp = this.getMatrixPanel();
        if (mgp == null) {
            MainFrame.showMessageDialog("The active view is not a matrix view!", "Set focus on matrix!");
            return;
        }

        for (Component c : this.getParent().getComponents()) {
            if (c instanceof PinBoardTab) {
                ((PinBoardTab)c).close();
            }
        }

        ArrayList<CompoundTextNode> nodeList = mgp.getSelectedNodes();

        PinBoardTab pinBoard = new PinBoardTab(nodeList, mgp);

        mgp.setPinBoard(pinBoard.getPanel());

        this.getParent().add(pinBoard);
        this.revalidate();
    }

    /**
     * returns an instance of GridBagConstraints with default values and x and y as position
     *
     * @param x x position of item
     * @param y y position of item
     * @return instance of GridBagConstraints
     */
    private GridBagConstraints setPosition(int x, int y) {
        return new GridBagConstraints(x, y, 1, 1, 0.5, 0.5,
                                      GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                                      new Insets(0, 0, 0, 0), 0, 0);
    }

    /**
     * returns an instance of GridBagConstraints with default values and x and y as position and size constraints.
     *
     * @param x x position of item
     * @param y y position of item
     * @param width:  number of columns
     * @param height: number of rows
     * @return instance of GridBagConstraints
     */
    // Right now height is always 1, but a different height might be used in the future.
    @SuppressWarnings("SameParameterValue")
    private GridBagConstraints setPosition(int x, int y, int width, int height) {
        return new GridBagConstraints(x, y, width, height, 0.5, 0.5,
                                      GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                                      new Insets(0, 0, 0, 0), 0, 0);
    }
}