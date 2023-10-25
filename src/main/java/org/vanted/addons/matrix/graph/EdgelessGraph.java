package org.vanted.addons.matrix.graph;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.AbstractGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElementNotFoundException;
import org.graffiti.graph.Node;

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a Graph without any edges.
 * This class rudimentarily implements edge-related methods.
 *
 * @author Benjamin Moser.
 */
public abstract class EdgelessGraph extends AbstractGraph {
    EdgelessGraph(CollectionAttribute coll) {
        super(coll);
    }

    EdgelessGraph() {
        super();
    }

    @Override
    protected Edge doAddEdge(Node source, Node target, boolean directed) throws GraphElementNotFoundException {
        throw new InvalidOperationException("Cannot add an edge to an edgeless graph");
    }

    @Override
    protected Edge doAddEdge(Node source, Node target, boolean directed, CollectionAttribute col) throws GraphElementNotFoundException {
        // refer to more extensive definition
        return doAddEdge(source, target, directed);
    }

    @Override
    protected void doDeleteEdge(Edge e) throws GraphElementNotFoundException {
        // no-op.
    }

    /**
     * Note that the AbstractGraph default implementation makes use of getEdges for this.
     *
     * @return always 0
     */
    @Override
    public int getNumberOfEdges() {
        return 0;
    }

    @Override
    public int getNumberOfDirectedEdges() {
        return 0;
    }

    @Override
    public int getNumberOfUndirectedEdges() {
        return 0;
    }

    @Override
    public Collection<Edge> getEdges() {
        return new HashSet<>(); // empty set
    }
}
