package org.vanted.addons.matrix.ui;


import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import org.AttributeHelper;
import org.OpenFileDialogService;
import org.apache.batik.ext.swing.GridBagConstants;
import org.apache.commons.collections15.BidiMap;
import org.vanted.addons.matrix.datavis.ColorCode;
import org.vanted.addons.matrix.graph.*;
import org.vanted.addons.matrix.keggUtils.CompoundAdapter;
import org.vanted.addons.matrix.keggUtils.PathwayAdapter;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.pinBoard.PinBoardPanel;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.utils.EqPair;
import org.vanted.addons.matrix.utils.NoSelectionModel;
import org.vanted.addons.matrix.utils.Table;
import org.vanted.addons.matrix.writing.MatrixDataFileWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * A JPanel to display the Compounds x Pathways Matrix
 *
 * @author Philipp Eberhard
 */
public class MatrixGraphPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static ArrayList<LabelNode> compounds;
    private static ArrayList<LabelNode> pathways;
    private static CpdPwayGraph matrixGraph;
    private JScrollPane matrixScrollPanel;
    private JPanel corner;
    private JList<String> rowAndColLabel;
    private JPanel matrixPanel;
    private final HashMap<CellNode, MatrixCellButton> cellMap = new HashMap<>();
    private JPanel yAxis;
    private Order yOrder;
    private JPanel xAxis;
    private Order xOrder;
    private final HashSet<MatrixLabelButton> selectedButtons = new HashSet<>();
    private final ArrayList<MatrixLabelButton> deletedButtons = new ArrayList<>();
    private PinBoardPanel pinBoard;
    private SidebarTab sidebar;
    private int size;
    private int labelLength;
    private int rows;
    private int columns;
    private int visibleRows;
    private int visibleColumns;
    private EnhancedGridLayout enhancedGridLayout;

    private boolean isFlipped;

    private String pathwayGroupCategory;
    private String compoundGroupCategory;

    private String colorMapProperty;
    private ColorCode colorCode;

    private String timePoint = "hour 0";
    private String cellContent = "standard deviation";

//constructors

    /**
     * initializes this Panel with a BorderLayout
     */
    public MatrixGraphPanel() {
        super(new BorderLayout());
    }

