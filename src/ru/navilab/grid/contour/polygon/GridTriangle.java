package ru.navilab.grid.contour.polygon;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Mikhailov_KG
 * Date: 22.07.2020
 */
public class GridTriangle {
    private GridPoint[] ownPoints;
    private GridTriangle unionTriangle;
    private GridPoint unionStartPoint;

    public GridTriangle(double[] xyarr, int arrIndex) {
        ownPoints = new GridPoint[arrIndex / 2];
        GridPoint prevPoint = null;
        for (int i = 0; i < arrIndex/2; i++) {
            double x = xyarr[i*2];
            double y = xyarr[i*2+1];
            GridPoint newPoint = new GridPoint(x, y);
            ownPoints[i] = newPoint;
            if (prevPoint != null) {
                newPoint.setPrev(prevPoint);
                prevPoint.setNext(newPoint);
            }
            prevPoint = newPoint;
        }
        GridPoint lastPoint = prevPoint;
        lastPoint.setNext(ownPoints[0]); // close polygon
        ownPoints[0].setPrev(lastPoint);
    }

    public GridPoint[] getOwnPoints() {
        return ownPoints;
    }

    public void union(GridTriangle triangle2) {
        if (triangle2.getUnionTriangle() != null && triangle2.getUnionTriangle() == unionTriangle) return;
        if (triangle2.getUnionTriangle() != null) {
            return;
        }

        // iterate all t1 points
        int found = 0;
        GridPoint[] pairs = new GridPoint[4];
        for (GridPoint point : ownPoints) {
            GridPoint p = triangle2.findPointEquals(point);
            if (p != null) {
                if (found >= 4) {
                    System.err.println("too many joint points");
                    return;
                }
                pairs[found++] = point;
                pairs[found++] = p;
            }

        }
        if (found == 4) {
            union(pairs[0], pairs[1], pairs[2], pairs[3], triangle2);
        }
    }

    private void union(GridPoint p1t1, GridPoint p1t2, GridPoint p2t1, GridPoint p2t2, GridTriangle triangle2) {
        // checks for validity - local points linked together
        if (!p1t1.isDirectLinkedTo(p2t1)) {
            System.err.println("not linked " + p1t1 + " and " + p2t1);
            return;
        }
        if (!p1t2.isDirectLinkedTo(p2t2)) {
            System.err.println("not linked " + p1t2 + " and " + p2t2);
            return;
        }

        if ((p1t1.getNext() == p2t1 && p1t2.getNext() == p2t2) ||
                (p2t1.getNext() == p1t1 && p2t2.getNext() == p1t2)) {
            p1t2.removeBothLinks(p2t2);
            p1t2.reverseOrder(null);
        }

        // remove local links
        p1t1.removeBothLinks(p2t1);

        triangle2.replace(p1t2, p1t1);
        triangle2.replace(p2t2, p2t1);

        connect(p1t1, p2t1, p1t2, p2t2);

        // copy own to union if last is not set
        if (unionTriangle == null) {
            unionTriangle = this;
            if (unionStartPoint == null) unionStartPoint = ownPoints[0];
        }
        triangle2.setUnionTriangle(unionTriangle);
    }

    String printPrevFrom(GridPoint startPoint) {
        StringBuilder sb = new StringBuilder();
        GridPoint p = startPoint;
        do {
            sb.append("\tp=" + p + "\n");
            p = p.getPrev();
            if (p == null) {
                sb.append("prev is null!\n");
                p = startPoint;
            }

        } while (p != startPoint);
        return sb.toString();
    }


    String printNextFrom(GridPoint startPoint) {
        StringBuilder sb = new StringBuilder();
        GridPoint p = startPoint;
        do {
            sb.append("\tp=" + p + "\n");
            p = p.getNext();
            if (p == null) {
                sb.append("next is null!\n");
                p = startPoint;
            }

        } while (p != startPoint);
        return sb.toString();
    }

    String printOwn() {
        String s = "";
        for (GridPoint ownPoint : ownPoints) {
            s += "own " + ownPoint;
        }
        return s;
    }

