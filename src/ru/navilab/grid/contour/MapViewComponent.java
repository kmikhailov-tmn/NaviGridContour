package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;

/**
 * Created by: Mikhailov_KG
 * Date: 24.07.2020
 */
public class MapViewComponent extends JComponent {
    private static final double BORDER = 0.9;
    private List<Geometry> geomList = new ArrayList<>();
    private boolean drawLabelFlag;

    public void setDrawLabelFlag(boolean drawLabelFlag) {
        this.drawLabelFlag = drawLabelFlag;
    }

    public boolean getDrawLabelFlag() {
        return drawLabelFlag;
    }


    static class GeomProperties {
        Color color;

        public GeomProperties(Color color) {
            this.color = color;
        }
    }
    private Map<Geometry,GeomProperties> propsMap = Collections.synchronizedMap(new Hashtable<>());

    public void add(Geometry geom, Color red) {
        GeomProperties geomProperties = new GeomProperties(red);
        propsMap.put(geom, geomProperties);
        geomList.add(geom);
        repaint();
    }

    public void remove(Geometry geometry) {
        geomList.remove(geometry);
        propsMap.remove(geometry);
        repaint();
    }


    @Override
    public void paint(Graphics g) {
        try {
            if (geomList.isEmpty()) {
                g.drawLine(0, 0, getWidth(), getHeight());
                g.drawLine(getWidth(), 0, 0, getHeight());
            } else {
                double minx = 0;
                double maxx = 0;
                double miny = 0;
                double maxy = 0;
                int i=0;
                for (Geometry geometry : geomList) {
                    Coordinate[] coordinates = geometry.getCoordinates();
                    for (Coordinate coordinate : coordinates) {
                        if (i++ == 0) {
                            maxx = minx = coordinate.x;
                            maxy = miny = coordinate.y;
                        } else {
                            if (minx > coordinate.x) minx = coordinate.x;
                            if (maxx < coordinate.x) maxx = coordinate.x;
                            if (miny > coordinate.y) miny = coordinate.y;
                            if (maxy < coordinate.y) maxy = coordinate.y;
                        }
                    }
                }
                double dy = maxy - miny;
                double sy = getHeight()* BORDER / dy;
                double dx = maxx - minx;
                double sx = getWidth()*BORDER / dx;
                Graphics2D g2 = (Graphics2D) g;
                AffineTransform transform = g2.getTransform();
                g2.translate(getWidth()*(1-BORDER)/2, getHeight()*(1-BORDER)/2);
                g2.scale(sx, sy);
                g2.translate(-minx, -miny);

                for (Geometry geometry : geomList) {
                    if (geometry instanceof MultiPolygon) {
                        MultiPolygon mpolygon = (MultiPolygon) geometry;
                        int numGeometries = mpolygon.getNumGeometries();
                        for (int j = 0; j < numGeometries; j++) {
                            Geometry geometryN = mpolygon.getGeometryN(j);
                            if (geometryN instanceof Polygon) {
                                Polygon p = (Polygon) geometryN;
                                drawPoly(g2, p, propsMap.get(mpolygon));
                            }
                        }
                    }
                    if (geometry instanceof Polygon) {
                        Polygon polygon = (Polygon) geometry;
                        drawPoly(g2, polygon, propsMap.get(polygon));
                    } else if (geometry instanceof Point) {
                        Point point = (Point) geometry;
                        drawPoint(g2, point);
                    }
                }
                g2.setTransform(transform);
            }
        } catch (RuntimeException e) {
            System.err.println("error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void drawPoint(Graphics2D g2, Point point) {
        Coordinate c = point.getCoordinate();
        AffineTransform transform = g2.getTransform();
        g2.translate(c.x, c.y);
        g2.setColor(propsMap.get(point).color);
        g2.drawRect(-100, -100, 200,200);
        g2.setTransform(transform);
    }

    private void drawPoly(Graphics2D g2, Polygon geom, GeomProperties geomProperties) {
        Polygon polygon = geom;
        drawExterior(g2, polygon, geomProperties);

        Stroke savedStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2, new float[]{2}, 0));
        int numInteriorRing = polygon.getNumInteriorRing();
        for (int i = 0; i < numInteriorRing; i++) {
            LineString interiorRingN = polygon.getInteriorRingN(i);
            CoordinateSequence coordinateSequence = interiorRingN.getCoordinateSequence();
            drawSeq(g2, coordinateSequence, Color.BLACK);
        }
        g2.setStroke(savedStroke);
    }

    private void drawExterior(Graphics2D g2, Polygon polygon, GeomProperties geomProperties) {
        LineString exteriorRing = polygon.getExteriorRing();
        CoordinateSequence coordinateSequence = exteriorRing.getCoordinateSequence();
        drawSeq(g2, coordinateSequence, geomProperties.color);
    }

    private void drawSeq(Graphics2D g2, CoordinateSequence coordinateSequence, Color color) {
        int psize = coordinateSequence.size();
        GeneralPath gp = new GeneralPath();
        for (int i = 0; i < psize; i++) {
            Coordinate c = coordinateSequence.getCoordinate(i);
            if (i == 0) gp.moveTo(c.x, c.y);
            else gp.lineTo(c.x, c.y);
        }
        g2.setColor(color);
        g2.fill(gp);
//        g2.setColor(Color.yellow);
        g2.draw(gp);
        if (drawLabelFlag) {
            for (int i = 0; i < psize; i++) {
                Coordinate coordinate = coordinateSequence.getCoordinate(i);
                AffineTransform tr = g2.getTransform();
                g2.translate(coordinate.x, coordinate.y);
                g2.scale(10d, 10d);
                g2.drawString("" + i, 0, 0);
                g2.setTransform(tr);
            }
        }
    }
}
