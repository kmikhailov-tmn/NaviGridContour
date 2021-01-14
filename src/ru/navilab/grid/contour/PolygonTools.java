package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

public class PolygonTools {

    public static String point2DToWkt(Point2D p) {
        return String.format(Locale.US, "POINT(%.1f %.1f)", p.getX(), p.getY());
    }


    public static List<Polygon> buildPolygon(List<Point2D> pointList) {
        GeometryFactory factory = new GeometryFactory();
        Polygonizer polygonizer = new Polygonizer();
        float[] coords = new float[pointList.size() * 2];
        for (int i = 0; i < pointList.size(); i++) {
            Point2D point2D = pointList.get(i);
            coords[i * 2] = (float) point2D.getX();
            coords[i * 2 + 1] = (float) point2D.getY();
        }
        CoordinateSequence coordseq = new PackedCoordinateSequence.Double(coords, 2);
        LineString lineString = new LineString(coordseq, factory);
        Geometry noddedLine = lineString.union();

        for (int i = 0; i < noddedLine.getNumGeometries(); i++) {
            Geometry geom = noddedLine.getGeometryN(i);
            Coordinate[] coordinates = geom.getCoordinates();
            if (coordinates.length > 0) {
                PackedCoordinateSequence.Double sequence = new PackedCoordinateSequence.Double(coordinates);//list.toArray(new Coordinate[list.size()]));
                LineString shell = new LineString(sequence, factory);
                if (!shell.isClosed()) {
                    Coordinate[] closedCoords = new Coordinate[coordinates.length + 1];
                    System.arraycopy(coordinates, 0, closedCoords, 0, coordinates.length);
                    closedCoords[closedCoords.length - 1] = closedCoords[0];
                    PackedCoordinateSequence.Double sequence2 = new PackedCoordinateSequence.Double(closedCoords);//list.toArray(new Coordinate[list.size()]));
                    shell = new LineString(sequence2, factory);
                }
                polygonizer.add(shell);
            }
        }
        return new ArrayList<>(polygonizer.getPolygons());
    }

    public static List<Polygon> buildPolygonList(List<List<Point2D>> listOfPointList) {
        GeometryFactory factory = new GeometryFactory();
        Polygonizer polygonizer = new Polygonizer();
//        List<Polygon> resultList = new ArrayList<>();
        for (List<Point2D> point2DList : listOfPointList) {
            float[] coords = new float[point2DList.size() * 2];
            for (int i = 0; i < point2DList.size(); i++) {
                Point2D point2D = point2DList.get(i);
                coords[i * 2] = (float) point2D.getX();
                coords[i * 2 + 1] = (float) point2D.getY();
            }
            CoordinateSequence coordseq = new PackedCoordinateSequence.Double(coords, 2);
            LineString lineString = new LineString(coordseq, factory);
            Geometry noddedLine = lineString.union();

            for (int i = 0; i < noddedLine.getNumGeometries(); i++) {
                Geometry geom = noddedLine.getGeometryN(i);
                Coordinate[] coordinates = geom.getCoordinates();
                if (coordinates.length > 0) {
                    PackedCoordinateSequence.Double sequence = new PackedCoordinateSequence.Double(coordinates);//list.toArray(new Coordinate[list.size()]));
                    LineString shell = new LineString(sequence, factory);
                    if (!shell.isClosed()) {
                        Coordinate[] closedCoords = new Coordinate[coordinates.length + 1];
                        System.arraycopy(coordinates, 0, closedCoords, 0, coordinates.length);
                        closedCoords[closedCoords.length - 1] = closedCoords[0];
                        PackedCoordinateSequence.Double sequence2 = new PackedCoordinateSequence.Double(closedCoords);//list.toArray(new Coordinate[list.size()]));
                        shell = new LineString(sequence2, factory);
                    }
                    polygonizer.add(shell);
                }
            }
        }
        Collection<Polygon> polygons = polygonizer.getPolygons();
        return new ArrayList<>(polygons);
    }

    public static Geometry getBiggestGeometry(Geometry noddedLine) {
        Geometry[] geometries = new Geometry[noddedLine.getNumGeometries()];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = noddedLine.getGeometryN(i);
        }
        Arrays.sort(geometries, new Comparator<Geometry>() {
            @Override
            public int compare(Geometry o1, Geometry o2) {
                return Integer.compare(o2.getCoordinates().length, o1.getCoordinates().length);
            }
        });
        return geometries[0];
    }

    static Point2D intersect(Line2D.Double line1, Line2D.Double line2) {
        Point2D.Double s1 = (Point2D.Double) line1.getP1();
        Point2D.Double s2 = (Point2D.Double) line1.getP2();
        Point2D.Double d1 = (Point2D.Double) line2.getP1();
        Point2D.Double d2 = (Point2D.Double) line2.getP2();
        double a1 = s2.y - s1.y;
        double b1 = s1.x - s2.x;
        double c1 = a1 * s1.x + b1 * s1.y;

        double a2 = d2.y - d1.y;
        double b2 = d1.x - d2.x;
        double c2 = a2 * d1.x + b2 * d1.y;

        double delta = a1 * b2 - a2 * b1;
        return new Point2D.Float((float) ((b2 * c1 - b1 * c2) / delta), (float) ((a1 * c2 - a2 * c1) / delta));
    }

    public static List<? extends Geometry> makeHeightPolygons(List<Polygon> curPolygonList, List<Polygon> prevPolygonList) {
        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < curPolygonList.size(); i++) {
            Polygon current = curPolygonList.get(i);
            int resultCount = 0;
            for (int j = 0; j < prevPolygonList.size(); j++) {
                Polygon prev = prevPolygonList.get(j);
                try {
                    if (current.intersects(prev)) {
                        result.add(current.symDifference(prev));
                        resultCount++;
                        break;
                    }
                } catch (Exception e) {
                    System.err.println(prev.isSimple() + " " + prev.isValid() + " " + e.getLocalizedMessage());
                    System.err.println(current.isSimple() + " " + current.isValid() + " " + e.getLocalizedMessage());
                    e.printStackTrace();
                    break;
                }
            }
            if (resultCount == 0) result.add(current);
        }
        return result;
    }
}
