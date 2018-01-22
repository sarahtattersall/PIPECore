package uk.ac.imperial.pipe.layout;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;
import uk.ac.imperial.pipe.models.petrinet.*;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class Layout {

    /**
     * When items are aligned to (0,0) this is the layout offset we want
     */
    private static final int LAYOUT_OFFSET = 20;

    /**
     * Layout the graph in a hierarchical manner
     *
     * @param petriNet       Petri net to lay out
     * @param interRankCell  inter rank cell spacing
     * @param interHierarchy inter hierarchy spacing
     * @param parallelEdge   parallel edge spacing
     * @param intraCell      intra cell spacing
     * @param orientation SwingConstants.NORTH, SwingConstants.EAST, SwingConstants.SOUTH or SwingConstants.WEST
     */
    public static void layoutHierarchical(PetriNet petriNet, int interRankCell, int interHierarchy, int parallelEdge,
            int intraCell, int orientation) {
        mxGraph graph = initialiseGraph(petriNet);
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, orientation);
        layout.setInterRankCellSpacing(interRankCell);
        layout.setInterHierarchySpacing(interHierarchy);
        layout.setParallelEdgeSpacing(parallelEdge);
        layout.setIntraCellSpacing(intraCell);
        layout.execute(graph.getDefaultParent());
        layoutPetriNet(graph, petriNet);
    }

    /**
     * @param petriNet
     * @return xmGraph with original Petri net components and their layout
     */
    private static mxGraph initialiseGraph(PetriNet petriNet) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        Map<String, Object> objectMap = new HashMap<>();

        try {

            for (Place place : petriNet.getPlaces()) {
                objectMap.put(place.getId(), graph
                        .insertVertex(parent, place.getId(), place.getId(), 0, 0, Place.DIAMETER, Place.DIAMETER));

            }

            for (Transition transition : petriNet.getTransitions()) {
                objectMap.put(transition.getId(), graph.insertVertex(parent, transition.getId(), transition
                        .getId(), 0, 0, Transition.TRANSITION_WIDTH, Transition.TRANSITION_HEIGHT));
            }

            for (Arc<? extends Connectable, ? extends Connectable> arc : petriNet.getArcs()) {
                Object source = objectMap.get(arc.getSource().getId());
                Object target = objectMap.get(arc.getTarget().getId());
                graph.insertEdge(parent, arc.getId(), arc.getId(), source, target);
            }
        } finally {
            graph.getModel().endUpdate();
        }
        return graph;
    }

    /**
     * Layout the Petri net with the same components from the graph
     *
     * @param graph
     * @param petriNet
     */
    private static void layoutPetriNet(mxGraph graph, PetriNet petriNet) {
        double minX = 0;
        double minY = 0;
        Map<String, Point> points = new HashMap<>();

        for (Object o : graph.getChildVertices(graph.getDefaultParent())) {
            mxCell cell = (mxCell) o;
            mxGeometry geometry = cell.getGeometry();
            points.put(cell.getId(), geometry.getPoint());
            if (geometry.getX() < minX) {
                minX = geometry.getX();
            }
            if (geometry.getY() < minY) {
                minY = geometry.getY();
            }
        }

        for (Transition transition : petriNet.getTransitions()) {
            Point point = points.get(transition.getId());
            transition.setX((int) (point.getX() + Math.abs(minX) + LAYOUT_OFFSET));
            transition.setY((int) (point.getY() + Math.abs(minY) + LAYOUT_OFFSET));
        }

        for (Place place : petriNet.getPlaces()) {
            Point point = points.get(place.getId());
            place.setX((int) (point.getX() + Math.abs(minX) + LAYOUT_OFFSET));
            place.setY((int) (point.getY() + Math.abs(minY) + LAYOUT_OFFSET));
        }
    }

    /**
     * Lays out the Petri net with a graph layout
     *
     * @param petriNet to lay out
     * @param forceConstant    force constant by which the attractive forces are divided. A good default is 50.
     * @param minDistanceLimit minimum distance limit. A good default is 20.
     */
    public static void layoutOrganic(PetriNet petriNet, int forceConstant, int minDistanceLimit) {
        mxGraph graph = initialiseGraph(petriNet);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
        layout.setForceConstant(forceConstant);
        layout.setMinDistanceLimit(minDistanceLimit);
        layout.execute(graph.getDefaultParent());
        layoutPetriNet(graph, petriNet);
    }
}
