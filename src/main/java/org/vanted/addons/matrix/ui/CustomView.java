package org.vanted.addons.matrix.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.MessageListener;
import org.graffiti.plugin.view.View;
import org.vanted.addons.matrix.graph.MatrixGraph;

import org.vanted.addons.matrix.mapping.MappingManager;

/**
 * @author ph_e6
 *
 */
public class CustomView extends JComponent implements View {
//fields
	private static final long serialVersionUID = 1L;
	/** The current zoom for this view. - AbstractView */
	protected AffineTransform zoom = View.NO_ZOOM;
	/** The autoscroll margin. */
	protected static final int autoscrollMargin = 20;
	/** The autoresize margin. */
	protected static final int autoresizeMargin = 50;

	/** The insets of the autoscroll. */
	protected static final Insets autoscrollInsets = new Insets(0, 0, 0, 0);

	private static MatrixGraph currentGraph;
	
//constructors	
	public CustomView() {
		// super();
		setLayout(new BorderLayout());

		MatrixGraphPanel panel = new MatrixGraphPanel();
		this.add(panel);
		
	}

//inherited methods
	@Override
	public void postEdgeAdded(GraphEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postEdgeRemoved(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postGraphCleared(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postNodeAdded(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postNodeRemoved(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preEdgeAdded(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preEdgeRemoved(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preGraphCleared(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preNodeAdded(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preNodeRemoved(GraphEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionStarted(TransactionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postUndirectedEdgeAdded(NodeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postUndirectedEdgeRemoved(NodeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preUndirectedEdgeAdded(NodeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preUndirectedEdgeRemoved(NodeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postDirectedChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postEdgeReversed(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSourceNodeChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postTargetNodeChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preDirectedChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preEdgeReversed(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSourceNodeChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preTargetNodeChanged(EdgeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postAttributeAdded(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postAttributeChanged(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postAttributeRemoved(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preAttributeAdded(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preAttributeChanged(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preAttributeRemoved(AttributeEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * copied from AbstractView
	 * @see java.awt.dnd.Autoscroll#getAutoscrollInsets()
	 */
	public Insets getAutoscrollInsets() {
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();

		autoscrollInsets.top = rect.y + autoscrollMargin;
		autoscrollInsets.left = rect.x + autoscrollMargin;
		autoscrollInsets.bottom = size.height - (rect.y + rect.height) + autoscrollMargin;
		autoscrollInsets.right = size.width - (rect.x + rect.width) + autoscrollMargin;

		return autoscrollInsets;
	}

	/**
	 * copied from AbstractView
	 * @see java.awt.dnd.Autoscroll#autoscroll(Point)
	 */
	public void autoscroll(Point location) {
		int top = 0;
		int left = 0;
		int bottom = 0;
		int right = 0;

		Point origLoc = (Point) location.clone();
		zoom.transform(location, location);

		// location.setLocation(location.getX() * ((Point2D) zoom).getX(),
		// location.getY() * ((Point2D) zoom).getY());
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();

		int bottomEdge = rect.y + rect.height;
		int rightEdge = rect.x + rect.width;

		if (((location.y - rect.y) <= autoscrollMargin) && (rect.y > 0)) {
			top = autoscrollMargin;
		}

		if (((location.x - rect.x) <= autoscrollMargin) && (rect.x > 0)) {
			left = autoscrollMargin;
		}

		autoresize(origLoc);

		if (((bottomEdge - location.y) <= autoscrollMargin) && (bottomEdge < size.height)) {
			bottom = autoscrollMargin;
		}

		if (((rightEdge - location.x) <= autoscrollMargin) && (rightEdge < size.width)) {
			right = autoscrollMargin;
		}

		rect.x += (right - left);
		rect.y += (bottom - top);

		scrollRectToVisible(rect);
	}

	/**
	 *copied from AbstractView
	 */
	@Override
	public void zoomChanged(AffineTransform newZoom) {
		this.zoom = newZoom;
		repaint();
		
		//version of GraffitiView (super=AbstractView):
		//super.zoomChanged(newZoom);
		//adjustPreferredSize(true);
	}

	/**
	 *copied from AbstractView
	*/
	public AffineTransform getZoom() {
		return this.zoom;
	}

	/**
	 * copied from GraffitiView
	 * @param e
	 * @return
	 */
	MouseEvent getZoomedEvent(MouseEvent e) {
		Point2D invZoomedPoint = null;
		try {
			invZoomedPoint = zoom.inverseTransform(e.getPoint(), null);
			MouseEvent newME = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
					(int) (invZoomedPoint.getX()), (int) (invZoomedPoint.getY()), e.getClickCount(),
					e.isPopupTrigger());

			return newME;
		} catch (NoninvertibleTransformException nite) {
			// when setting the zoom, it must have been checked that
			// the transform is invertible
			return e;
		}
	}
	
	/**
	 *copied from AbstractView
	*/
	public boolean redrawActive() {
		return false;
	}

	@Override
	public void setAttributeComponentManager(AttributeComponentManager acm) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<?, ?> getComponentElementMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphElementComponent getComponentForElement(GraphElement ge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<AttributeComponent> getAttributeComponentsForElement(GraphElement ge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGraph(Graph graph) {
			this.currentGraph = (MatrixGraph) graph;
	}

	@Override
	public Graph getGraph() {
		return currentGraph;
	}

	/**
	 *copied from FAstView
	 */
	public JComponent getViewComponent() {
		return this;
	}

	@Override
	public String getViewName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 *default (inherited from View): false -> makes more sense for our needs.
	 *we want only the matrix part in a scrollPane
	 */
	public boolean putInScrollPane() {
		return false;
	}

	@Override
	public void attributeChanged(Attribute attr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMessageListener(MessageListener ml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		MappingManager.reset();
	}

	@Override
	public void completeRedraw() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMessageListener(MessageListener ml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void repaint(GraphElement ge) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getViewToolbarComponentTop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getViewToolbarComponentBottom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getViewToolbarComponentLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getViewToolbarComponentRight() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getViewToolbarComponentBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closing(AWTEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 *copied from FAstView
	 */
	public boolean worksWithTab(InspectorTab tab) {
		return true;
	}
//custom methods
	/**
	 * copied from AbstractView
	 * Resizes the panel dynamically so that enough drawing space is available.
	 * 
	 * @param location
	 *            the point that should be checked for border conflicts
	 */
	public void autoresize(Point location) {
		boolean resize = false;
		Dimension size = getSize();

		zoom.transform(location, location);

		if ((location.y + autoresizeMargin) > size.height) {
			size.height = Math.max(location.y, size.height) + 100;
			resize = true;
		}

		if ((location.x + autoresizeMargin) > size.width) {
			size.width = Math.max(location.x, size.width) + 100;
			resize = true;
		}

		if (resize) {
			this.setSize(size);
			this.setPreferredSize(size);
			// revalidate();
			invalidate();
		}
	}
}