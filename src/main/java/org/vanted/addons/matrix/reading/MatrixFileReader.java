package org.vanted.addons.matrix.reading;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.*;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.vanted.addons.matrix.graph.CpdPwayGraph;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.utils.IntRange;

import java.io.File;
import java.util.*;

/**
 * Assumptions about the input file:
 * - Substances start at row 3 (1-indexed)
 * - Each subsequent row corresponds to a substance, there are no blank rows in between
 * - Each row has the same number of columns
 * - Measurement values are not blank
 * - sub and super pathway fields are not blank
 *
 * @author Benjamin Moser.
 */
public class MatrixFileReader extends ExperimentDataFileReader {

    // note that rows and cols are 1-indexed
    Map<Integer, IntRange> epochs = initEpochs();
    private final Map<Integer, Integer> alternateIdsCols = initAlternateIdsCols();
    private final int substanceColIndex = 1;
    HashMap<String, Integer> columnMap = new HashMap<>();

    
    private Map<Integer, IntRange> initEpochs() {
        // key: the sample time value
        // value: the range of corresponding columns
        Map<Integer, IntRange> epochs = new HashMap<>();
        epochs.put(0, new IntRange(9, 12));
        epochs.put(3, new IntRange(13, 15));
        epochs.put(6, new IntRange(16, 18));
        epochs.put(8, new IntRange(19, 21));
        epochs.put(10, new IntRange(22, 24));
        epochs.put(12, new IntRange(25, 27));

        return epochs;
    }

    private Map<Integer, Integer> initAlternateIdsCols() {
        Map<Integer, Integer> m = new HashMap<>();
        m.put(0, 4); // CAS
        m.put(1, 5); // pubchem
        m.put(2, 6); // chemspider
        m.put(3, 7); // kegg
        m.put(4, 8); // hmdb
        return m;
    }


    private ExperimentInterface csvMetabolite(TableData td) {
        ExperimentInterface experiment = new Experiment();
        ArrayList<DataPathway> pathways= new ArrayList<>();

        // Find out which data is present and which columns contain them
        int maxCols = td.getMaximumCol();

        String timeUnit = td.getCellData(2, 1, "").toString();
        int dataStartIndex, dataEndIndex;
        dataStartIndex = ((Double)td.getCellData(3, 1, "")).intValue();
        dataEndIndex = ((Double)td.getCellData(4, 1, "")).intValue();
        for (int i = 1; i <= maxCols; i++) {
            String row2Content = td.getCellData(i, 2, "").toString().trim();
            if (!row2Content.isEmpty())
                columnMap.put(row2Content, i);
        }

        HashMap<Integer, IntRange> sampleLabels = new HashMap<>();
        ArrayList<Integer> labelList = new ArrayList<>();
        ArrayList<Integer> labelCol = new ArrayList<>();
        for (int i = dataStartIndex; i <= dataEndIndex; i++)  {
            String label = td.getCellData(i, 2, "").toString();
            if (!label.isEmpty()) {
                double time;
                try {
                    time = Double.parseDouble(label);
                } catch (NumberFormatException e) {
                    MainFrame.getInstance()
                            .showMessageDialog("The csv file seems to be broken. " +
                                    "Please ensure that the timestamps for the samples contain only integers.");
                    return null;
                }
                labelList.add((int)time);
                labelCol.add(i);
            }
        }
        for (int i = 0; i < labelList.size() - 1; i++)
            sampleLabels.put(labelList.get(i), new IntRange(labelCol.get(i), labelCol.get(i + 1) - 1));

        sampleLabels.put(labelList.get(labelList.size() - 1),
                new IntRange(labelCol.get(labelList.size() - 1), dataEndIndex));

        Set<String> columnHeaders = columnMap.keySet();
        Set<String> availableDBs = new TreeSet<>();
        availableDBs.add("CAS");
        availableDBs.add("PUBCHEM");
        availableDBs.add("CHEMSPIDER");
        availableDBs.add("KEGG");
        availableDBs.add("HMDB");
        availableDBs.retainAll(columnHeaders);
        Object[] availableDBsArr = availableDBs.toArray();

        // Parse table data
        for (int rowIndex = 3; rowIndex <= td.getMaximumRow(); rowIndex++) {
            // create and attach substance
            SubstanceWithPathways subst = new SubstanceWithPathways();
            String[] namesArray = td.getCellData(columnMap.get("NAME"), rowIndex, "").toString()
                    .split(";");
            String[] namesTrimmedArray = new String[namesArray.length];
            for (int i = 0; i < namesArray.length; i++)
                namesTrimmedArray[i] = namesArray[i].trim();
            subst.setName(namesTrimmedArray[0]);
            subst.setAlternativeNames(new HashSet<>(Arrays.asList(namesTrimmedArray)));
            for (int i = 0; i < availableDBsArr.length; i++) {
                String dbName = availableDBsArr[i].toString();
                Object dbIDObj = td.getCellData(columnMap.get(dbName), rowIndex, "");
                String dbIDStr = "";
                if (dbIDObj instanceof Double) {
                    int dbIDInt = ((Double) dbIDObj).intValue();
                    if (dbIDInt > 0)
                        dbIDStr = Integer.toString(dbIDInt);
                }
                if (dbIDObj instanceof String)
                    dbIDStr = dbIDObj.toString();
                subst.setDbId(dbName, dbIDStr);
            }
            if(!subst.getName().equals("no name"))  // when reloading a saved work in progress the dummy substance from creating CpdPwayGraph comes through
                experiment.add(subst);

            // handle sub pathways
            String[] subPathways = td.getCellData(columnMap.get("SUB_PATHWAY"), rowIndex, "").toString()
                    .split(";");
            for (int i = 0; i < subPathways.length; i++) {
                DataPathway pw = new DataPathway(subPathways[i]);
                if (i == 0)
                    pw.setSuperPathway(td.getCellData(columnMap.get("SUPER_PATHWAY"), rowIndex, "")
                            .toString());
                if (!pathways.contains(pw))
                    pathways.add(pw);
                else if (pw.getSuperPathway() != null)
                    pathways.get(pathways.indexOf(pw)).setSuperPathway(pw.getSuperPathway());
                pathways.get(pathways.indexOf(pw)).addSubstance(subst);	// assign substance to pathway
                subst.addPathway(pw);
            }

            // only have one single condition in this case
            Condition cond = new Condition(subst);
            subst.add(cond); // attach (yet empty) condition to substance

            for (int i = 0; i < sampleLabels.size(); i++) {
                int timeLabel = labelList.get(i);
                IntRange columns = sampleLabels.get(timeLabel);
                Sample sample = createSample(timeLabel, timeUnit, cond);
                cond.add(sample);
                for (int colIndex : columns) {
                    NumericMeasurement nm = createMeasurement(colIndex, rowIndex, sample, td);
                    if (nm != null)
                        sample.add(nm);
                }
            }
        }

        CpdPwayGraph matrixGraph = (CpdPwayGraph)MappingManager.getMatrixView().getGraph();
        List<Node> nodelist = matrixGraph.getNodes();
        matrixGraph.deleteAll(nodelist);
        matrixGraph.addPathways(pathways);

        return experiment;
    }

