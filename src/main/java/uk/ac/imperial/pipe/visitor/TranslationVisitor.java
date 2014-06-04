package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.*;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

/**
 * Translates moveable PetriNetComponents by a given amount
 */
public final class TranslationVisitor
        implements ArcVisitor, ArcPointVisitor, PlaceVisitor, TransitionVisitor, AnnotationVisitor {
    /**
     * Vector  to add to components to translate them
     */
    private final Point translation;

    /**
     * Selected components to translate
     */
    private final Collection<PetriNetComponent> selected;

    /**
     * Constructor
     *
     * @param translation vector with the amount to move the components by
     * @param selected    components to translate
     */
    public TranslationVisitor(Point translation, Collection<PetriNetComponent> selected) {
        this.translation = translation;
        this.selected = selected;
    }


    /**
     * Moves the place by the translation vector amount
     *
     * @param place
     */
    @Override
    public void visit(Place place) {
        place.setX(place.getX() + translation.x);
        place.setY(place.getY() + translation.y);

    }

    /**
     * Moves the transition by the translation vector amount
     *
     * @param transition
     */
    @Override
    public void visit(Transition transition) {
        transition.setX(transition.getX() + translation.x);
        transition.setY(transition.getY() + translation.y);

    }

    /**
     * Moves the arc point by the translation vector amount
     *
     * @param arcPoint
     */
    @Override
    public void visit(ArcPoint arcPoint) {
        double x = arcPoint.getX() + translation.getX();
        double y = arcPoint.getY() + translation.getY();
        arcPoint.setPoint(new Point2D.Double(x, y));
    }

    /**
     * Moves the annotation by the translation vector amount
     *
     * @param annotation
     */
    @Override
    public void visit(Annotation annotation) {
        annotation.setX(annotation.getX() + (int) translation.getX());
        annotation.setY(annotation.getY() + (int) translation.getY());
    }

    /**
     * Moves arc points in the arc by the translate vector amount
     *
     * @param inboundArc
     */
    @Override
    public void visit(InboundArc inboundArc) {
        visitArc(inboundArc);
    }

    /**
     * Moves arc points in the arc by the translate vector amount
     *
     * @param outboundArc
     */
    @Override
    public void visit(OutboundArc outboundArc) {
        visitArc(outboundArc);
    }


    /**
     * Visits an arc moving its arc points by the translate vector amount
     * @param arc
     * @param <T>
     * @param <S>
     */
    private <T extends Connectable, S extends Connectable> void visitArc(Arc<S, T> arc) {
        if (selected.contains(arc.getSource()) && selected.contains(arc.getTarget())) {
            List<ArcPoint> points = arc.getArcPoints();
            for (ArcPoint arcPoint : points) {
                Point2D point = arcPoint.getPoint();
                Point2D newPoint =
                        new Point2D.Double(point.getX() + translation.getX(), point.getY() + translation.getY());
                arcPoint.setPoint(newPoint);
            }
        }
    }
}
