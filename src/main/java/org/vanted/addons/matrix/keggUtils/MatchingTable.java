package org.vanted.addons.matrix.keggUtils;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.graffiti.util.Pair;

public class MatchingTable extends JTable {
	private final DefaultTableModel tableModel = new DefaultTableModel();
	private final String[] columnHeads = new String[] {"", "IDs", "names", "DB IDs", "DB names"};
	
	private int slctdLeftRow = -1;
	private int slctdRightRow = -1;
	
//constructors
	public MatchingTable(){
		super();
		
		tableModel.setColumnCount(5);
		this.setModel(tableModel);
		this.setCellSelectionEnabled(true);
		
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableColumnModel tcm = this.getColumnModel();
	
		for(int i = 0; i < columnHeads.length; i++) {
			TableColumn tc = tcm.getColumn(i);
			tc.setHeaderValue(columnHeads[i]);
		}
		//different background colors for columns and selection color
		this.setDefaultRenderer(this.getColumnClass(1),
		new DefaultTableCellRenderer() {
		    
		    public Component getTableCellRendererComponent(
		    		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		    	 Component l =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		    	 switch(col) {
		    	 case 0:
		    	 case 1:
		    		 if(row == slctdLeftRow) {
			    		 l.setBackground(Color.GREEN);
			    		 return l;
		    		 }
		    		 l.setBackground(Color.LIGHT_GRAY);
		    		 break;
		    	 case 2:
		    	 case 3:
		    		 if(row == slctdRightRow){
			    		 l.setBackground(Color.GREEN);
			    		 return l;
			    	 }
		    		 l.setBackground(new Color(0xe0e0e0));
		    		 break;
		    	default:
		    		 l.setBackground(Color.WHITE);
		    	 }
		        return l;
		    }});
		
		this.setSelectionForeground(Color.WHITE);
		
		this.sort();
	}
	
	/**
	 * makes sure that only two rows can be selected at a time
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);

		int col = this.getSelectedColumn();
		switch(col) {
			case 0:
			case 1:
				this.slctdLeftRow = this.getSelectedRow();
				break;
			case 2:
			case 3:
				this.slctdRightRow = this.getSelectedRow();
				break;
		}
		this.repaint();
	}

	/**
	 * Ensures correct rendering of selected rows when selecting the same row in both table halves.
	 * @param e ListSelectionEvent
	 */
	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
		super.columnSelectionChanged(e);

		int col = this.getSelectedColumn();
		switch(col) {
			case 0:
			case 1:
				this.slctdLeftRow = this.getSelectedRow();
				break;
			case 2:
			case 3:
				this.slctdRightRow = this.getSelectedRow();
				break;
		}
		this.repaint();
	}
	
	/**
	 * disables cell editing in this table
	 */
	public boolean editCellAt(int x, int y, EventObject e) {
		return false;
	}
	
	/**
	 * rows are sorted primarily by the ranking in the (invisible) first row
	 * then by names, then by IDs
	 */
	public void sort() {
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		 
		Comparator<String> comparator = (name1, name2) -> {
			if(!name1.equals(name2)) {
				if(name1.isEmpty()) return 10;
				if(name2.isEmpty()) return -10;
			}
			return name1.compareTo(name2);
		};
		
		sorter.setComparator(0, (Comparator<String>) (name1, name2) -> {
			Integer int1 = Integer.valueOf(name1);
			Integer int2 = Integer.valueOf(name2);
			return int1.compareTo(int2);
		});
		
		sorter.setComparator(2, comparator);
		sorter.setComparator(4, comparator);
		sorter.setComparator(1, comparator);
		sorter.setComparator(3, comparator);
		
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		
		this.setRowSorter(sorter);
		
		sorter.sort();
	}

	/**
	 * sets the data to the TableModel as a DataVector with an additional column
	 * this column contains a ranking depending on which columns of this row have actual content
	 * this additional column is then removed from the table and occurs only in the TableModel and is used for sorting
	 * @param data data to fill the table
	 */
	public void setData(String[][] data){
		String[][] newData = new String[data.length][5];
		
		for(int row = 0; row < data.length; row++) {
			int emptyStringCount = 0;
			if(data[row][0].isEmpty() && !data[row][1].isEmpty() && !data[row][2].isEmpty() && !data[row][3].isEmpty())
				emptyStringCount = 1;
			if(!data[row][0].isEmpty()&& !data[row][1].isEmpty() && data[row][2].isEmpty() && data[row][3].isEmpty())
				emptyStringCount = 2;
			if(data[row][0].isEmpty() && !data[row][1].isEmpty() && data[row][2].isEmpty() && data[row][3].isEmpty())
				emptyStringCount = 3;
			if(data[row][0].isEmpty() && data[row][1].isEmpty() && !data[row][2].isEmpty()&& !data[row][3].isEmpty())
				emptyStringCount = 4;
			if(data[row][0].isEmpty() && data[row][1].isEmpty() && data[row][2].isEmpty() && data[row][3].isEmpty())
				emptyStringCount = 5;

			System.arraycopy(data[row], 0, newData[row], 1, data[row].length);
			newData[row][0] = ((Integer) emptyStringCount).toString();
		}
		
		tableModel.setDataVector(newData, this.columnHeads);
		this.sort();
		this.removeColumn(this.getColumnModel().getColumn(0));
		
		this.getColumn("IDs").setPreferredWidth(80);
		this.getColumn("names").setPreferredWidth(300);
		this.getColumn("DB IDs").setPreferredWidth(80);
		this.getColumn("DB names").setPreferredWidth(300);
	}

	/**
	 * returns the first position pair, currently selected
	 * @return first position pair
	 */
	public Pair<String, String> getFirstSelected(){
		if(this.slctdLeftRow >= 0) {
			String firstID = (String) this.getValueAt(slctdLeftRow, 0);
			String firstName =  (String) this.getValueAt(slctdLeftRow, 1);
			return new Pair<>(firstID, firstName);
		}
		return null;
	}
	
	/**
	 * returns the second position pair, currently selected
	 * @return second position pair
	 */
	public Pair<String, String> getSecondSelected(){
		if(this.slctdRightRow >= 0) {
			String secondID = (String) this.getValueAt(slctdRightRow, 2);
			String secondName =  (String) this.getValueAt(slctdRightRow, 3);
			return new Pair<>(secondID, secondName);
		}
		return null;
	}
}