    @Override
    public ExperimentInterface getXMLDataFromExcelTable(File inFile, TableData td,
    		BackgroundTaskStatusProviderSupportingExternalCall statusProvider /*unused*/) {
        String keyword = td.getCellData(1, 1, "").toString();
        ExperimentInterface experimentInterface;
        switch (keyword) {
            case "metabolites":
                experimentInterface = csvMetabolite(td);
                break;
            case "each metabolite relative to mean control [%]":
                experimentInterface = parseMetabolitesLegacy(td);
                break;
            default:
                MainFrame.getInstance().showMessageDialog("Unsupported file format.");
                experimentInterface = null;
                break;
        }
        return experimentInterface;
    }


    private ExperimentInterface parseMetabolitesLegacy(TableData td) {

        // the TableData object is a doubly nested hashmap column -> row -> cell
        ExperimentInterface experiment = new Experiment();
        ArrayList<DataPathway> pathways= new ArrayList<>();
        ArrayList<DataPathway> pathwayInfos = new ArrayList<>(); // will be used to obtain the data from the pathway table below

        // Parse Excel file
        for (int rowIndex = 3; rowIndex <= td.getMaximumRow(); rowIndex++) {

            // do not continue reading rows if a row with no substance name is encountered.
            if (td.getCellData(substanceColIndex, rowIndex, "").toString().length() < 1) {
        		if(td.getCellData(substanceColIndex, rowIndex + 2, "").toString().equals("PATHWAY")){

	            	for (int row = rowIndex + 3; row <= td.getMaximumRow(); row++) {
	            		String name = td.getCellData(1, row, "").toString();
	            		String superPw = td.getCellData(2, row, "").toString();
	            		String scoreString = td.getCellData(3, row, "").toString();
	            		
	            		DataPathway dpw = new DataPathway(name);
	            		dpw.setSuperPathway(superPw);
	            		if(scoreString != null && scoreString.length() > 0) {
	            			double score = Double.parseDouble(scoreString);
	            			dpw.setScore(score);
	            		}
	            		
	            		pathwayInfos.add(dpw);
	            	}
        		}else {
        			break;
        		}
    		}

            // create and attach substance
            SubstanceWithPathways subst = (SubstanceWithPathways) createSubstance(rowIndex, td);
            if(!subst.getName().equals("no name")) {            // when reloading a saved work in progress the dummy substance from creating CpdPwayGraph comes through
            	experiment.add(subst);
            }

            String allPathways = (String) td.getCellData(3, rowIndex, "");
            String[] allPathwaysArray = allPathways.split(";");


            for(int i = 0; i < allPathwaysArray.length; i++) {
            	DataPathway pw =  new DataPathway(allPathwaysArray[i]);	
            
            	if(i == 0) {
            		pw.setSuperPathway((String) td.getCellData(2, rowIndex, ""));
            	}
            	
            	 if(!pathways.contains(pw)){
                     pathways.add(pw);
            	 }
            	 else {
            		 String superPw = pw.getSuperPathway();
            		 if(superPw != null) { //.length() > 1) {
            			 pathways.get(pathways.indexOf(pw)).setSuperPathway(pw.getSuperPathway());
            		 }
            	 }
            	 
            	 pathways.get(pathways.indexOf(pw)).addSubstance(subst);	// assign substance to pathway
            	 subst.addPathway(pw);																// assign pathway to substance
            }
    		
            // only have one single condition in this case
            Condition cond = new Condition(subst);
            subst.add(cond); // attach (yet empty) condition to substance

            // next level are the samples -- these correspond to the time steps in the metabolon file.
            // create and attach these to the condition
            for (Map.Entry<Integer, IntRange> epoch : epochs.entrySet()) {
                Integer time = epoch.getKey();
                IntRange colRange = epoch.getValue();
                Sample sample = createSample(time, "hour", cond);

                cond.add(sample);

                // for each sample, we have several measurements (replicas)
                // create and attach these to the samples
                for (int colIndex : colRange) {
                    NumericMeasurement nm = createMeasurement(colIndex, rowIndex, sample, td);
                    if (nm != null) {
                        sample.add(nm);
                    }
                }
            }
        } // Parse Excel file
     
    // transfer infos from pathway table to the matrix pathways
		for(DataPathway dpw: pathways) {
			for(DataPathway infoPW: pathwayInfos) {
				if(dpw.equals(infoPW)) {
					dpw.setSuperPathway(infoPW.getSuperPathway());
					dpw.setScore(infoPW.getScore());
				}
			}
			//System.out.println(dpw.getTitle() + dpw.getSuperPathway() + dpw.getScore());
		}

        CpdPwayGraph matrixGraph = (CpdPwayGraph)MappingManager.getMatrixView().getGraph();
        List<Node> nodelist = matrixGraph.getNodes();
        matrixGraph.deleteAll(nodelist);
    
        matrixGraph.addPathways(pathways);
    
        return experiment;
    }

