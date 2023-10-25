package org.vanted.addons.matrix.graph;

import org.AttributeHelper;
import org.graffiti.graph.AbstractNode;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import java.util.Collections;
import java.util.Iterator;

/**
 * A node subclass to represent a Node of a graph without any edges (such as a matrix graph).
 * This class rudimentarily implements edge-related methods.
 * cf. <code>EdgelessGraph</code>.
 *
 * @author Benjamin Moser
 */
public abstract class EdgelessNode extends AbstractNode {

    EdgelessNode(Graph graph) {
        super(graph);
        AttributeHelper.setPosition(this, 0, 0);
    }

    @Override
    public Iterator<Edge> getDirectedInEdgesIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Edge> getDirectedOutEdgesIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Edge> getEdgesIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Edge> getUndirectedEdgesIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void setGraph(Graph graph) {
        assert graph != null;
        this.graph = graph;
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int compareTo(GraphElement o) {
        return Long.compare(this.getID(), o.getID());
    }
}
