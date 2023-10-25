package org.vanted.addons.matrix.utils;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A List of Strings in which the user can change single entries
 * basically it consists of a one column JTable in a JScrollPane
 * code is partly taken from this source:
 * http://www.java2s.com/Code/Java/Swing-Components/EditableListExample.htm
 * @author Philipp Eberhard
 *
 */
public class EditableList extends JScrollPane {
	private JTable table;
	private Vector dummyHeader = new Vector();
	private DefaultTableModel dm;
	
	private boolean isExtendable;
	private boolean isEditable;
	
//constructor	
	public EditableList() {
		super();
		
		dm = new DefaultTableModel();
	    dummyHeader.addElement("");
	    table  = new JTable(dm);
	    table.setShowGrid(false);
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    table.setTableHeader(null);
	    
	    this.setViewportView(table);
	    
	    if(this.isExtendable) {
		    dm.addTableModelListener(new TableModelListener() {
		    	
		    // manages insertion and deletion of rows. After a row was manipulated 
		    // it is deleted if it's empty, if it was the last row, a new row is inserted at the end
				public void tableChanged(TableModelEvent e) {
					int row = e.getFirstRow();
					if(row == dm.getRowCount() - 1) {
						if(!dm.getValueAt(row, 0).toString().equals(new String("+"))) {
							dm.insertRow(row + 1, new String[] {"+"});
						}
					}
					if(row >= 0  && dm.getValueAt(row, 0).toString().equals(new String(""))) {
							dm.removeRow(row);
					}
				}
		    });
	    }
	}
	
	public EditableList(String[] values, boolean editable, boolean extendable){
		super();
		
		this.isExtendable = extendable;
		this.isEditable = editable;
		
		dm = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return isEditable;
			    }
		};
	    dummyHeader.addElement("");
	    table  = new JTable(dm);
	    table.setShowGrid(false);
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    table.setTableHeader(null);
	    
	    this.setViewportView(table);
	    if(this.isExtendable) {
		    dm.addTableModelListener(new TableModelListener() {
		    	
		    // manages insertion and deletion of rows. After a row was manipulated 
		    // it is deleted if it's empty, if it was the last row, a new row is inserted at the end
				public void tableChanged(TableModelEvent e) {
					int row = e.getFirstRow();
					if(row == dm.getRowCount() - 1) {
						if(!dm.getValueAt(row, 0).toString().equals(new String("+"))) {
							dm.insertRow(row + 1, new String[] {"+"});
						}
					}
					if(row >= 0) {
						if( dm.getValueAt(row, 0).toString().equals(new String(""))) {
							dm.removeRow(row);
					}
					}
				}
		    });
	    }
	    
	    this.setValues(values);
	}
//methods
	
	/**
	 * converts an Array of Strings to a Vector
	 * since table accepts vectors as input
	 * @param str
	 * @return
	 */
	private Vector strArray2Vector(String[] str) {
        Vector vector = new Vector();
        for (int i = 0; i < str.length; i++) {
          Vector v = new Vector();
          v.addElement(str[i]);
          vector.addElement(v);
        }
        return vector;
      }
	
	/**
	 * replaces the Strings displayed by this list
	 * @param values
	 */
	public void setValues(String[] values) {

			if(this.isExtendable) {
				String[] valuesPlus = new String[values.length + 1];
				for(int index = 0; index < values.length; index++) {
					valuesPlus[index] = values[index];
				}
				valuesPlus[valuesPlus.length - 1] = "+";
				values = valuesPlus;
			}
			dm.setDataVector(strArray2Vector(values), dummyHeader);
	}

	/**
	 * adds the values at the end of this EditableList
	 * @param values
	 */
	public void addValues(String[] values) {
		if(this.isExtendable) {
			for(String s: values) {
				if(!Arrays.asList(this.getCurrentValues()).contains(s)){
					dm.setValueAt(s, dm.getRowCount()-1, 0);
				}
			}
		}
		else {
			for(String s: values) {
				if(!Arrays.asList(this.getCurrentValues()).contains(s)){
					dm.addRow(new String[] {s});
				}
			}
		}
		this.sort();
	}

	public void removeSelectedValues(){
		int[] rows = table.getSelectedRows();
		for(int row: rows) {
			if(!table.getValueAt(row, 0).equals("+")) {
				dm.removeRow(row);
			}
		}
	}
	
	public HashSet<String> getSelectedValuesList(){
		int[] rows = table.getSelectedRows();
		
		HashSet<String> values = new HashSet<String>();
		
		for(int row: rows) {
			values.add((String) dm.getValueAt(row, 0));
		}
		
		return values;
	}
	
	/**
	 * returns the current values of this List
	 * as an Array of Strings
	 * @return
	 */
	public String[] getCurrentValues(){
		String longString = dm.getDataVector().toString();
		String[] strings = {};
		if(longString.length() > 9) {
			longString = longString.substring(2, longString.length() - 7);
			strings = longString.split("\\], \\[");
		}
		
		return strings;
	}

	public void setSelectionMode(int listSelectionModel){
		table.setSelectionMode(listSelectionModel);
	}
	
	public void sort(){
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(dm);
		
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		
		Comparator<String> comparator = new Comparator<String>() {
			 
		    public int compare(String name1, String name2) {
		    	if(name1 != name2) {
		    		if(name1 == "") return 10;
		    		if(name2 == "") return -10;
		    		if(name1 == "+") return 10;
		    		if(name2 == "+") return -10;
		    	}
		    	return name1.compareTo(name2);
		    }
		};
	
		sorter.setComparator(0, comparator);
		
		table.setRowSorter(sorter);
		
		sorter.sort();
		
		this.updateModel();
	}
	
	public void updateModel(){
		int rowCount = table.getRowCount();
		if(this.isExtendable) {
			rowCount = rowCount - 1;
		}
		String[] values = new String[rowCount];

		for(int i = 0; i < rowCount; i++) {
			values[i] = (String) table.getValueAt(i, 0);

		}
		this.setValues(values);
	}

	
}
