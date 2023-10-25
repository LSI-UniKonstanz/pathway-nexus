package org.vanted.addons.matrix.utils;

import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.utils.EqPair;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.MapIterator;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This data structure with a bidirectional map at its heart resembles a table/matrix and provides convenient
 * access methods to columns, rows, etc.
 *
 * @author Benjamin Moser.
 */
public class Table<YLabelT, XLabelT, CellT> {
//fields
    /*
     * The HashMap implementation was chosen over the TreeBidiMap because the latter requires the elements to
     * implement `Comparable`, that is, there has to be a meaningful ordering. This is not really the case for Pairs.
     * Note that, thus, this map is without ordering.
     */
    private BidiMap<EqPair<YLabelT, XLabelT>, CellT> bidiMap = new DualHashBidiMap<>();

    /**
     * Labels are additionally kept in separate sets for ease of access.
     * This requires more housekeeping but the set of all yLabels (or of all xLabels)
     * has to be accessed quite frequently. This makes more sense than iterating over the entire
     * bidimap every time.
     */
    private HashSet<YLabelT> yLabels = new HashSet<>();
    private HashSet<XLabelT> xLabels = new HashSet<>();

    
//methods    
    public BidiMap<EqPair<YLabelT, XLabelT>, CellT> getBidiMap() {
        return this.bidiMap;
    }

   
    /**
     * Adds a cell corresponding to the labels. If a label is not present, it is inserted.
     * If both labels are present, the previous cell is replaced. Hence "upsert" as combination of "insert" and "update".
     * @param p
     * @param c
     */
    public void upsertCell(EqPair<YLabelT, XLabelT> p, CellT c) {
        this.yLabels.add(p.getFst());
    	this.xLabels.add(p.getSnd());
    	this.bidiMap.put(p, c);
    }

    public Set<YLabelT> getYLabels() {
        return this.yLabels;
    }

    public HashSet<XLabelT> getXLabels() {
        return this.xLabels;
    }

    public Set<CellT> getAllCells() {
        Set<CellT> s = new HashSet<>();
        for (Map.Entry<EqPair<YLabelT, XLabelT>, CellT> e : bidiMap.entrySet()) {
            s.add(e.getValue());
        }
        return s;
    }

    public Set<CellT> getRow(YLabelT rowIndex) {
        Set<CellT> s = new HashSet<>();
        for (XLabelT colIndex :
                this.getXLabels()) {
            s.add(bidiMap.get(new EqPair<>(rowIndex, colIndex)));
        }
        return s;
    }

    public Set<CellT> getCol(XLabelT colIndex) {
        Set<CellT> s = new HashSet<>();
        for (YLabelT rowIndex : this.getYLabels()) {
            s.add(bidiMap.get(new EqPair<>(rowIndex, colIndex)));
        }
        return s;
    }

    public int size() {
        return this.bidiMap.size();
    }

    public CellT getCell(EqPair<YLabelT, XLabelT> cellId) {
        return this.bidiMap.get(cellId);
    }
    
    /**
     * deletes all data stored in this Table
     */
    public void clear() {
    	this.bidiMap.clear();
    	this.xLabels.clear();
    	this.yLabels.clear();
    }

    public void removePathway(XLabelT pathwayNode){
    	MapIterator<EqPair<YLabelT, XLabelT>, CellT> iterator = this.bidiMap.mapIterator();
    	
    	while(iterator.hasNext()) {
    		EqPair<YLabelT, XLabelT> eqPair = iterator.next();
    		XLabelT nodeFromMap =  eqPair.getSnd();
    		if((pathwayNode).equals(nodeFromMap)) {
    			//System.out.println(((PathwayTextNode) nodeFromMap).getPathway().getTitle());
    			iterator.remove();
    			//this.bidiMap.remove(eqPair);
    		}
    	}
    
    	this.xLabels.remove(pathwayNode);
    }
}
