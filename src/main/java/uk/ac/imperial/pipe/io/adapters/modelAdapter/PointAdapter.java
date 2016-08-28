package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.geom.Point2D;

/**
 * Marshalls Petri net arc points in and out of their PNML format
 */
public final class PointAdapter extends XmlAdapter<PointAdapter.AdaptedPoint, Point2D> {

    /**
     *
     * @param adaptedPoint to unmarshal
     * @return unmarshaled point
     */
    @Override
    public Point2D unmarshal(AdaptedPoint adaptedPoint) {
        return new Point2D.Double(adaptedPoint.x, adaptedPoint.y);
    }

    /**
     *
     * @param point2D to marshal
     * @return marshaled poinmt
     */
    @Override
    public AdaptedPoint marshal(Point2D point2D) {
        AdaptedPoint adaptedPoint = new AdaptedPoint();
        adaptedPoint.x = point2D.getX();
        adaptedPoint.y = point2D.getY();
        return adaptedPoint;
    }

    /**
     * Adapted point for PNML
     */
    public static class AdaptedPoint {
        /**
         * wraps the x value in a x attribute
         */
        @XmlAttribute
        public double x;

        /**
         * wraps the  y value in a y attribute
         */
        @XmlAttribute
        public double y;
    }
}
