package org.vanted.addons.matrix.mapping;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.*;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

import org.ErrorMsg;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.session.EditorSession;
import org.vanted.addons.matrix.graph.CellNode;
import org.vanted.addons.matrix.graph.MatrixGraph;
import org.vanted.addons.matrix.reading.MatrixFileReader;
import org.vanted.addons.matrix.ui.CustomView;
import org.vanted.addons.matrix.ui.SidebarTab;
import org.vanted.addons.matrix.ui.Utils;

import javax.swing.*;

import java.io.File;
import java.util.logging.Logger;

/**
 * Contains some high-level routines for data import and mapping
 *
 * @author Benjamin Moser.
 */
public class MappingManager {

    private static final Logger logger = Logger.getLogger("File loading");
    private static CustomView matrixView;
    private static EditorSession es;

    /**
     * Presents the matrix graph in a View.
     *
     * @param g The graph to show
     */
    public static void showMatrixGraphView(MatrixGraph g) {
        MainFrame mf = MainFrame.getInstance();
        if (matrixView == null) {
            es = new EditorSession(g);
            matrixView = (CustomView) mf.createInternalFrame(CustomView.class.getCanonicalName(), es, false);
            mf.setActiveSession(es, matrixView);
        }
        SwingUtilities.invokeLater(() -> {
            GraphHelper.issueCompleteRedrawForView(matrixView, g);
        });
    }

    /**
     * Entry point for loading, processing and mapping experiment (and compound) data.
     * TODO: move somewhere else
     */
    public static void experimentLoadRoutine(SidebarTab sidebarTab) throws FileFormatException, IllegalArgumentException {

        File file;
        file = pickFileSafe();

        ensureActiveGraph();
        processFile(file, sidebarTab);
    }

    /**
     * ensure that there is a matrix graph to work on
     */
    private static void ensureActiveGraph() {
        if (!haveActiveGraph()) {
            showMatrixGraphView(Utils.getEmptyGraph());
            logger.info("created new empty graph and put it into a view");
        } else if (!graphIsValid()) {
            showMatrixGraphView(Utils.getDummyGraph());
            logger.info("created new dummy graph and put it into a view");
            }
    }

    /**
     * returns true if current active Graph of MainFrame is instanceof MatrixGraph
     */
    private static boolean graphIsValid() {
        Graph currentGraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
        return currentGraph instanceof MatrixGraph;
    }

    private static boolean haveActiveGraph() {
        // left-hand side is checked first
        return !(MainFrame.getInstance().getActiveEditorSession() == null
                || MainFrame.getInstance().getActiveEditorSession().getGraph() == null);
    }

    public static void showErrorInfo(String s) {
        Logger logger = Logger.getLogger("Util");
        logger.severe(s);
        ErrorMsg.addErrorMessage(s);
    }

    /**
     * opens a file pick dialog and checks the picked file for !=null
     * and whether it can be handled by ExperimentLoader
     * @return File
     * @throws IllegalArgumentException in case no file was selected
     * @throws FileFormatException in case file can not be loaded
     */
    public static File pickFileSafe() throws IllegalArgumentException, FileFormatException {

    // open a file pick dialog
        final File file = OpenExcelFileDialogService.getExcelOrBinaryFile();

    // error handling
        if (file == null) {
            throw new IllegalArgumentException();
        }

        if (!ExperimentLoader.canLoadFile(file)) {
            throw new FileFormatException();
        }

        return file;
    }

