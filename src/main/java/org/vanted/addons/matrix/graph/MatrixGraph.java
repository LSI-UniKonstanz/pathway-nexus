package org.vanted.addons.matrix.graph;


import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;


import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.MapIterator;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.*;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;
import org.vanted.addons.matrix.utils.EqPair;
import org.vanted.addons.matrix.utils.Table;

import java.io.File;
import java.util.*;

/**
 * Abstract internal representation of a matrix graph. The contained types were parametrised to allow subclasses
 * to require their own types. This is more restrictive than e.g. using subclass of LabelNode. In particular this means
 * that in a graph, yLabels, xLabels and Cells can only be of one type, respectively (and not merely both inheriting
 * from a common superclass).
 * @author Benjamin Moser.
 */
public abstract class MatrixGraph<YLabelT extends LabelNode, XLabelT extends LabelNode, CellT extends CellNode>
        extends EdgelessGraph
        implements Factory<CellT> {
    private boolean modified;
    private String idName;
    private int id; // TODO
    private String fileTypeDescription;

    public Table<YLabelT, XLabelT, CellT> contents = new Table<>();
    private ExperimentInterface experiment;


    public MatrixGraph() {
        super();
    }

    /**
     * Override this method because the default implementation uses getNodes().size where getNodes() is a list type.
     * This doesn't apply here.
     *
     * @return The number of cells in the matrix
     */
    @Override
    public int getNumberOfNodes() {
        return contents.size();
    }

    @Override
    protected void doAddNode(Node node) {
        // TODO
    }

    @Override
    protected void doClear() {
        // TODO
    }

    @Override
    protected void doDeleteNode(Node n) throws GraphElementNotFoundException {
        // TODO
    }

    @Override
    protected Node createNode() {
        return null; // TODO
    }

    @Override
    protected Node createNode(CollectionAttribute col) {
        return null; // TODO
    }

    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return a list of *all* node elements of the MatrixGraph. Note that this includes label nodes as well as cell nodes.
     */
    @Override
    public List<Node> getNodes() {
        List<Node> l = new LinkedList<>();
        l.addAll(contents.getYLabels());
        l.addAll(contents.getXLabels());
        l.addAll(contents.getAllCells());
        return l;
    }


    @Override
    public Iterator<Node> getNodesIterator() {
        return null; // TODO
    }

    @Override
    public void deleteAll(Collection<? extends GraphElement> graphelements) {
        // TODO
    }

    @Override
    public String getName() {
        return getName(false);
    }

    @Override
    public void setName(String name) {
        this.idName = name;
    }

    @Override
    public String getName(boolean fullName) {
        if (idName == null)
            return "[not saved " + id + "]";
        if (fullName)
            return idName;
        else {
            String res;
            if (idName.lastIndexOf(File.separator) > 0)
                res = idName.substring(idName.lastIndexOf(File.separator) + File.separator.length());
            else if (idName.lastIndexOf("/") > 0)
                res = idName.substring(idName.lastIndexOf("/") + "/".length());
            else
                res = idName;
            return res;
        }
    }

    /**
     * Not supported anymore.
     *
     * @see AdjListGraph#numberGraphElements()
     * @see Graph#getNumberOfNodes()
     */
    @Override
    @Deprecated
    public void numberGraphElements() {
    }

    /**
     * @param id of the graph.
     * @see AdjListGraph#checkMaxGraphElementId(long)
     */
    @Override
    public void checkMaxGraphElementId(long id) {
        // not implemented.
    }

    @Override
    public void setListenerManager(ListenerManager object) {
        // TODO
    }

    @Override
    public String getFileTypeDescription() {
        return this.fileTypeDescription;
    }

    @Override
    public void setFileTypeDescription(String fileTypeDescription) {
        this.fileTypeDescription = fileTypeDescription;
    }

    /**
     *  How experiment data is unpacked and mapped to nodes is left to the specific kind of matrix graph
     *  for e.g. a (compound x pathway)-matrix or a (time x #{cpds in pathway changed})-matrix,
     *  different  • aggregations  and  • methods of mapping to graph elements  are needed.
     *  these are, among others, properties of the specific kind of matrix graph.
     * @param experimentData
     * @return
     */
    public abstract Table<YLabelT, XLabelT, CellT> extractModelFromData(ExperimentInterface experimentData) throws Exception;

    public void updateModel(Table<YLabelT, XLabelT, CellT> newModel, boolean addMissing, Factory<CellT> cellTFactory) {
    	this.contents = newModel;
    }
    
    /**
     * Update the graphs model with data from `newModel`, potentially inserting new rows/cols.
     * This can be implemented here because its logic is independent from the semantic interpretation of graph elements.
     * 
     * I replaced this method by the above. Since in this version the Nodes were not updated: e.g. if compounds were assigned an 
     * additional pathway from the matrix the resp PWTNode was not assigned the resp CpdTNode
     * So far I did not encounter a problem with this solution
     * 
     * @param newModel
     * @param addMissing
     * @param cellTFactory -- We need a means of creating new cells to fill the matrix in case new columns or rows
     *                     were created.
     */
    public void updateModel2(Table<YLabelT, XLabelT, CellT> newModel, boolean addMissing, Factory<CellT> cellTFactory) {
        for (Map.Entry<EqPair<YLabelT, XLabelT>, CellT> entry : newModel.getBidiMap().entrySet()) {
            EqPair<YLabelT, XLabelT> pair = entry.getKey();
            CellT cell = entry.getValue();

            // if there are new labels, fill any new rows/cols with empty cells
            if (addMissing) {
                // in case new columns or rows were created, we have to fill up empty spots with blank cells.
                YLabelT incomingYLabel = pair.getFst();
                XLabelT incomingXLabel = pair.getSnd();
                if (!this.contents.getXLabels().contains(incomingXLabel)) {
                    for (YLabelT presentYLabel : this.contents.getYLabels()) {
                        CellT newCell = cellTFactory.create();
                        this.contents.upsertCell(new EqPair<>(presentYLabel, incomingXLabel), newCell);
                        //MappingManager.addVisAttrToCell(newCell);
                        //fillWithCellIfEmpty(new EqPair<>(presentYLabel, incomingXLabel), cellTFactory);
                    }
                }

                if (!this.contents.getYLabels().contains(incomingYLabel)) {
                    for (XLabelT presentXlabel : this.contents.getXLabels()) {
                        CellT newCell = cellTFactory.create();
                        this.contents.upsertCell(new EqPair<>(incomingYLabel, presentXlabel), newCell);
                        MappingManager.addVisAttrToCell(newCell);
                        //fillWithCellIfEmpty(new EqPair<>(incomingYLabel, presentXlabel), cellTFactory);
                    }
                }
            }

            // upsert cells for which we have new data
            if (this.contents.getBidiMap().containsKey(pair)) {
                // by power of overriding `equals` and `hashCode` for label and cell nodes,
                // this does everything we need.
                this.contents.upsertCell(pair, cell);
                MappingManager.addVisAttrToCell(cell);

                PathwayTextNode pn = (PathwayTextNode) pair.getSnd();
    			DataPathway pw = (DataPathway) pn.getPathway();
             	if(pw != null && pw.getTitle() != null && pw.getTitle().equals("TCA Cycle")) {
             		System.out.println("bidicheck: " + pw.getTitle());
             		for(CompoundTextNode cn: pn.getCompoundTextNodes()) {
             			
                 		System.out.println("\t" + cn.getSubstance().getName());
             		}
             	}
                	
                }
                
            }
        

        
        ArrayList<LabelNode> pathways = new ArrayList<LabelNode>(newModel.getXLabels());

        for(LabelNode ln: pathways) {
         	PathwayTextNode pn = (PathwayTextNode) ln;
			DataPathway pw = (DataPathway) pn.getPathway();
         	if(pw != null && pw.getTitle() != null && pw.getTitle().equals("TCA Cycle")) {
         		System.out.println("newModel: " + pw.getTitle());
         		for(CompoundTextNode cn: pn.getCompoundTextNodes()) {
         			
             		System.out.println("\t" + cn.getSubstance().getName());
         		}
         	}
         }
        
        ArrayList<LabelNode> pathways2 = new ArrayList<LabelNode>(contents.getXLabels());

        for(LabelNode ln: pathways2) {
         	PathwayTextNode pn = (PathwayTextNode) ln;
			DataPathway pw = (DataPathway) pn.getPathway();
         	if(pw != null && pw.getTitle() != null && pw.getTitle().equals("TCA Cycle")) {
         		System.out.println("contents: " + pw.getTitle());
         		for(CompoundTextNode cn: pn.getCompoundTextNodes()) {
         			
             		System.out.println("\t" + cn.getSubstance().getName());
         		}
         	}
         }
        
        // empty spots will be at
        // - where one label in the new model was new but another one already present
        // - in the spots of the old model where something new came in
    }

    private void fillWithCellIfEmpty(EqPair<YLabelT, XLabelT> cellId, Factory<CellT> cellTFactory) {
        if (!this.contents.getBidiMap().containsKey(cellId)) {
            CellT blankCell = cellTFactory.create();
            this.contents.upsertCell(cellId, blankCell);
        }
    }

//methods for MatrixGraphPanel
    
    /**
     * assignes an experiment to this Graph
     * TODO: extract data in the same step?
     * @param exp
     */
    public void setExperiment(ExperimentInterface exp) {
    	this.experiment = exp;
    }
    
    public ExperimentInterface getExperiment() {
    	return  this.experiment;
    }
    
    public Table<YLabelT, XLabelT, CellT> getContent(){
    	return this.contents;
    }
    
     public void clear() {
    	 this.contents.clear();
     }
}