    private void connect(GridPoint p1t1, GridPoint p2t1, GridPoint p1t2, GridPoint p2t2) {
        if (!p1t1.hasNext()) {
            GridPoint p1t1prev = p1t1.getPrev();
            double y = p1t1.getY();
            if (isOneLine(p1t1prev, p1t1, p1t2.getNext())) {
                p1t1prev.setNext(p1t2.getNext());
                p1t2.getNext().setPrev(p1t1prev);
                changeUnionStartPoint(p1t1prev);
            } else {
                p1t1.setNext(p1t2.getNext());
            }
            GridPoint p2t1next = p2t1.getNext();
            if (isOneLine(p2t1next, p2t1, p2t2.getPrev())) {
                p2t1next.setPrev(p2t2.getPrev());
                p2t2.getPrev().setNext(p2t1next);
                changeUnionStartPoint(p2t1next);
            } else {
                p2t1.setPrev(p2t2.getPrev());
            }
        } else if (!p1t1.hasPrev()) {
            GridPoint p1t1next = p1t1.getNext();
            if (isOneLine(p1t1next, p1t1, p1t2.getPrev())) {
                p1t1next.setPrev(p1t2.getPrev());
                p1t2.getPrev().setNext(p1t1next);
                changeUnionStartPoint(p1t1next);
            } else {
                p1t1.setPrev(p1t2.getPrev());
            }
            GridPoint p2t1prev = p2t1.getPrev();
            if (isOneLine(p2t1prev, p2t1, p2t2.getNext())) {
                p2t1prev.setNext(p2t2.getNext());
                p2t2.getNext().setPrev(p2t1prev);
                changeUnionStartPoint(p2t1prev);
            } else {
                p2t1.setNext(p2t2.getNext());
            }
        }
    }

    private void changeUnionStartPoint(GridPoint point) {
        if (unionTriangle == null) unionStartPoint = point;
        else {
            unionTriangle.unionStartPoint = point;
        }
    }

    private boolean isOneLine(GridPoint p1t1neighb, GridPoint p1t1, GridPoint p1t2neighb) {
        double y = p1t1.getY();
        double x = p1t1.getX();
        return (y == p1t1neighb.getY()) && (y == p1t2neighb.getY()) &&
                (Math.signum(p1t1neighb.getX() - x) == Math.signum(p1t2neighb.getX() - x));
    }

    private void replace(GridPoint p1, GridPoint p2) {
        for (int i = 0; i < ownPoints.length; i++) {
            GridPoint ownPoint = ownPoints[i];
            if (ownPoint.getNext() == p1) ownPoint.setNext(p2);
            if (ownPoint.getPrev() == p1) ownPoint.setPrev(p2);
            if (p1 == ownPoint) {
                ownPoints[i] = p2;
            }
        }
    }

    private GridPoint findPointEquals(GridPoint p) {
        for (GridPoint ownPoint : ownPoints) {
            if (p.equals(ownPoint)) return ownPoint;
        }
        return null;
    }

    public void setUnionTriangle(GridTriangle unionTriangle) {
        this.unionTriangle = unionTriangle;
    }

    public GridTriangle getUnionTriangle() {
        return unionTriangle;
    }

    public Geometry buildGeometry(GeometryFactory factory) {
        GridPoint startPoint = unionTriangle != null ? unionTriangle.unionStartPoint : ownPoints[0];
        List<GridPoint> gridPoints = getGridPoints(startPoint);
        return getGeometry(factory, gridPoints);
    }

    private Geometry getGeometry(GeometryFactory factory, List<GridPoint> gridPoints) {
        Coordinate[] coordinates = convertToCoordinates(gridPoints);
        CoordinateArraySequence arraySequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(arraySequence, factory);
        return new Polygon(linearRing, null, factory);
    }

    private Coordinate[] convertToCoordinates(List<GridPoint> gridPoints) {
        Coordinate[] coordinates = new Coordinate[gridPoints.size()];
        for (int i = 0; i < coordinates.length; i++) {
            GridPoint p = gridPoints.get(i);
            Coordinate c = new Coordinate();
            c.x = p.getX();
            c.y = p.getY();
            coordinates[i] = c;
        }
        return coordinates;
    }

    private List<GridPoint> getGridPoints(GridPoint startPoint) {
        GridPoint p = startPoint;
        ArrayList<GridPoint> gridPoints = new ArrayList<>();
        do {
            gridPoints.add(p);
            p = p.getNext();
        } while (p != startPoint);
        gridPoints.add(startPoint);
        return gridPoints;
    }


    @Override
    public String toString() {
        if (unionTriangle != null) return buildGeometry(new GeometryFactory()).toString();
        return toStringOwn();
    }

    public String toStringOwn() {
        List<GridPoint> gridPoints = new ArrayList<>(Arrays.asList(ownPoints));
        gridPoints.add(gridPoints.get(0));
        Geometry geometry = getGeometry(new GeometryFactory(), gridPoints);
        return geometry.toString();
    }
}
