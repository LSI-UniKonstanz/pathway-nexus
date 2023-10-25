package org.vanted.addons.matrix.utils;

import javax.swing.DefaultListSelectionModel;

/**
 * A ListSelectionModel to disable the selection functionality for JLists
 * @author Philipp Eberhard
 *
 */
public class NoSelectionModel extends DefaultListSelectionModel {

	   @Override
	   public void setAnchorSelectionIndex(final int anchorIndex) {}

	   @Override
	   public void setLeadAnchorNotificationEnabled(final boolean flag) {}

	   @Override
	   public void setLeadSelectionIndex(final int leadIndex) {}

	   @Override
	   public void setSelectionInterval(final int index0, final int index1) { }
	 
} 