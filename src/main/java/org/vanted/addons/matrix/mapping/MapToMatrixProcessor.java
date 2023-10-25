package org.vanted.addons.matrix.mapping;


import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import org.BackgroundTaskStatusProvider;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.view.View;
import org.vanted.addons.matrix.graph.MatrixGraph;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;
import org.vanted.addons.matrix.ui.SidebarTab;

import java.awt.*;
import java.util.logging.Logger;

import org.vanted.addons.matrix.graph.CpdPwayGraph;


/**
 * Maps the given experiment data to a matrix graph.
 * This is an instance of a "experiment data processor". Usually, an experiment data processor is registered with the
 * ExperimentDataProcessingManager which in turn feeds the processor with data. In this case, however, since we do *not*
 * want this processor to be available for other workflows, it is not registered.
 * @author Benjamin Moser.
 */
public class MapToMatrixProcessor extends AbstractExperimentDataProcessor {

    private ExperimentInterface experimentData;
    private final SidebarTab sidebarTab;

    // TODO: Implement meaningful status messages and functionality to interrupt (see BackgroundTaskHelper)
    private final BackgroundTaskStatusProvider mappingWorkStatusProvider =
            new BackgroundTaskStatusProviderSupportingExternalCallImpl("Performing mapping onto matrix.", "");

    private MapToMatrixProcessor(SidebarTab sidebarTab) {
        // do not expose constructor
        this.sidebarTab = sidebarTab;
    }

    Logger logger = Logger.getLogger("MapToMatrixProcessor");

    private static MapToMatrixProcessor instance;
    public static MapToMatrixProcessor getInstance(SidebarTab sidebarTab) {
        if (instance == null)
            instance = new MapToMatrixProcessor(sidebarTab);
        return instance;
    }

    private Runnable getMappingWorkTask(ExperimentInterface experimentData) {
        // this is to provide the returned runnable with a parameter
        return () -> {
            Graph graph = MappingManager.getMatrixView().getGraph();
            try {
                // make sure we are indeed working with a matrix graph
                CpdPwayGraph matrixGraph = (CpdPwayGraph) graph;
                matrixGraph.setExperiment(experimentData);

                // update the current graph model with the new data, potentially insert elements (labels, cells)
                // into the graph if they are present in the incoming data but not yet in the current graph
                // TODO: put 'addMissing' into UI and access state. Function 'updateModel' needs to be changed first.
                matrixGraph.updateModel(matrixGraph.extractModelFromData(experimentData), true, matrixGraph);

            } catch (ClassCastException e) {
                e.printStackTrace();
                // TODO -- graph was not a matrix graph
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("Experiment data does not have the required format");
            }

            Component c;
            try {
                c = MappingManager.getMatrixView().getViewComponent().getComponent(0);
            } catch (NullPointerException e) {
                MainFrame.getInstance().showMessageDialog("no active view!");
                return;
            }
            MatrixGraphPanel mgp;
            if (c instanceof MatrixGraphPanel) {
                mgp = (MatrixGraphPanel) c;
            } else {
                return;
            }
            ((CpdPwayGraph) MappingManager.getMatrixView().getGraph()).deleteDummies();
            mgp.prepareData();
            mgp.setSidebarTab(sidebarTab);
            sidebarTab.build();

            invokePostProcessors();
        };
    }


    /**
     * an identical (except for the message) routine is already called right after invoking `processData`.
     * see AbstractExperimentDataProcessor.execute
     * however, if a background task is used to run this processor (as we do), we have to run the
     * postprocessors again after everything is done.
     */
    private void invokePostProcessors() {
        for (Runnable r : postProcessors)
            r.run();
        setExperimentData(null);
        MainFrame.showMessage("Done loading experiment data for matrix", MessageType.INFO);
    }

    /**
     * Central entry point, this method is called by the ExperimentDataProcessingManager.
     * {@link de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager}
     */
    @Override
    protected void processData() {
        // facilitates running the task asynchroneously and with progress feedback
        BackgroundTaskHelper bth = new BackgroundTaskHelper(
                // note that the `experimentData` field will be set to null after leaving this method. Thus, it can't
                // be accessed safely at a later point in time. Hence we pass the reference through as a parameter.
                getMappingWorkTask(this.experimentData),
                mappingWorkStatusProvider,
                "title",
                "name",
                true,
                false
        );
        bth.startWork(this);
    }

    @Override
    public void setExperimentData(ExperimentInterface experimentData) {
        this.experimentData = experimentData;
    }

    @Override
    public boolean activeForView(View v) {
        // the processor has to be registered (globally) among all other processors,
        // thus there might be potential side-effects otherwise.
        try {
            Graph graph = MainFrame.getInstance().getActiveSession().getGraph();
            return (graph instanceof MatrixGraph);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "Map experiment data to matrix graph";
    }
}
