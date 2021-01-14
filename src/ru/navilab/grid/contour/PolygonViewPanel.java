package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PolygonViewPanel extends JComponent {
    private static final BasicStroke BASIC_STROKE = new BasicStroke(2f);
    private static final Color START_COLOR = Color.BLACK;
    private double dw;
    private double dh;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private int w;
    private int h;
    private boolean first = true;
    private List<? extends Geometry> polygonList;
    private Color color;

    public PolygonViewPanel(Collection<? extends Geometry> polygonList) {
        this.polygonList = new ArrayList<>(polygonList);
        init(this.polygonList);
    }

    private void init(List<? extends Geometry> polygonList) {
        for (Geometry geom : polygonList) {
            if (geom instanceof Polygon) {
                initPoly((Polygon) geom);
            } else if (geom instanceof MultiPolygon) {
                MultiPolygon multiPoly = (MultiPolygon) geom;
                int numGeometries = multiPoly.getNumGeometries();
                for (int i = 0; i < numGeometries; i++) {
                    Geometry geometryN = multiPoly.getGeometryN(i);
                    if (geometryN instanceof Polygon) {
                        initPoly((Polygon) geometryN);
                    }
                }
            }
        }
        initDwDh();
    }

    private void initPoly(Polygon geom) {
        Polygon polygon = geom;
        LineString exteriorRing = polygon.getExteriorRing();
        CoordinateSequence coordinateSequence = exteriorRing.getCoordinateSequence();
        int size = coordinateSequence.size();
        for (int i = 0; i < size; i++) {
            Coordinate c = coordinateSequence.getCoordinate(i);
            addPoint((float) c.x, (float) c.y);
        }
    }


    private void initDwDh() {
        dw = maxX - minX;
        dh = maxY - minY;
    }

    private void addPoint(float x, float y) {
        if (first) {
            maxX = minX = x;
            maxY = minY = y;
            first = false;
        } else {
            if (x > maxX) maxX = x;
            if (x < minX) minX = x;
            if (y > maxY) maxY = y;
            if (y < minY) minY = y;
        }
    }

    double toMapY(int y) {
        return minY - (y - h - 25) * dh / h;
    }

    double toMapX(int x) {
        return (x - 25) * dw / w + minX;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        w = size.width - 50;
        h = size.height - 50;

        BasicStroke stroke = new BasicStroke(1);
        g2.setStroke(stroke);
        color = START_COLOR;
        List<Color> colorList = new ArrayList<>();
        DistantColorFinder distantColorFinder = new DistantColorFinder();
        for (Geometry geom : polygonList) {
            colorList.add(color);
            if (geom instanceof Polygon) {
                drawPoly(g2, colorList, distantColorFinder, (Polygon) geom);
            } else if (geom instanceof MultiPolygon) {
                MultiPolygon multiPoly = (MultiPolygon) geom;
                int numGeometries = multiPoly.getNumGeometries();
                for (int i = 0; i < numGeometries; i++) {
                    Geometry geometryN = multiPoly.getGeometryN(i);
                    if (geometryN instanceof Polygon) {
                        drawPoly(g2, colorList, distantColorFinder, (Polygon) geometryN);
                    }
                }
            }
        }
    }

    private void drawPoly(Graphics2D g2, List<Color> colorList, DistantColorFinder distantColorFinder, Polygon geom) {
        Polygon polygon = geom;
        g2.setColor(color);
        //g2.setStroke(BASIC_STROKE);
        drawExterior(g2, polygon);

        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2, new float[]{2}, 0));
        int numInteriorRing = polygon.getNumInteriorRing();
        for (int i = 0; i < numInteriorRing; i++) {
            LineString interiorRingN = polygon.getInteriorRingN(i);
            CoordinateSequence coordinateSequence = interiorRingN.getCoordinateSequence();
            drawSeq(g2, coordinateSequence, Color.YELLOW);
        }
        this.color = distantColorFinder.findDistantColor(color, colorList);
        g2.setStroke(stroke);
    }

    private void drawExterior(Graphics2D g2, Polygon polygon) {
        LineString exteriorRing = polygon.getExteriorRing();
        CoordinateSequence coordinateSequence = exteriorRing.getCoordinateSequence();
        drawSeq(g2, coordinateSequence, Color.BLUE);
    }

    private void drawSeq(Graphics2D g2, CoordinateSequence coordinateSequence, Color col) {
        int psize = coordinateSequence.size();
        GeneralPath gp = new GeneralPath();
        for (int i = 0; i < psize; i++) {
            Coordinate c = coordinateSequence.getCoordinate(i);
            double x = c.x;
            double y = c.y;
            double px = (x - minX) / dw * w + 25;
            double py = h - (y - minY) / dh * h + 25;
            if (i == 0) gp.moveTo(px, py);
            else gp.lineTo(px, py);
        }
        g2.draw(gp);
        g2.setColor(col);
        g2.fill(gp);
    }

    public void setPolygonList(List<? extends Geometry> polygonList) {
        this.polygonList = polygonList;
        init(polygonList);
    }
}
