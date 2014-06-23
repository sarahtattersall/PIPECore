package uk.ac.imperial.pipe.layout;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.models.petrinet.*;

import java.awt.Color;

public class Layout {

    public static void layout(PetriNet petriNet) {
        Graph graph = new SingleGraph("Tutorial 1");

        for (Place place : petriNet.getPlaces()) {
            graph.addNode(place.getId());
        }

        for (Transition transition : petriNet.getTransitions()) {
            graph.addNode(transition.getId());
        }

        for (Arc<? extends Connectable, ? extends Connectable> arc : petriNet.getArcs()) {
            graph.addEdge(arc.getId(), arc.getSource().getId(), arc.getTarget().getId());
        }

//        Viewer v = graph.display(true);
//        Thread.sleep(1000);

        SpringBox layout = new SpringBox();

        Toolkit.computeLayout(graph);

//        for (Node node : graph.getEachNode()) {
//            double[] pos = Toolkit.nodePosition(node);
//            System.out.println(node.getId());
//            System.out.println("x: " + Math.abs(pos[0]*100) + "y "+ Math.abs(pos[1]*100));
//        }

        for (Place place : petriNet.getPlaces()) {
            double[] pos = Toolkit.nodePosition(graph, place.getId());
            int x = getLocation(pos[0]);
            int y = getLocation(pos[1]);
            place.setX(x);
            place.setY(y);
        }

        for (Transition transition : petriNet.getTransitions()) {
//                  double[] pos = Toolkit.nodePosition(graph, place.getId());
            double[] pos = Toolkit.nodePosition(graph, transition.getId());
            int x = getLocation(pos[0]);
            int y = getLocation(pos[1]);
            transition.setX(x);
            transition.setY(y);
        }

    }

    private static int getLocation(double x) {
        return (int)Math.round(Math.abs(x * 100));
    }


    public static void main(String[] args) throws InterruptedException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").tokens()).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").tokens());
//
        layout(petriNet);

//
//        Graph g = new AdjacencyListGraph("g");
//        g.addNode("A");
//        g.addNode("B");
//        g.addEdge("AB", "A", "B");
//
//        Viewer v = g.display(true);
//
//        Thread.sleep(1000);
//
//        double[] d = org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition(v.getGraphicGraph(),"A");
//        System.out.println(d[0] + ", " + d[1] + ", " + d[2]);

    }
}
