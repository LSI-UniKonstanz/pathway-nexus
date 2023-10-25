package org.vanted.addons.matrix.pinBoard;

import java.util.ArrayList;
import javax.swing.BoxLayout;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.ui.MatrixGraphPanel;

public class PinBoardTab extends InspectorTab{
	private final PinBoardPanel pinBoard;

	public PinBoardTab(ArrayList<CompoundTextNode> selectedNodes, MatrixGraphPanel mgp) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		pinBoard = new PinBoardPanel(selectedNodes, mgp, this);
		this.add(pinBoard);
		this.revalidate();
		this.repaint();
	}
	
	@Override
	public boolean visibleForView(View v) {
		return true;
	}

	@Override
	public String getTitle() {
	        return "Pin Board";
    }

	public PinBoardPanel getPanel() {
		return this.pinBoard;
	}

	public void close() {
		this.getParent().remove(this);
	}
}
