package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import ru.navilab.grid.contour.polygon.GridContourPolygons;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TriangleContourTest {
    private JComponent jComponent;
    private Collection<Geometry> geometryCollection;
    private SpinnerNumberModel spinnerModel;
    private boolean debugMode;

    public static void main(String[] args) throws Exception {
        new TriangleContourTest().run(args[0]);
    }

    public void run(String fileName) throws Exception {
        GridReader gridReader = new GridReader();
        GridAdapter mySample =  readFile(fileName, gridReader);// getSample();
        GridContourIsolines2 isolines2 = new GridContourIsolines2(mySample);
        List<Point2D> point2DS = isolines2.drawIsoline(2);
        List<Point2D> oldLines = new ArrayList<>();
        GridContourIsolines isolines = new GridContourIsolines(mySample, new IsolineConsumer() {
            @Override
            public void startPolyline(double isovalue) {
            }

            @Override
            public void consumePoint(double x, double y) {
                oldLines.add(new Point2D.Double(x, y));
            }

            @Override
            public void endPolyline(double isovalue) {
            }
        });
        isolines.drawIsoline(2f);

        jComponent = new JComponent() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                AffineTransform transform = g2.getTransform();

                double sy = getHeight() / ((mySample.getMaxY() - mySample.getMinY()));
                double sx = getWidth() / ((mySample.getMaxX() - mySample.getMinX()));
                g2.scale(sx, sy);
                g2.translate(-mySample.getMinX(), -mySample.getMinY());

                g.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2f));
                for (int i = 0; i < oldLines.size() - 1; i++) {
                    Point2D p1 = oldLines.get(i);
                    Point2D p2 = oldLines.get(i + 1);
                    double x1 = p1.getX();
                    double y1 = p1.getY();
                    g2.draw(new Line2D.Double(x1, y1, p2.getX(), p2.getY()));
                }

                g.setColor(Color.MAGENTA);
                g2.setStroke(new BasicStroke(3f));
                for (int i = 0; i < point2DS.size() - 1; i += 2) {
                    Point2D p1 = point2DS.get(i);
                    Point2D p2 = point2DS.get(i + 1);
                    double x1 = p1.getX();
                    double y1 = p1.getY();
                    g2.draw(new Line2D.Double(x1, y1, p2.getX(), p2.getY()));
//                    AffineTransform tr = g2.getTransform();
//                    g2.translate(x1, y1);
//                    g2.scale(10d, 10d);
//                    g2.drawString("" + i/2, 0, 0);
//                    g2.setTransform(tr);
                }
                g.setColor(Color.RED);
                g2.setStroke(new BasicStroke(1f));
                double prev = 0;
                for (int i = 0; i < mySample.getAxX().length; i++) {
                    double x = mySample.getAxX()[i];
                    if ((x - prev)*sx > 50) {
                        g.drawLine((int) x, (int) mySample.getMinY(), (int) x, (int) mySample.getMaxY());
                    }
                    prev = x;
                }
                prev = 0;
                for (int j = 0; j < mySample.getAxY().length; j++) {
                    double y = mySample.getAxY()[j];
                    if ((y - prev)*sy > 50) {
                        g.drawLine((int) mySample.getMinX(), (int) y, (int) mySample.getMaxX(), (int) y);
                    }
                    prev = y;
                }

                if (geometryCollection != null) draw(g2, geometryCollection);

                g2.setTransform(transform);
            }

            private void draw(Graphics2D g2, Collection<Geometry> geometryCollection) {
                for (Geometry geometry : geometryCollection) {
                    if (geometry instanceof Polygon) {
                        Polygon polygon = (Polygon) geometry;
                        drawPoly(g2, polygon);
                    }
                }
            }
            private void drawPoly(Graphics2D g2, Polygon geom) {
                Polygon polygon = geom;
                drawExterior(g2, polygon);

                Stroke savedStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2, new float[]{2}, 0));
                int numInteriorRing = polygon.getNumInteriorRing();
                for (int i = 0; i < numInteriorRing; i++) {
                    LineString interiorRingN = polygon.getInteriorRingN(i);
                    CoordinateSequence coordinateSequence = interiorRingN.getCoordinateSequence();
                    drawSeq(g2, coordinateSequence, Color.MAGENTA);
                }
                g2.setStroke(savedStroke);
            }

            private void drawExterior(Graphics2D g2, Polygon polygon) {
                LineString exteriorRing = polygon.getExteriorRing();
                CoordinateSequence coordinateSequence = exteriorRing.getCoordinateSequence();
                drawSeq(g2, coordinateSequence, Color.BLUE);
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
                BasicStroke s = new BasicStroke(3);
                g2.setStroke(s);
//                g2.setColor(Color.yellow);
//                g2.draw(gp);
            }
        };
        JFrame frame = new JFrame("polygons");
        frame.getContentPane().add(jComponent, BorderLayout.CENTER);
        JButton button = new JButton("gtest");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    GridContourPolygons contourPolygons = new GridContourPolygons(mySample);
                    contourPolygons.setDebugMode(debugMode);
                    int value = spinnerModel.getNumber().intValue();
                    if (debugMode) contourPolygons.setDebugCount(value);
                    geometryCollection = contourPolygons.drawPolygons(2, 3);//value);
                    jComponent.repaint();
                    spinnerModel.setValue(value + 1);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });
        spinnerModel = new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1);
        JSpinner jSpinner = new JSpinner(spinnerModel);
        jSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                debugMode = true;
            }
        });
        frame.getContentPane().add(jSpinner, BorderLayout.NORTH);
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
        frame.setVisible(true);
    }

    private XYZGrid getSample() {
        return new XYZGrid(12650000, 6950000, 500, 5, 5,
                new double[][]{
                        {1,     1,      1,      1,      1},
                        {1,     2.7,    1.7,    2.1,    1},
                        {0.1,   2.2,    5,      2.1,    1},
                        {1,     2.7,    1.7,    2.1,    1},
                        {1,     1,      1,      1,      1}
                }, -9999d
        );
    }

    private GridAdapter readFile(String fileName, GridReader gridReader) throws GridLoadException {
        return gridReader.readGridData(fileName);
    }
}
