package org.vanted.addons.matrix.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

import org.AttributeHelper;
import org.vanted.addons.matrix.utils.JCollapsablePanel;

/**
 * @author Philipp Eberhard
 *
 */
public class ButtonRestoreWindow extends JFrame{

	static class SelectionButton extends JButton {
		private final MatrixLabelButton buttonReference;
		SelectionButton(String label, MatrixLabelButton reference) {
			super(label);
			buttonReference = reference;
			this.setBorder(BorderFactory.createLineBorder(Color.white));
		}

		public void setSelected(boolean selected) {
			super.setSelected(selected);
			if(!selected) {
				this.setBorder(BorderFactory.createLineBorder(Color.white));
			}
			else {
				this.setBorder(BorderFactory.createLineBorder(Color.black));
			}
		}

		MatrixLabelButton getAssociatedButton() {
			return buttonReference;
		}
	}

	private static final long serialVersionUID = 1L;

	private final ArrayList<SelectionButton> selectedButtons = new ArrayList<>();
	
	/**
	 * external Frame to display buttons deleted from the matrix
	 * they are shown according to the categories in the matrix (if they are set)
	 * single buttons or whole categories can be chosen to be restored to the matrix
	 */
	public ButtonRestoreWindow(MatrixGraphPanel mgPanel) {
		super();
		JRootPane root = new JRootPane();
		root.setLayout(new BorderLayout());
		
		//build panels
		JPanel pwContentPanel = new JPanel();
		JScrollPane pwPanel = new JScrollPane(pwContentPanel);
		pwPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pwPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		pwContentPanel.setLayout(new BoxLayout(pwContentPanel, BoxLayout.Y_AXIS));
    	JLabel pathways = new JLabel("Pathways");
		pathways.setFont(new Font(null, Font.BOLD, 16));
		pwPanel.setColumnHeaderView(pathways);
		
	    JPanel cpdContentPanel = new JPanel();
		JScrollPane cpdPanel = new JScrollPane(cpdContentPanel);
		cpdPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		cpdPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		cpdContentPanel.setLayout(new BoxLayout(cpdContentPanel, BoxLayout.Y_AXIS));
    	JLabel compounds = new JLabel("Compounds");
		compounds.setFont(new Font(null, Font.BOLD, 16));
		cpdPanel.setColumnHeaderView(compounds);
		    
		//get data from matrix
		ArrayList<MatrixLabelButton> hiddenButtons = mgPanel.getDeletedButtons();
		
        //add buttons
		addCatsAndButtons(hiddenButtons, pwContentPanel, cpdContentPanel);
		JButton restoreButton = new JButton("restore Buttons");
		restoreButton.setForeground(Color.BLUE);
		restoreButton.addActionListener(e -> {
			for(SelectionButton sb: selectedButtons) {
				mgPanel.invertVisible(sb.getAssociatedButton());
				(sb.getParent()).remove(sb);
			}
			selectedButtons.clear();
			root.updateUI();
			this.pack();
		});
		root.add(restoreButton, BorderLayout.SOUTH);
		
		//finish
		root.add(pwPanel, BorderLayout.WEST);
		root.add(cpdPanel, BorderLayout.EAST);
		this.setRootPane(root);
		this.pack();
}

	private void addCatsAndButtons(ArrayList<MatrixLabelButton> hiddenButtons,
								   JPanel pathwayPanel, JPanel compoundPanel) {


		MouseAdapter buttonMA = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				SelectionButton button = (SelectionButton) e.getSource();
				if(button.isSelected()) {
					selectedButtons.remove(button);
				}
				else {
					selectedButtons.add(button);
				}
				button.setSelected(!button.isSelected());
			}};

		HashMap<String, ArrayList<MatrixLabelButton>> pathwayCategoryMap = new HashMap<>();
		HashMap<String, ArrayList<MatrixLabelButton>> compoundCategoryMap = new HashMap<>();
		ArrayList<String> pathwayCategories = new ArrayList<>();
		ArrayList<String> compoundCategories = new ArrayList<>();
		for (MatrixLabelButton mlButton : hiddenButtons) {
			String category = mlButton.getLabelNode().getStringProperty(null);
			if (category == null)
				category = "no category set";
			if (mlButton.isPathway()) {
				if (!pathwayCategoryMap.containsKey(category)) {
					pathwayCategories.add(category);
					pathwayCategoryMap.put(category, new ArrayList<>());
				}
				pathwayCategoryMap.get(category).add(mlButton);
			}
			else {
				if (!compoundCategoryMap.containsKey(category)) {
					compoundCategories.add(category);
					compoundCategoryMap.put(category, new ArrayList<>());
				}
				compoundCategoryMap.get(category).add(mlButton);
			}
		}
		addButtons(pathwayCategories, pathwayCategoryMap, buttonMA, pathwayPanel);
		addButtons(compoundCategories, compoundCategoryMap, buttonMA, compoundPanel);
	}

	private void addButtons(ArrayList<String> categories,
							HashMap<String, ArrayList<MatrixLabelButton>> categoryMap,
							MouseAdapter buttonMA,
							JPanel panel) {
		for (String category : categories) {
			JPanel catPanel = new JPanel();
			catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.Y_AXIS));
			for (MatrixLabelButton mlButton : categoryMap.get(category)) {
				SelectionButton b = new SelectionButton(AttributeHelper.getLabel(mlButton.getLabelNode(), ""), mlButton);
				b.addMouseListener(buttonMA);
				b.setPreferredSize(new Dimension(150, 30));
				b.setMaximumSize(new Dimension(150, 30));
				b.setMinimumSize(new Dimension(150, 30));
				catPanel.add(b);
			}
			JButton selectAll = new JButton("select all");
			selectAll.addActionListener(l -> {
				Component[] comp = catPanel.getComponents();
				for (Component c : comp) {
					if (c instanceof SelectionButton) {
						if (((SelectionButton) c).isSelected()) {
							selectedButtons.remove((SelectionButton) c);
						} else {
							selectedButtons.add((SelectionButton) c);
						}
						((SelectionButton) c).setSelected(!((SelectionButton) c).isSelected());

					}
				}
			});
			JCollapsablePanel colPanel = new JCollapsablePanel(category, catPanel, selectAll);
			colPanel.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					pack();
				}
			});
			panel.add(colPanel);
		}
	}
}