    private Substance createSubstance(int rowIndex, TableData td) {
        SubstanceWithPathways s = new SubstanceWithPathways();
        String allNames = (String) td.getCellData(this.substanceColIndex, rowIndex, "no name");
        String[] allNamesArray = allNames.split(";");
        String[] allNamesCorrectedArray = new String[allNamesArray.length];
        for(int i = 0; i < allNamesArray.length; i++){
        	allNamesCorrectedArray[i] = allNamesArray[i].trim();
        }
        
        s.setName(allNamesCorrectedArray[0]);
        s.setAlternativeNames(new HashSet<>(Arrays.asList(allNamesCorrectedArray)));
        
        // in a `Substance`, alternative IDs, or "synonyms" are only indexed by an int.
        for (Map.Entry<Integer, Integer> entry : alternateIdsCols.entrySet()) {
            int altIdColIndex = entry.getValue();
            int synonymIndex = entry.getKey();
            Object altIdCellData = td.getCellData(altIdColIndex, rowIndex, "");

            String altId = "";
            if (altIdCellData instanceof Double) {
                int i = ((Double) altIdCellData).intValue();
                if (i > 0) {
                    altId = Integer.toString(i);
                }
            }
            if (altIdCellData instanceof String) {
                altId = (String) altIdCellData;
            }

            String dbName = "";
            switch (synonymIndex) {
                case 0:
                    dbName = "CAS";
                    break;
                case 1:
                    dbName = "PUBCHEM";
                    break;
                case 2:
                    dbName = "CHEMSPIDER";
                    break;
                case 3:
                    dbName = "KEGG";
                    break;
                case 4:
                    dbName = "HMDB";
                    break;
            }
            s.setDbId(dbName, altId);
        }
        return s;
    }

    private Sample createSample(Integer time, String timeUnit, Condition parent) {
        Sample sample = new Sample(parent);
        sample.setTime(time);
        sample.setTimeUnit(timeUnit);
        return sample;
    }

    private NumericMeasurement createMeasurement(int colIndex, int rowIndex, Sample parent, TableData td) {
        NumericMeasurement m = new NumericMeasurement(parent);
        m.setReplicateID(colIndex);
        if((td.getCellData(colIndex, rowIndex, "").toString()).equals("")) {	       // if((td.getCellData(colIndex, rowIndex, this.measurementEmptyValue).toString()).equals("")) {

        	return null;
        }
        m.setValue(Double.parseDouble(td.getCellData(colIndex, rowIndex, 0).toString()));
        m.setUnit("foldchange");
        return m;
    }
}