    static void processFile(File file, SidebarTab sidebarTab) {
        // The matrixView must be the active view for this to work
        if (MainFrame.getInstance().getActiveSession().getActiveView() != matrixView)
            MainFrame.getInstance().setActiveSession(es, matrixView);

    //create TableDate representation of the given File
        final TableData tableData = ExperimentDataFileReader.getExcelTableData(file, null);
    
        ExperimentDataFileReader metabolonReader = new MatrixFileReader();

        // note that this ExperimentDataPresenter can be used to implement Drag&Drop in the future
        ExperimentDataPresenter receiver = (td, experimentName, md, gui) -> {

            // for the processor to be used within `processIncomingData`, it needs to be in the list of
            // available processors
            upsertProcessor(MapToMatrixProcessor.getInstance(sidebarTab));

            // TODO: access pathway from TableData here
            // TODO: incorporate other pathways from other sources here
            // Problem: the ExperimentDataProcessor only receives the ExperimentInterface.
            // How to pass pathway information?
            // -- somehow augment ExperimentInterface with pway information
            //      -- subclassing doesnt really work because we lose the specific type by handing it through the
            //          interfaces. Technically, we could cast down.
            // -- do we even need to use ExperimentDataProcessingManager? If not, simply do the work here.
            //      -- answer is no. not using it even removes complexity. (but dont forget to still use a
            //          background task with progress reporting; and maybe invoke post processors?)

            // note that if a processor is specified here, only this processor will be used and
            // no dialog to select a processor will be shown
            ExperimentDataProcessingManager.getInstance().processIncomingData(md,
                    (Class) MapToMatrixProcessor.class);
        };

        ExperimentLoader.loadExcelFileWithBackGroundService(metabolonReader, tableData, file,
                new RunnableWithXMLexperimentData() {
                    private ExperimentInterface md = null;

                    /**
                     * <code>setExperimentData</code> will be automatically called before this
                     * method is called. This is a two step solution as the loading of the data is
                     * done in background.
                     */
                    public void run() {
                        // TODO: experiment name
                        receiver.processReceivedData(tableData, "Metabolon Data", md, null);
                    }

                    public void setExperimenData(ExperimentInterface md) {
                        this.md = md;
                    }
                });
    }

    /**
     * Register a given experiment data processor. A processor has to be registered before it can be used with
     * `processIncomingData`.
     *
     * @param p: ExperimentDataProcessor
     * @see ExperimentDataProcessingManager#processIncomingData(ExperimentInterface, Class)
     */
    public static void upsertProcessor(ExperimentDataProcessor p) {
        if (!ExperimentDataProcessingManager.getExperimentDataProcessors().contains(p)) {
            ExperimentDataProcessingManager.addExperimentDataProcessor(p);
        }
    }

    /**
     * Add a XMLAttribute with the experiment data to the cell. Heavily based on the method linked below. Difference is
     * that we do not set any (attribute) component type.
     *
     * @see Experiment2GraphHelper#addMappingData2Node(SubstanceInterface, GraphElement, String)
     */
    public static void addExperimentDataToCell(SubstanceInterface subst, CellNode cell) {
        XMLAttribute measurementDataAttr;
        CollectionAttribute mappingAttr;

        // access attribute at "mapping", create if not present
        try {
            mappingAttr = (CollectionAttribute) cell.getAttribute(Experiment2GraphHelper.mapFolder);
        } catch (AttributeNotFoundException e) {
            cell.addAttribute(new HashMapAttribute(Experiment2GraphHelper.mapFolder), "");
            mappingAttr = (CollectionAttribute) cell.getAttribute(Experiment2GraphHelper.mapFolder);
        }

        // access attribute "measurementdata" (child of "mapping".
        try {
            measurementDataAttr = (XMLAttribute) mappingAttr.getAttribute(Experiment2GraphHelper.mapVarName);
        } catch (AttributeNotFoundException e) {
            mappingAttr.add(new XMLAttribute(Experiment2GraphHelper.mapVarName), false);
            measurementDataAttr = (XMLAttribute) mappingAttr.getAttribute(Experiment2GraphHelper.mapVarName);
        }

        // Add XML Substance Data to Mapping List
        measurementDataAttr.addData(subst);
    }

    public static CustomView getMatrixView() {
        return matrixView;
    }

    /**
     * Add an attribute to the cell that will serve to visualise the mapped experiment data via its assigned
     * attribute component.
     */
    public static void addVisAttrToCell(CellNode cell) {
      //  if (!AttributeHelper.hasAttribute(cell, ColorMapAttribute.path, ColorMapAttribute.name)) {
        //    // TODO: other visualisations, more dynamic
         //   Attribute attr = new ColorMapAttribute(ColorMapAttribute.name, "a"); // TODO
          //  AttributeHelper.setAttribute(cell, ColorMapAttribute.path, ColorMapAttribute.name, attr);
     //   }
    }

    public static void reset() {
        es = null;
        matrixView = null;
    }
}