//methods

    /**
     * initializes ScrollPane and copies lists for compounds and patways maps Data and builds matrix by calling the
     * respective methods
     */
    public void prepareData() {
        this.removeAll();
        matrixScrollPanel = new JScrollPane();
        matrixScrollPanel.setAutoscrolls(true);

        matrixScrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        matrixScrollPanel.getHorizontalScrollBar().setUnitIncrement(16);

        matrixGraph = (CpdPwayGraph)MappingManager.getMatrixView().getGraph();
        compounds = new ArrayList<>(matrixGraph.contents.getYLabels());
        pathways = new ArrayList<>(matrixGraph.contents.getXLabels());

        double min = this.getMinimum("fold change");
        double max = this.getMaximum("fold change");
        createColorCode("fold change", min, max, (min + max) / 2, 10);

        mapData();
        //set sizes
        size = 25;
        labelLength = 150;

        buildMatrix();

        this.add(matrixScrollPanel);
        this.updateUI();
    }

    /**
     * builds Orders for axes assignes lists of LabelNodes to them according to whether the matrix is flipped
     */
    public void mapData() {
        if (isFlipped) {
            xOrder = new Order(compounds, true, false);
            yOrder = new Order(pathways, false, true);
        } else {
            xOrder = new Order(pathways, true, true);
            yOrder = new Order(compounds, false, false);
        }
    }

    /**
     * initializes size related parameters and panels and builds their layout sets their sizes according to mapped data
     * puts them together in matrixScrollPanel and implements zoom on matrixScrollPanel
     */
    public void buildMatrix() {
        //build panels
        matrixScrollPanel.getVerticalScrollBar().setUnitIncrement(16);

        corner = new JPanel();
        matrixPanel = new JPanel();

        xAxis = new JPanel();
        xAxis.setLayout(new GridBagLayout());
        xOrder.setTimePoint(this.timePoint);
        buildXAxis();

        yAxis = new JPanel();
        yAxis.setLayout(new GridBagLayout());
        yOrder.setTimePoint(this.timePoint);
        buildYAxis();

        xAxis.setPreferredSize(new Dimension(visibleColumns * size, labelLength));
        yAxis.setPreferredSize(new Dimension(labelLength, visibleRows * size));

        //build cells
        if (isFlipped) {
            enhancedGridLayout = new EnhancedGridLayout(columns, rows);
            enhancedGridLayout.setTranspose(true);
        }
        else
            enhancedGridLayout = new EnhancedGridLayout(rows, columns);
        matrixPanel.setLayout(enhancedGridLayout);
        buildMatrixPanel();
        matrixPanel.setPreferredSize(new Dimension(visibleColumns * size, visibleRows * size));

        matrixScrollPanel.setViewportView(matrixPanel);
        matrixScrollPanel.setColumnHeaderView(xAxis);
        matrixScrollPanel.setRowHeaderView(yAxis);
        matrixScrollPanel.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);

        // set text to be displayed in the corner
        String[] params = {"columns: " + columns, "rows: " + rows};
        rowAndColLabel = new JList<>(params);
        rowAndColLabel.setSelectionModel(new NoSelectionModel());
        rowAndColLabel.setBackground(null);
        rowAndColLabel.setOpaque(false);
        corner.add(rowAndColLabel);

        JButton clearSelection = new JButton("clear selection");
        clearSelection.addActionListener(l -> {
            this.deselectAll(true);
            this.deselectAll(false);
        });
        corner.add(clearSelection);

        //zoom
        matrixPanel.addMouseWheelListener(e -> {
            int rot = e.getWheelRotation();

            double oldSize = size;
            double xOldOri;
            double yOldOri;
            double xMouse;
            double yMouse;
            try {
                xOldOri = matrixScrollPanel.getViewport().getViewPosition().getX();
                yOldOri = matrixScrollPanel.getViewport().getViewPosition().getY();
                xMouse = matrixPanel.getMousePosition().getX(); //e.getX();
                yMouse = matrixPanel.getMousePosition().getY(); //e.getY();
            } catch (NullPointerException f) {
                return; //exception occurs, if mouse is moved out of MatrixScrollPane while mouse wheel is moved
            }
            if (rot < 0 & size < 300) {                                                //cell size won't exceed 300
                size += 5;
            } else if (rot > 0 &
                    (size - 5) > (matrixScrollPanel.getViewport().getWidth() / columns) &
                    (size - 5) > (matrixScrollPanel.getViewport().getHeight() / rows)) {    //matrixPanel measures shall not be smaller than its viewPort
                size -= 5;
            }

            updatePanelSize();
            matrixScrollPanel.validate();//this and the above updatePanelSize() invocation seem to be necessary for
            // the matrixPanel to actually know its new measures.
            // otherwise, there's a bug if the new origin would be outside the old measures of matrixPanel

            double z = size / oldSize;
            int xNewOri = (int)((xOldOri - xMouse + z * xMouse) + 0.5d);  //0.5d is for proper rounding when casting from double to int
            int yNewOri = (int)((yOldOri - yMouse + z * yMouse) + 0.5d);
            if (xNewOri < 0) xNewOri = 0;
            if (yNewOri < 0) yNewOri = 0;
            Point point = new Point(xNewOri, yNewOri);
            matrixScrollPanel.getViewport().setViewPosition(point);
        });

        matrixScrollPanel.validate();
    }

    /**
     * adds MatrixLabelButtons for categories and MatrixLabelButtons from xOrder to xAxis
     */
    public void buildXAxis() {
        xAxis.removeAll();

        if (isFlipped) {
            xOrder.orderBy(compoundGroupCategory);
        } else {
            xOrder.orderBy(pathwayGroupCategory);
        }
        columns = xOrder.size();
        visibleColumns = xOrder.getVisibleSize();
        if (columns == 0) columns = 1;

        ActionListener categoryAL = e -> {
            MatrixLabelButton button = (MatrixLabelButton)e.getSource();
            String category = button.toString();
            ArrayList<MatrixLabelButton> elements = xOrder.getListFor(category);

            if (!button.isSelected()) {
                for (MatrixLabelButton element : elements) {
                    selectedButtons.add(element);
                    element.setSelected(true);
                }
                button.setSelected(true);
            } else {
                for (MatrixLabelButton element : elements) {
                    selectedButtons.remove(element);
                    element.setSelected(false);

                }
                button.setSelected(false);
            }
        };

        MouseAdapter labelMA = this.getLabelButtonMouseListener();

        ArrayList<String> catList = xOrder.getCategories();                                    //get List of Categories
        int buttonY = 0;
        int catX = 0;

        for (String category : catList) {
            ArrayList<MatrixLabelButton> buttonList = xOrder.getListFor(category);    //get List of elements to the current category

            if (category != null && !category.isEmpty()) {
                buttonY = 1;
                MatrixLabel catLabel = new MatrixLabel(category);                        //create LabelButton for the current category
                MatrixLabelButton catButton = new MatrixLabelButton(catLabel, false);
                catButton.addActionListener(categoryAL);
                catButton.setContentAreaFilled(true);

                catButton.setPosition(catX, 0);
                catButton.setSizeConstraints(buttonList.size(), 1);
                catButton.setWeightConstraints(0.5, 0.1);
                catButton.setPreferredSize(new Dimension(0, 0));

                xAxis.add(catButton, catButton.getConstraints());
            }

            for (int i = 0; i < buttonList.size(); i++) {
                MatrixLabelButton button = buttonList.get(i);
                button.setSelected(false);
                button.setVertical(true);
                button.setPosition(i + catX, buttonY);
                button.setSizeConstraints(1, GridBagConstants.REMAINDER);
                button.setPreferredSize(new Dimension(0, 0));            //seems to be necessary so that all is displayed well.
                button.setWeightConstraints(0.5, 0.90);

                if (button.getMouseListeners().length > 0)
                    button.removeMouseListener(button.getMouseListeners()[0]);
                button.addMouseListener(labelMA);

                xAxis.add(button, button.getConstraints());
            }
            catX += buttonList.size();
        }
        xAxis.updateUI();
    }

    /**
     * adds MatrixLabelButtons for categories and MatrixLabelButtons from yOrder to yAxis
     */
    public void buildYAxis() {
        yAxis.removeAll();

        if (isFlipped) {
            yOrder.orderBy(pathwayGroupCategory);
        } else {
            yOrder.orderBy(compoundGroupCategory);
        }
        rows = yOrder.size();
        visibleRows = yOrder.getVisibleSize();
        if (rows == 0) rows = 1;

        ActionListener categoryAL = e -> {
            MatrixLabelButton button = (MatrixLabelButton)e.getSource();
            String category = button.toString();
            ArrayList<MatrixLabelButton> elements = yOrder.getListFor(category);

            if (!button.isSelected()) {
                for (MatrixLabelButton element : elements) {
                    selectedButtons.add(element);
                    element.setSelected(true);
                }
                button.setSelected(true);
            } else {
                for (MatrixLabelButton element : elements) {
                    selectedButtons.remove(element);
                    element.setSelected(false);

                }
                button.setSelected(false);
            }
        };

        MouseAdapter labelMA = this.getLabelButtonMouseListener();

        ArrayList<String> catList = yOrder.getCategories();                                    //get List of Categories
        int buttonX = 0;
        int catY = 0;

        for (String category : catList) {
            ArrayList<MatrixLabelButton> buttonList = yOrder.getListFor(category);    //get List of elements to the current category

            if (category != null && !category.isEmpty()) {                                                    //create LabelButton for the current category
                buttonX = 1;
                MatrixLabel catLabel = new MatrixLabel(category);
                MatrixLabelButton catButton = new MatrixLabelButton(catLabel, true);
                catButton.addActionListener(categoryAL);
                catButton.setContentAreaFilled(true);

                catButton.setPosition(0, catY);
                catButton.setSizeConstraints(1, buttonList.size());
                catButton.setWeightConstraints(0.1, 0.5);
                catButton.setPreferredSize(new Dimension(0, 0));

                yAxis.add(catButton, catButton.getConstraints());
            }

            for (int i = 0; i < buttonList.size(); i++) {
                MatrixLabelButton button = buttonList.get(i);
                button.setSelected(false);
                button.setVertical(false);
                button.setPosition(buttonX, i + catY);
                button.setSizeConstraints(GridBagConstants.REMAINDER, 1);
                button.setWeightConstraints(0.9, 0.5);
                button.setPreferredSize(new Dimension(0, 0)); //seems to be necessary so that all is displayed well.

                if (button.getMouseListeners().length > 0)
                    button.removeMouseListener(button.getMouseListeners()[0]);
                button.addMouseListener(labelMA);

                yAxis.add(button, button.getConstraints());
            }
            catY += buttonList.size();

        }
        yAxis.updateUI();
    }

    /**
     * builds cell Buttons builds MouseAdapter for panning assigns it to each button in cellButton[][] adds each button
     * from cellButton[][] to matrixPanel
     */
    public void buildMatrixPanel() {
        //handles button selection behaviour and pop up menu for cell buttons
        MouseAdapter ma = new MouseAdapter() {

            private Point origin;

            public void mouseClicked(MouseEvent e) {
                MatrixCellButton button = (MatrixCellButton)e.getSource();
                MatrixLabelButton compound = button.getCompoundButton();
                MatrixLabelButton pathway = button.getPwayButton();

                if (e.getButton() != 3) {
                    if (button.isSelected()) {
                        compound.setSelected(false);
						selectedButtons.remove(compound);

                        pathway.setSelected(false);
						selectedButtons.remove(pathway);
                    } else {
                        compound.setSelected(true);
						selectedButtons.add(compound);

                        pathway.setSelected(true);
						selectedButtons.add(pathway);
                        button.getModel().setPressed(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, matrixPanel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        matrixPanel.scrollRectToVisible(view);
                    }
                }
            }
        };

        matrixPanel.removeAll();
        LinkedHashMap<MatrixLabelButton, LabelNode> yMap;
        LinkedHashMap<MatrixLabelButton, LabelNode> xMap;
        if (isFlipped) {
            yMap = xOrder.getSortedList();
            xMap = yOrder.getSortedList();
        }
        else {
            yMap = yOrder.getSortedList();
            xMap = xOrder.getSortedList();
        }


        BidiMap<EqPair<CompoundTextNode, PathwayTextNode>, CellNode> bidiMap =
                matrixGraph.getContent().getBidiMap();

        MatrixCellButton[][] cellButtons = new MatrixCellButton[yMap.size()][xMap.size()];

        Iterator<MatrixLabelButton> yIterator = yMap.keySet().iterator();
        for (int y = 0; y < yMap.size(); y++) {
            MatrixLabelButton yButton = yIterator.next();
            LabelNode yNode = yMap.get(yButton);

            Iterator<MatrixLabelButton> xIterator = xMap.keySet().iterator();

            for (int x = 0; x < xMap.size(); x++) {
                MatrixLabelButton xButton = xIterator.next();
                LabelNode xNode = xMap.get(xButton);

                CellNode cell;
                CompoundTextNode cptNode;
                cell = bidiMap.get(new EqPair<>((CompoundTextNode) yNode, (PathwayTextNode) xNode));
                cptNode = (CompoundTextNode)yNode;

                cellButtons[y][x] = cellMap.get(cell);
                if (cellButtons[y][x] == null) {
                    cellButtons[y][x] = new MatrixCellButton(xButton, yButton);

                    cellButtons[y][x].setToolTipText(
                            AttributeHelper.getLabel(yNode, "not found") + "/" + AttributeHelper.getLabel(xNode, "not found"));

                    cellButtons[y][x].addMouseListener(ma);
                    cellButtons[y][x].addMouseMotionListener(ma);

                    cellMap.put(cell, cellButtons[y][x]);
                }

                Color color = Color.GRAY;
                if (cell.getMatching()) {
                    String label = "";
                    if (cellContent.equals("standard deviation")) {
                        Double stdDev = cptNode.getStdDevFor(this.timePoint);
                        double roundedStdDev = Math.round(stdDev * 100.0) / 100.0;
                        label = Double.toString(roundedStdDev);
                    } else if (cellContent.equals("fold change")) {
                        Double foldChange = cptNode.getSampleMeanFor(this.timePoint);
                        double roundedFoldChange = Math.round(foldChange * 100.0) / 100.0;
                        label = Double.toString(roundedFoldChange);
                    }

                    cellButtons[y][x].setContent(label);

                    color = getColorMapping(cptNode);
                }
                cellButtons[y][x].setColor(color);
                matrixPanel.add(cellButtons[y][x]);
            }
        }
        matrixPanel.updateUI();
    }

    public void setSidebarTab(SidebarTab sbt) {
        this.sidebar = sbt;
    }

    /**
     * returns the color, that is currently mapped to the Node's colorMapProperty at the current timePoint
     *
     * @param cpdNode: CompoundTextNode
     * @return Color code for this Node
     */
    public Color getColorMapping(CompoundTextNode cpdNode) { //Substance substance) {
        double v = cpdNode.getDoubleProperty(colorMapProperty, timePoint);
        return this.colorCode.getColorFor(v);
    }

    /**
     * returns an array of Strings containing all categories for the specified buttons
     *
     * @param forPathways: True if you want pathway categories. Otherwise, you'll get compound categories
     * @return String array containing all categories
     */
    public String[] getCats(boolean forPathways) {
        return matrixGraph.getCategories(forPathways).toArray(new String[0]);
    }

    /**
     * sets the category according to which pathways shall be categorized
     */
    public void setPwCat(String category) {
        this.pathwayGroupCategory = category;
    }

    /**
     * sets the category according to which compounds shall be categorized
     */
    public void setCpdCat(String category) {
        this.compoundGroupCategory = category;
    }

    public String getTimePoint() {
        return this.timePoint;
    }

    public void setTimePoint(String tp) {
        this.timePoint = tp;
    }

    public void setCellContent(String content) {
        this.cellContent = content;
    }

    /**
     * Transpose the matrix
     */
    public void transpose() {
        isFlipped = !isFlipped;
        enhancedGridLayout.setTranspose(isFlipped);

        Order temp = xOrder;
        xOrder = yOrder;
        yOrder = temp;

        buildXAxis();
        buildYAxis();
        updateCorner();
        updatePanelSize();
    }

    /**
     * removes all components from MatrixGraphPanel and calculates them new
     */
    public void rebuild() {
        this.removeAll(); // makes n calls to component.remove(int)!
        matrixScrollPanel = new JScrollPane();
        matrixScrollPanel.setAutoscrolls(true);

        this.add(matrixScrollPanel);
        buildMatrix(); // modifies matrixScrollPane

        String[] params = {"columns: " + columns, "rows: " + rows};
        rowAndColLabel.setListData(params);

        this.updateUI();
        for (MatrixLabelButton button : this.selectedButtons) {
            button.setSelected(true);
        }
    }

    /**
     * recalculates the whole matrix, on basis of the underlying graph hence takes also changes regarding compounds and
     * pathways into account
     */
    public void completeRebuild() {
        this.removeAll();
        this.cellMap.clear();
        this.prepareData();
    }

    /**
     * deletes all selected buttons from its Order adds them to a list of deleted Buttons and clears selectedButtons
     */
    public void removeSelection() {

        for (MatrixLabelButton button : selectedButtons) {
            if (!xOrder.invertVisible(button)) {
                yOrder.invertVisible(button);
            }
            if (button.isVertical())
                visibleColumns--;
            else
                visibleRows--;
            button.hideCells();
        }
        deletedButtons.addAll(selectedButtons);  //so that they can be restored later
        selectedButtons.clear();
        updateCorner();
        updatePanelSize();
    }

    public HashSet<MatrixLabelButton> getVisibleButtons(boolean pathways) {
        if (pathways) {
            if (this.isFlipped) {
                return yOrder.getVisibleButtons();
            } else {
                return xOrder.getVisibleButtons();
            }
        } else {
            if (this.isFlipped) {
                return xOrder.getVisibleButtons();
            } else {
                return yOrder.getVisibleButtons();
            }
        }
    }

    public HashSet<MatrixLabelButton> getButtons(boolean pathways) {
        if (pathways) {
            if (this.isFlipped) {
                return yOrder.getButtons();
            } else {
                return xOrder.getButtons();
            }
        } else {
            if (this.isFlipped) {
                return xOrder.getButtons();
            } else {
                return yOrder.getButtons();
            }
        }
    }

    /**
     * returns a list of MatrixLabelButtons that were marked for being removed from the Matrix
     */
    public ArrayList<MatrixLabelButton> getDeletedButtons() {
        return this.deletedButtons;
    }

    public HashSet<MatrixLabelButton> getSelectedButtons() {
        return this.selectedButtons;
    }

    public HashMap<String, ArrayList<MatrixLabelButton>> getDeletedButtons(boolean pathways) {
        if (isFlipped) {
            if (pathways) {
                return yOrder.getDeletedButtons();
            } else {
                return xOrder.getDeletedButtons();
            }
        } else {
            if (pathways) {
                return xOrder.getDeletedButtons();
            } else {
                return yOrder.getDeletedButtons();
            }
        }
    }

     /**
     * Changes visibility of a compound or pathway in the matrix.
     *
     * @param button The label button that will change visibility along with its respective row or column
     */
    public void invertVisible(MatrixLabelButton button) {
        if (!xOrder.invertVisible(button)) {
            yOrder.invertVisible(button);
        }
        if (button.isVisible()) {
            if (button.isVertical())
                visibleColumns--;
            else
                visibleRows--;
            button.setSelected(false);
            selectedButtons.remove(button);
            deletedButtons.add(button);
            button.hideCells();
        }
        else {
            if (button.isVertical())
                visibleColumns++;
            else
                visibleRows++;
            deletedButtons.remove(button);
            button.showCells();
        }
        updateCorner();
        updatePanelSize();
    }


    public void sortBy(String property, boolean pathways) {
        if (this.isFlipped) {
            if (pathways) {
                yOrder.sortBy(property);
            } else {
                xOrder.sortBy(property);
            }
        } else {
            if (pathways) {
                xOrder.sortBy(property);
            } else {
                yOrder.sortBy(property);
            }
        }
    }

    /**
     * Selects buttons based on properties specified by the user and adds them to the list of selected buttons of the
     * MatrixGraphPanel.
     *
     * @param property Set a property that will be selected.
     * @param threshold Set a threshold that is used as a condition for selection.
     * @param pathways Switch between pathways and compounds
     * @param greaterSmaller Decides whether to select buttons with a value smaller or larger than threshold.
     */
    public void selectBy(String property, double threshold, boolean pathways, String greaterSmaller) {
        this.deselectAll(pathways);
        ArrayList<MatrixLabelButton> toSelect;

        if (this.isFlipped) {
            if (pathways) {
                toSelect = yOrder.selectBy(property, threshold, greaterSmaller);
            } else {
                toSelect = xOrder.selectBy(property, threshold, greaterSmaller);
            }
        } else {
            if (pathways) {
                toSelect = xOrder.selectBy(property, threshold, greaterSmaller);
            } else {
                toSelect = yOrder.selectBy(property, threshold, greaterSmaller);
            }
        }
        this.selectedButtons.addAll(toSelect);
    }

    /**
     * checks for all currently visible pathways whether they match to currently visible compounds selects all pathway
     * buttons which don't have a match in the current matrix
     */
    public void selectEmptyPways() {
        this.deselectAll(true);

        Iterator<Entry<MatrixLabelButton, LabelNode>> pwIterator;                                            //iterator for all visible pways
        Iterator<LabelNode> cpdIterator;                                                                    //iterator of all visible compoundNodes
        if (isFlipped) {
            pwIterator = yOrder.getSortedList().entrySet().iterator();
            cpdIterator = xOrder.getSortedList().values().iterator();
        } else {
            pwIterator = xOrder.getSortedList().entrySet().iterator();
            cpdIterator = yOrder.getSortedList().values().iterator();
        }

        ArrayList<SubstanceWithPathways> cpdList = new ArrayList<>();                    //List of their substances
        while (cpdIterator.hasNext()) {
            cpdList.add((SubstanceWithPathways)((CompoundTextNode)cpdIterator.next()).getSubstance());
        }

        while (pwIterator.hasNext()) {
            Entry<MatrixLabelButton, LabelNode> entry = pwIterator.next();

            DataPathway pwayNode = ((PathwayTextNode)entry.getValue()).getPathway();
            int i = 0;
            for (SubstanceWithPathways s : pwayNode.getSubstances()) {                    //is any of the associated substances in the list of visible substances?
                if (cpdList.contains(s)) {
                    i++;                                                                //if so, increment i
                }
            }
            if (i == 0) {                                                                //if i is still 0, none of the contained substances is currently visible
                MatrixLabelButton pwayButton = entry.getKey();                        //get button associated to node and select it
                pwayButton.setSelected(true);
                this.selectedButtons.add(pwayButton);
            }

        }
    }

    /**
     * sets isSelected=false to all buttons
     *
     * @param pathways True if all pathways are to be deselected. Otherwise, all compounds will be deselected.
     */
    public void deselectAll(boolean pathways) {
        ArrayList<MatrixLabelButton> toSelect = new ArrayList<>();
        if (pathways) {
            if (isFlipped) {
                toSelect.addAll(yOrder.deselectAll());
            } else {
                toSelect.addAll(xOrder.deselectAll());
            }
        } else {
            if (isFlipped) {
                toSelect.addAll(xOrder.deselectAll());
            } else {
                toSelect.addAll(yOrder.deselectAll());
            }
        }

        toSelect.forEach(this.selectedButtons::remove);
    }



    public ArrayList<CompoundTextNode> getSelectedNodes() {

        ArrayList<CompoundTextNode> nodeList = new ArrayList<>();
        for (MatrixLabelButton button : selectedButtons) {
            if (button.getLabelNode() instanceof CompoundTextNode) {
                nodeList.add((CompoundTextNode)button.getLabelNode());
            }

        }
        return nodeList;
    }

    public void de_prioritizeSelection() {
        for (MatrixLabelButton button : this.selectedButtons) {
            if (isFlipped) {
                if (button.isPathway()) {
                    yOrder.de_prioritize(button);
                } else {
                    xOrder.de_prioritize(button);
                }
            } else {
                if (button.isPathway()) {
                    xOrder.de_prioritize(button);
                } else {
                    yOrder.de_prioritize(button);
                }
            }
        }
    }

    private void addToPinboard(MatrixLabelButton button) {
        if (this.pinBoard == null) {
            selectedButtons.add(button);
            button.setSelected(true);
            sidebar.buildPinBoard();
        }
        this.pinBoard.addChart(button);
    }

    public void setPinBoard(PinBoardPanel pbp) {
        this.pinBoard = pbp;
        if (pbp != null) {
            pbp.setMatrixPanel(this);
        }
    }

    private MouseAdapter getLabelButtonMouseListener() {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                MatrixLabelButton button = (MatrixLabelButton)e.getSource();

                if (e.getButton() == 3) {
                    button.getModel().setPressed(false);

                    PopupMenu popupmenu = new PopupMenu();
                    button.add(popupmenu);
                    //menu for pathways
                    if (button.isPathway()) {
                        MenuItem dataBase = new MenuItem("to network view");
                        dataBase.addActionListener(e1 -> openPathwayAdapter(button));
                        popupmenu.add(dataBase);

                        MenuItem selectAssociated = new MenuItem("associated compounds");
                        selectAssociated.addActionListener(e12 -> selectAssociatedCompounds(button));
                        popupmenu.add(selectAssociated);

                        MenuItem prioritizePW = new MenuItem("de-/prioritize");
                        prioritizePW.addActionListener(e13 -> {
                            if (isFlipped) {
                                yOrder.de_prioritize(button);
                            } else {
                                xOrder.de_prioritize(button);
                            }
                            rebuild();
                        });
                        popupmenu.add(prioritizePW);
                    }
                    //menu for compounds
                    else {
                        MenuItem toPin = new MenuItem("add to pin board");
                        toPin.addActionListener(e14 -> {
                            if (!button.isPathway()) {
                                addToPinboard(button);
                            }
                        });
                        popupmenu.add(toPin);

                        MenuItem addInfo = new MenuItem("Infos");
                        addInfo.addActionListener(e15 -> openCoumpoundAdapter(button));
                        popupmenu.add(addInfo);

                        MenuItem selectAssociated = new MenuItem("associated pathways");
                        selectAssociated.addActionListener(e16 -> selectAssociatedPathways(button));
                        popupmenu.add(selectAssociated);

                        MenuItem prioritizeCpd = new MenuItem("de-/prioritize");
                        prioritizeCpd.addActionListener(e17 -> {
                            if (isFlipped) {
                                xOrder.de_prioritize(button);
                            } else {
                                yOrder.de_prioritize(button);
                            }
                            rebuild();
                        });
                        popupmenu.add(prioritizeCpd);
                    }
                    //general menu items
                    MenuItem remove = new MenuItem("remove");
                    remove.addActionListener(e18 -> invertVisible(button));
                    popupmenu.add(remove);


                    popupmenu.show(button, 0, 0);
                } else {
                    if (button.isSelected()) {
                        selectedButtons.remove(button);
                    } else {
                        selectedButtons.add(button);
                    }
                    button.setSelected(!button.isSelected());
                }
            }
        };
    }
    /**
     * finds the compounds contained in the pathway of this button selects the respective buttons representing these
     * compounds
     */
    private void selectAssociatedCompounds(MatrixLabelButton button) {
        LabelNode node = button.getLabelNode();
        ArrayList<MatrixLabelButton> assocButtons = new ArrayList<>();

        if (node instanceof PathwayTextNode) {
            PathwayTextNode ptn = (PathwayTextNode)node;
            Set<CellNode> cellSet = matrixGraph.getContent().getCol(ptn);

            for (CellNode cell : cellSet) {
                if (cell.getMatching()) {
                    CompoundTextNode cpdNode = cell.getCompoundNode();

                    if (cpdNode.isVisible()) {
                        if (!isFlipped) {
                            assocButtons.add(yOrder.getButton(cpdNode));
                        } else {
                            assocButtons.add(xOrder.getButton(cpdNode));
                        }
                    }
                }
            }
        }

        for (MatrixLabelButton assocButton : assocButtons) {
            assocButton.setSelected(true);
        }
        this.selectedButtons.addAll(assocButtons);
    }

    /**
     * finds the pathways that contain the compound of this button selects the respective buttons representing these
     * pathways
     */
    private void selectAssociatedPathways(MatrixLabelButton button) {
        CompoundTextNode node = (CompoundTextNode)button.getLabelNode();
        ArrayList<MatrixLabelButton> assocButtons = new ArrayList<>();

        Set<CellNode> cellSet = matrixGraph.getContent().getRow(node);

        for (CellNode cell : cellSet) {
            if (cell.getMatching()) {
                PathwayTextNode pwayNode = cell.getPathwayNode();

                if (pwayNode.isVisible()) {
                    MatrixLabelButton assocButton;
                    if (!isFlipped) {
                        assocButton = xOrder.getButton(pwayNode);
                    } else {
                        assocButton = yOrder.getButton(pwayNode);
                    }
                    if (assocButton != null) {
                        assocButtons.add(assocButton);
                    }
                }
            }
        }

        for (MatrixLabelButton assocButton : assocButtons) {
            assocButton.setSelected(true);
        }
        this.selectedButtons.addAll(assocButtons);
    }

    /**
     * instantiates a PathwayAdapter for the PathwayTextNode assigned to this button
     */
    private void openPathwayAdapter(MatrixLabelButton button) {
        PathwayTextNode pathway = (PathwayTextNode)button.getLabelNode();

        PathwayAdapter pwAdapter = new PathwayAdapter(pathway, this);
        pwAdapter.pack();
        pwAdapter.setVisible(true);
    }

    /**
     * retrieves associated pathways and alternative names to the substance mapped on button
     */
    private void openCoumpoundAdapter(MatrixLabelButton button) {
        CompoundTextNode node = ((CompoundTextNode)button.getLabelNode());

        CompoundAdapter cpdAdapter = new CompoundAdapter(node, this);
        cpdAdapter.pack();
        cpdAdapter.setVisible(true);
    }

    /**
     * creates the colorCode for this matrix between min and max
     * centered around a "base" value with an amount of steps reflecting granularity.
     * min, max and base can be null and then be
     * assigned a default value calculated from the mapped data.
     */
    public void createColorCode(String category, Double min, Double max, Double base, Integer granularity) {
        this.colorMapProperty = category;
        boolean findMin = false, findMax = false;
        if (min == null) {
            min = Double.POSITIVE_INFINITY;
            findMin = true;
        }
        if (max == null) {
            max = Double.NEGATIVE_INFINITY;
            findMax = true;
        }

        if (findMin || findMax) {
            for (LabelNode node : compounds) {
                CompoundTextNode cpdNode = (CompoundTextNode)node;
                if (cpdNode != null) {
                    double v = cpdNode.getDoubleProperty(colorMapProperty, timePoint);
                    if (findMin) {
                        min = Math.min(min, v);
                    }
                    if (findMax) {
                        max = Math.max(max, v);
                    }
                }
            }
        }
        if (base == null) {
            base = (min + max) / 2;
        }

        this.colorCode = new ColorCode(min, max, base, granularity);
    }

    public ColorCode getColorCode() {
        return this.colorCode;
    }

    /**
     * returns the maximum value for the given category for the substances currently mapped in this matrix
     */
    public Double getMaximum(String category) {
        double max = Double.NEGATIVE_INFINITY;

        for (LabelNode node : compounds) {
            CompoundTextNode cpdNode = (CompoundTextNode)node;
            if (cpdNode != null)
                max = Math.max(max, cpdNode.getDoubleProperty(category, timePoint));
        }
        return max;
    }

    /**
     * returns the minimum value for the given category for the substances currently mapped in this matrix
     */
    public Double getMinimum(String category) {
        double min = Double.POSITIVE_INFINITY;

        for (LabelNode node : compounds) {
            CompoundTextNode cpdNode = (CompoundTextNode)node;
            if (cpdNode != null)
                min = Math.min(min, cpdNode.getDoubleProperty(category, timePoint));
        }
        return min;
    }

    public CpdPwayGraph getGraph() {
        return matrixGraph;
    }

    public void setGraph(CpdPwayGraph graph) {
        matrixGraph = graph;
    }

    /**
     * collects the data of the currently visible buttons writes them to a Excel file openes dialog window for user to
     * choose a location
     */
    public void writeToExcelFile() {

        HashSet<MatrixLabelButton> pathwayButtons;
        HashSet<MatrixLabelButton> substanceButtons;

        //get currently visible buttons
        if (this.isFlipped) {
            pathwayButtons = yOrder.getVisibleButtons();
            substanceButtons = xOrder.getVisibleButtons();
        } else {
            pathwayButtons = xOrder.getVisibleButtons();
            substanceButtons = yOrder.getVisibleButtons();
        }

        ArrayList<DataPathway> pathways = new ArrayList<>();
        ArrayList<SubstanceInterface> substances = new ArrayList<>();

        //get the resp substances and pathways
        for (MatrixLabelButton pwButton : pathwayButtons) {
            PathwayTextNode pwtNode = (PathwayTextNode)pwButton.getLabelNode();
            DataPathway pathway = pwtNode.getPathway();
            pathways.add(pathway);
        }
        for (MatrixLabelButton substButton : substanceButtons) {
            CompoundTextNode ct = (CompoundTextNode)substButton.getLabelNode();
            SubstanceWithPathways swp = (SubstanceWithPathways)ct.getSubstance();
            // we need the substances without their assigned pathways bc we then would have all invisible pathways
            // included in the sheet, too
            SubstanceWithPathways newSwp = new SubstanceWithPathways();
            newSwp.addAll(swp);
            newSwp.setName(swp.getName());
            newSwp.setAlternativeNames(swp.getAlternativeNames());
            newSwp.setSynonyme(swp.getSynonymMap());

            substances.add(newSwp);
        }

        CpdPwayGraph graph = new CpdPwayGraph();
        graph.addPathways(pathways);

        ExperimentInterface newExp = new Experiment(substances);
        graph.setExperiment(newExp);

        // the substances are assigned to the pathways according to the list of substances in the pathway
        try {
            Table<CompoundTextNode, PathwayTextNode, CellNode> newModel = graph.extractModelFromDataWithExternalPathways(newExp);
            graph.updateModel(newModel, true, matrixGraph);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // the substances contained now in the graphs ExperimentInterface are now only assigned to the pathways present in that graph
        ExperimentInterface updatedExperiment = graph.getExperiment();

        File xmlFile = OpenFileDialogService.getSaveFile(new String[]{"xlsx", "xml"},
                "Experiment-Data (*.xlsx, *.xml)");
        if (xmlFile != null) {
            MatrixDataFileWriter.writeExcel(xmlFile, updatedExperiment);
        }
    }

    private void updatePanelSize() {
        xAxis.setPreferredSize(new Dimension(size * visibleColumns, labelLength));
        xAxis.updateUI();
        yAxis.setPreferredSize(new Dimension(labelLength, size * visibleRows));
        yAxis.updateUI();
        matrixPanel.setPreferredSize(new Dimension(size * visibleColumns, size * visibleRows));
        matrixPanel.updateUI();
    }

    private void updateCorner() {
        corner.remove(rowAndColLabel);
        String[] params = {"columns: " + visibleColumns, "rows: " + visibleRows};
        rowAndColLabel = new JList<>(params);
        rowAndColLabel.setSelectionModel(new NoSelectionModel());
        rowAndColLabel.setBackground(null);
        rowAndColLabel.setOpaque(false);
        corner.add(rowAndColLabel, 0);
    }
}