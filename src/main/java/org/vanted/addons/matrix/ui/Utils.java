package org.vanted.addons.matrix.ui;

import org.AttributeHelper;
import org.vanted.addons.matrix.graph.CellNode;
import org.vanted.addons.matrix.graph.CompoundTextNode;
import org.vanted.addons.matrix.graph.CpdPwayGraph;
import org.vanted.addons.matrix.graph.PathwayTextNode;
import org.vanted.addons.matrix.mapping.MappingManager;
import org.vanted.addons.matrix.utils.EqPair;

/**
 * @author Benjamin Moser.
 */
public class Utils {
    public static CpdPwayGraph getDummyGraph() {
        CpdPwayGraph g = new CpdPwayGraph();
      
        CompoundTextNode c1 = new CompoundTextNode(g, "dummy");
        //AttributeHelper.setPosition(c1, 0, 50);
      
        PathwayTextNode p1 = new PathwayTextNode(g, "dummy");
        //AttributeHelper.setPosition(p1, 50, 0);
       
        CellNode c11 = new CellNode(g);
        //AttributeHelper.setPosition(c11, 50, 50);
       
        g.upsertCell(new EqPair<>(c1, p1), c11);
     
        return g;
    }

    public static CpdPwayGraph getEmptyGraph() {
        return new CpdPwayGraph();
    }

}
