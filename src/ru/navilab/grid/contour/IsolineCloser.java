package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IsolineCloser {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final List<IsolevelLines> isolevelList;
    private GridAdapter gridData;
    private List<List<Point2D>> prevLineList = Collections.EMPTY_LIST;
    private List<List<Point2D>> currentLineList = new ArrayList<>();
    private Set<List<Point2D>> usedPointSet = new HashSet<>();
    private Set<Point2D> usedPoints = new HashSet<>();

    private static class XYZ {
        double x;
        double y;
        double z;

        public Point2D.Double createPoint2D() {
            return new Point2D.Double(x, y);
        }
    }


    public IsolineCloser(List<IsolevelLines> isolevelList, GridAdapter gridData) {
        this.isolevelList = isolevelList;
        this.gridData = gridData;
    }

    static final boolean isZInInterval(double z, double prevLevel, double isolevel) {
        return (z <= isolevel); // (z >= prevLevel) &&
    }


    public List<LevelPolygon> buildClosedIsolevelLines() {
        List<LevelPolygon> r = new ArrayList<>();
        double prevLevel = gridData.getMinZ() - 1;
        prevLineList = new ArrayList<>();

        for (IsolevelLines isolevelLines : isolevelList) {
            double isolevel = isolevelLines.getIsolevel();
            logger.fine("isolevel " + isolevel);
            List<List<Point2D>> listOfPointList = isolevelLines.getListOfPointList();
            List<Polygon> result = new ArrayList<>();
            for (List<Point2D> pointList : listOfPointList) {
                if (pointList.size() > 1 && !isClosed(pointList)) {
                    // All polylines (not closed) in contour plot always touch two or one side of grid
                    currentLineList.add(new ArrayList<>(pointList));
                } else {
                    List<Polygon> polygonList = PolygonTools.buildPolygon(pointList);
                    for (Polygon polygon : polygonList) {
                        if (checkNeedInvert(polygon, isolevel)) {
                            Geometry geometry = invertPolygon(polygon);
                            logger.fine("invert " + Level2PolygonTransformer.poly2str(polygon));
                            logger.fine("inverted " + Level2PolygonTransformer.poly2str(geometry));
                            if (geometry instanceof Polygon) {
                                Polygon poly = (Polygon) geometry;
                                result.add(poly);
                            } else {
                                result.add(polygon);
                            }
                        } else {
                            result.add(polygon);
                        }
                    }
                }
            }
            for (List<Point2D> pointList : currentLineList) {
                if (!usedPointSet.contains(pointList)) {
                    List<Point2D> closedPointList = closePolyline(prevLevel, isolevel, pointList);
                    if (closedPointList != null) {
                        logger.finer("closedPolyline closedPointList=" + closedPointList.size());
                        List<Polygon> polygonList = PolygonTools.buildPolygon(closedPointList);
                        for (Polygon polygon : polygonList) {
                            logger.finer("closed wkt " + polygon.getExteriorRing().getNumPoints() + " " + polygon.toText());
                        }
                        result.addAll(polygonList);
                    }
                } else {
                    logger.finer("pointList contained in usedPointSet");
                }
                usedPoints.clear();
            }
            if (result.size() > 0) r.add(new LevelPolygon(isolevelLines.getIsolevel(), result));
            prevLineList = currentLineList;
            currentLineList = new ArrayList<>();
            usedPointSet.clear();
            prevLevel = isolevel;
        }
        return r;
    }

    private Geometry invertPolygon(Polygon polygon) {
        Coordinate[] coordArray = new Coordinate[5];
        coordArray[0] = toCoordinate(getTopLeft());
        coordArray[1] = toCoordinate(getTopRight());
        coordArray[2] = toCoordinate(getBottomRight());
        coordArray[3] = toCoordinate(getBottomLeft());
        coordArray[4] = toCoordinate(getTopLeft());
        GeometryFactory factory = new GeometryFactory();
        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(coordArray), factory);
        Polygon rectPolygon = new Polygon(linearRing, null, factory);
        return rectPolygon.symDifference(polygon);
    }

    private Coordinate toCoordinate(XYZ xyz) {
        return new Coordinate(xyz.x, xyz.y);
    }

    private boolean checkNeedInvert(Polygon polygon, double isolevel) {
        try {
            Point centroid = polygon.getCentroid();
            Coordinate c = centroid.getCoordinate();
            double[] axisX = gridData.getAxX();
            double[] axisY = gridData.getAxY();
            int ncols = axisX.length;
            int nrows = axisY.length;
            double stepY = (axisY[nrows - 1] - axisY[0]) / (nrows - 1);
            double stepX = (axisX[ncols - 1] - axisX[0]) / (ncols - 1);
            int yIndex =  (int) ((c.y - axisY[0]) / stepY);
            int xIndex = (int) ((c.x - axisX[0]) / stepX);
            if (yIndex <= 0) yIndex = 0;
            if (yIndex >= (nrows-1)) yIndex = nrows-1-1;
            if (xIndex <= 0) xIndex = 0;
            if (xIndex >= (ncols-1)) xIndex = ncols-1-1;
            double[][] z = gridData.getValueZ();
            double zValue = z[xIndex][nrows - 1 - yIndex];
            double x = axisX[xIndex];
            double y = axisY[yIndex];
            Coordinate newCoord = new Coordinate(x, y);
            GeometryFactory f = new GeometryFactory();
            CoordinateSequence sequence = new CoordinateArraySequence(new Coordinate[]{newCoord});
            Point point = new Point(sequence, f);
            boolean within = point.within(polygon);
            boolean zInInterval = (zValue <= isolevel);
            if (zValue == gridData.getMissingZ()) zInInterval = false;
            if (zInInterval && within) return false;
            else if (!zInInterval && !within) return false;
            else return true;
        } catch (TopologyException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isClosed(List<Point2D> pointList) {
        Point2D first = pointList.get(0);
        Point2D last = pointList.get(pointList.size() - 1);
        return first.equals(last);
    }

    private List<Point2D> closePolyline(double prevLevel, double isolevel, List<Point2D> pointList) {
        logger.finer("closePolyline isolevel=" + isolevel + " pointCount=" + pointList.size() + " " + prevLevel);
        Point2D p1 = pointList.get(pointList.size() - 1);
        logger.finer("p1 " + PolygonTools.point2DToWkt(p1));
        usedPoints.add(p1);
        Point2D pn = pointList.get(0);
        logger.finer("pn " + PolygonTools.point2DToWkt(pn));
        if (!isPointOnSide(p1)) return null;
        Side side1 = getSide(p1);
        Side sideN = getSide(pn);
        return startWalkToSideOld(prevLevel, isolevel, p1, side1, sideN, new ArrayList<>(pointList), pn);
    }

    private boolean isPointOnSide(Point2D p1) {
        double dx = gridData.getMaxX() - gridData.getMinX();
        double dy = gridData.getMaxY() - gridData.getMinY();
        if (Math.abs(p1.getX() - gridData.getMinX()) / dx < 0.01) return true;
        else if (Math.abs(gridData.getMaxX() - p1.getX()) / dx < 0.01) return true;
        else if (Math.abs(p1.getY() - gridData.getMinY()) / dy < 0.01) return true;
        else if (Math.abs(gridData.getMaxY() - p1.getY()) / dy < 0.01) return true;
        return false;
    }

    private Direction getInitialDirection(Point2D p1, float prevLevel, float isolevel, Side side1) {
        switch (side1) {
            case LEFT:
            case RIGHT:
                return getVerticalDirection(p1, prevLevel, isolevel, side1);
            case BOTTOM:
            case TOP:
                return getHorizontalDirection(p1, prevLevel, isolevel, side1);
        }
        return null;
    }

    private List<Point2D> startWalkToSideOld(double prevLevel, double isolevel, Point2D p1, Side side1, Side sideN, List<Point2D> pointList, Point2D pn) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("walkSideToSide side1=" + side1 + " sideN=" + sideN + " pointCount=" + pointList.size() + " " + pointList.hashCode());
        }
// if (side1 == sideN) {
// if (logger.isLoggable(Level.FINER)) {
// logger.finer("side1=sideN=" + side1 + ", so close polyline " + pointList.size() + " " + pointList.hashCode());
// }
// return closePolyline(pointList);
// }
        switch (side1) {
            case LEFT: {
                Direction dir = getVerticalDirection(p1, prevLevel, isolevel, side1);
                if (checkOneSide(side1, sideN, dir, p1, pn)) return closePolyline(pointList, side1);
                if (dir == Direction.DOWN) return move2(side1, sideN, pointList, getBottomLeft(), Side.BOTTOM);
                else return move2(side1, sideN, pointList, getTopLeft(), Side.TOP);
            }
            case RIGHT: {
                Direction dir = getVerticalDirection(p1, prevLevel, isolevel, side1);
                if (checkOneSide(side1, sideN, dir, p1, pn)) return closePolyline(pointList, side1);
                if (dir == Direction.DOWN) return move2(side1, sideN, pointList, getBottomRight(), Side.BOTTOM);
                else return move2(side1, sideN, pointList, getTopRight(), Side.TOP);
            }
            case BOTTOM: {
                Direction dir = getHorizontalDirection(p1, prevLevel, isolevel, side1);
                if (checkOneSide(side1, sideN, dir, p1, pn)) return closePolyline(pointList, side1);
                if (dir == Direction.LEFT) return move2(side1, sideN, pointList, getBottomLeft(), Side.LEFT);
                else return move2(side1, sideN, pointList, getBottomRight(), Side.RIGHT);
            }
            case TOP: {
                Direction dir = getHorizontalDirection(p1, prevLevel, isolevel, side1);
                if (checkOneSide(side1, sideN, dir, p1, pn)) return closePolyline(pointList, side1);
                if (dir == Direction.LEFT) return move2(side1, sideN, pointList, getTopLeft(), Side.LEFT);
                else return move2(side1, sideN, pointList, getTopRight(), Side.RIGHT);
            }
            default: {
                return closePolyline(pointList, side1);
            }
        }
    }

    private List<Point2D> move2(Side fromSide, Side endSide, List<Point2D> pointList, XYZ xyz, Side newSide) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("move2 fromSide=" + fromSide + " newSide=" + newSide + " pointCount=" +
                    pointList.size() + " " + pointList.hashCode() + " xy=" + xyz.createPoint2D().toString());
        }
        addPoint(pointList, xyz.createPoint2D(), fromSide);

        if (newSide == endSide) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("newSide1=endSide=" + newSide + ", so close polyline " + pointList.size() + " " + pointList.hashCode());
            }
            return closePolyline(pointList, fromSide);
        }
        switch (newSide) {
            case TOP:
                if (fromSide == Side.RIGHT) return move2(newSide, endSide, pointList, getTopLeft(), Side.LEFT);
                else return move2(newSide, endSide, pointList, getTopRight(), Side.RIGHT);
            case BOTTOM:
                if (fromSide == Side.RIGHT) return move2(newSide, endSide, pointList, getBottomLeft(), Side.LEFT);
                else return move2(newSide, endSide, pointList, getBottomRight(), Side.RIGHT);
            case RIGHT:
                if (fromSide == Side.TOP) return move2(newSide, endSide, pointList, getBottomRight(), Side.BOTTOM);
                else return move2(newSide, endSide, pointList, getTopRight(), Side.TOP);
            case LEFT:
                if (fromSide == Side.TOP) return move2(newSide, endSide, pointList, getBottomLeft(), Side.BOTTOM);
                else return move2(newSide, endSide, pointList, getTopLeft(), Side.TOP);
            default: {
                return closePolyline(pointList, fromSide);
            }
        }
    }

    private void addPoint(List<Point2D> pointList, Point2D p, Side fromSide) {
        Point2D lastPoint = pointList.get(pointList.size() - 1);
        switch (fromSide) {
            case LEFT:
            case RIGHT:
                pointList.add(new Point2D.Double(p.getX(), lastPoint.getY()));
                break;
            case TOP:
            case BOTTOM:
                pointList.add(new Point2D.Double(lastPoint.getX(), p.getY()));
                break;
        }
        pointList.add(p);
    }


    private List<Point2D> startWalkToSide(float prevLevel, float isolevel, Point2D p1, Side side1, Side sideN, List<Point2D> pointList, Point2D pn, Direction dir) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("walkSideToSide side1=" + side1 + " sideN=" + sideN + " pointCount=" + pointList.size() +
                    " p1=" + p1 + " pn=" + pn + " dir=" + dir);
        }
        NearestPoint nearestPoint = findNearestPoint(side1, dir, p1, pn);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("nearest point = " + nearestPoint);
            if (nearestPoint != null) {
                logger.finer("2nearest point = " + usedPoints.size() + " " + usedPoints.contains(nearestPoint.p));
            }
        }
        if (nearestPoint != null) {
            if (nearestPoint.p == pn) {
                pointList.add(nearestPoint.p);
                return pointList;
            } else {
                return moveToNearestPoint(side1, dir, p1, pn, nearestPoint, sideN, pointList, prevLevel, isolevel);
            }
        } else if (side1 == sideN) {
            logger.finer("no nearest point on one side so close the polygon " + nearestPoint);
            return closePolyline(pointList, pn);
        }
        switch (side1) {
            case LEFT: {
                if (dir == Direction.DOWN) return moveToSide(side1, sideN, pointList, getBottomLeft(), Side.BOTTOM);
                else return moveToSide(side1, sideN, pointList, getTopLeft(), Side.TOP);
            }
            case RIGHT: {
                if (dir == Direction.DOWN) return moveToSide(side1, sideN, pointList, getBottomRight(), Side.BOTTOM);
                else return moveToSide(side1, sideN, pointList, getTopRight(), Side.TOP);
            }
            case BOTTOM: {
                if (dir == Direction.LEFT) return moveToSide(side1, sideN, pointList, getBottomLeft(), Side.LEFT);
                else return moveToSide(side1, sideN, pointList, getBottomRight(), Side.RIGHT);
            }
            case TOP: {
                if (dir == Direction.LEFT) return moveToSide(side1, sideN, pointList, getTopLeft(), Side.LEFT);
                else return moveToSide(side1, sideN, pointList, getTopRight(), Side.RIGHT);
            }
            default: {
                return closePolyline(pointList, pn);
            }
        }
    }

    static class NearestPoint implements Comparable<NearestPoint> {
        private Point2D p;
        private double distance;
        private List<Point2D> pointList;
        private boolean current;
        private boolean endPointFlag;

        public NearestPoint(Point2D p, double distance, List<Point2D> pointList, boolean current, boolean endPointFlag) {
            this.p = p;
            this.distance = distance;
            this.pointList = pointList;
            this.current = current;
            this.endPointFlag = endPointFlag;
        }

        @Override
        public int compareTo(NearestPoint o) {
            return Double.compare(distance, o.distance);
        }

        @Override
        public String toString() {
            return String.format("p=%s distance=%.0f %s %s", p, distance, current ? "current" : "notCurrent", endPointFlag ? "EPF" : "notEPF");
        }
    }

    private List<Point2D> moveToNearestPoint(Side side1, Direction dir, Point2D p1, Point2D pn, NearestPoint nearestPoint, Side sideN, List<Point2D> pointList, float prevLevel, float isolevel) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("moveToNearestPoint side1=" + side1 + " dir=" + dir + " p1=" + p1 + " pn=" + pn +
                    " nearestPoint " + nearestPoint.p + " sideN=" + sideN);
        }
        usedPoints.add(nearestPoint.p);
        if (nearestPoint.endPointFlag) {
            for (int i = nearestPoint.pointList.size() - 1; i >= 0 ; i--) {
                pointList.add(nearestPoint.pointList.get(i));
            }
        } else {
            pointList.addAll(nearestPoint.pointList);
        }
        if (nearestPoint.current) {
            usedPointSet.add(pointList);
        }
        Point2D lastPoint = pointList.get(pointList.size() - 1);
        ClockDirection clockDir = getClockDir(side1, dir);
        Side newSide = getSide(lastPoint);
        Direction newDir = getDirection(newSide, clockDir);
        return startWalkToSide(prevLevel, isolevel, lastPoint, newSide, sideN, pointList, pn, newDir);
    }

    private Direction getDirection(Side side, ClockDirection clockDir) {
        switch (side) {
            case RIGHT: return clockDir == ClockDirection.CLOCKWISE ? Direction.DOWN : Direction.UP;
            case LEFT: return clockDir == ClockDirection.ANTICLOCKWISE ? Direction.DOWN : Direction.UP;
            case BOTTOM: return clockDir == ClockDirection.CLOCKWISE ? Direction.LEFT: Direction.RIGHT;
            case TOP: return clockDir == ClockDirection.ANTICLOCKWISE ? Direction.LEFT: Direction.RIGHT;
        }
        return null;
    }

    private ClockDirection getClockDir(Side side1, Direction dir) {
        switch (side1) {
            case LEFT: return (dir == Direction.UP) ? ClockDirection.CLOCKWISE : ClockDirection.ANTICLOCKWISE;
            case RIGHT: return (dir == Direction.DOWN) ? ClockDirection.CLOCKWISE : ClockDirection.ANTICLOCKWISE;
            case BOTTOM: return (dir == Direction.LEFT) ? ClockDirection.CLOCKWISE : ClockDirection.ANTICLOCKWISE;
            case TOP: return (dir == Direction.RIGHT) ? ClockDirection.CLOCKWISE : ClockDirection.ANTICLOCKWISE;
        }
        throw new RuntimeException("" + side1);
    }

    private NearestPoint findNearestPoint(Side side1, Direction dir, Point2D p, Point2D pn) {
        List<NearestPoint> result = new ArrayList<>();
        addPoints(side1, dir, p, result, this.prevLineList, false);
        addPoints(side1, dir, p, result, this.currentLineList, true);
        if (result.size() == 0) return null;
        if (result.size() > 1) Collections.sort(result);
        return result.get(0);
    }

    private void addPoints(Side side1, Direction dir, Point2D p, List<NearestPoint> result, List<List<Point2D>> lineList, boolean current) {
        for (List<Point2D> pointList : lineList) {
            if (pointList.size() > 1) {
                Point2D p1 = pointList.get(0);
                Point2D p2 = pointList.get(pointList.size() - 1);
                if (!usedPoints.contains(p1) && getSide(p1) == side1 && isPointOnDirection(p, p1, dir)) {
                    result.add(new NearestPoint(p1, distance(p, p1, dir), pointList, current, false));
                }
                if (!usedPoints.contains(p2) && getSide(p2) == side1 && isPointOnDirection(p, p2, dir)) {
                    result.add(new NearestPoint(p2, distance(p, p2, dir), pointList, current, true));
                }
            }
        }
    }

    private double distance(Point2D p1, Point2D p2, Direction dir) {
        switch (dir) {
            case DOWN: return (p2.getY() - p1.getY());
            case UP: return (p2.getY() - p1.getY());
            case RIGHT: return (p2.getX() - p1.getX());
            case LEFT: return (p2.getX() - p1.getX());
        }
        throw new RuntimeException("this should never happen");
    }

    private boolean isPointOnDirection(Point2D p1, Point2D p2, Direction dir) {
        switch (dir) {
            case DOWN: return (p2.getY() - p1.getY()) < 0;
            case UP: return (p2.getY() - p1.getY()) > 0;
            case RIGHT: return (p2.getX() - p1.getX()) > 0;
            case LEFT: return (p2.getX() - p1.getX()) < 0;
        }
       throw new RuntimeException("this should never happen");
    }

    private boolean checkOneSide(Side side1, Side sideN, Direction dir, Point2D p1, Point2D pn) {
        if (side1 != sideN) return false;
        switch (side1) {
            case LEFT:
            case RIGHT:
                double dy = pn.getY() - p1.getY();
                return ((dir == Direction.DOWN && dy < 0) || (dir == Direction.UP && dy > 0));
            case TOP:
            case BOTTOM:
                double dx = pn.getX() - p1.getX();
                return ((dir == Direction.RIGHT && dx > 0) || (dir == Direction.LEFT && dx < 0));
        }
        return false;
    }

//    private List<Point2D> move(float prevLevel, float isolevel, Side sideN, List<Point2D> pointList, XYZ xyz, Side side) {
//        if (logger.isLoggable(Level.FINER)) {
//            logger.finer("move level=" + isolevel + " sideN=" + sideN + " side=" + side + " pointCount=" +
//                    pointList.size() + " " + pointList.hashCode() + " xy=" + xyz.createPoint2D().toString());
//        }
//        Point2D p = xyz.createPoint2D();
//        pointList.add(p);
//        return startWalkToSide(prevLevel, isolevel, p, side, sideN, pointList, pn);
//    }

    private List<Point2D> moveToSide(Side fromSide, Side endSide, List<Point2D> pointList, XYZ xyz, Side newSide) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("moveToSide fromSide=" + fromSide + " newSide=" + newSide + " pointCount=" +
                    pointList.size() + " " + pointList.hashCode() + " xy=" + xyz.createPoint2D().toString());
        }
        Point2D p = xyz.createPoint2D();
        pointList.add(p);

        if (newSide == endSide) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("newSide1=endSide=" + newSide + ", so close polyline " + pointList.size() + " " + pointList.hashCode());
            }
            return closePolyline(pointList, fromSide);
        }
        switch (newSide) {
            case TOP:
                if (fromSide == Side.RIGHT) return moveToSide(newSide, endSide, pointList, getTopLeft(), Side.LEFT);
                else return moveToSide(newSide, endSide, pointList, getTopRight(), Side.RIGHT);
            case BOTTOM:
                if (fromSide == Side.RIGHT) return moveToSide(newSide, endSide, pointList, getBottomLeft(), Side.LEFT);
                else return moveToSide(newSide, endSide, pointList, getBottomRight(), Side.RIGHT);
            case RIGHT:
                if (fromSide == Side.TOP) return moveToSide(newSide, endSide, pointList, getBottomRight(), Side.BOTTOM);
                else return moveToSide(newSide, endSide, pointList, getTopRight(), Side.TOP);
            case LEFT:
                if (fromSide == Side.TOP) return moveToSide(newSide, endSide, pointList, getBottomLeft(), Side.BOTTOM);
                else return moveToSide(newSide, endSide, pointList, getTopLeft(), Side.TOP);
            default: {
                return closePolyline(pointList, fromSide);
            }
        }
    }

    private final XYZ getBottomLeft() {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int nrows = axisY.length;
        XYZ xyz = new XYZ();
        xyz.x = axisX[0];
        xyz.y = axisY[0];
        xyz.z = gridData.getValueZ()[0][nrows-1];
        return xyz;
    }

    private final XYZ getTopLeft() {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int nrows = axisY.length;
        XYZ xyz = new XYZ();
        xyz.x = axisX[0];
        xyz.y = axisY[nrows-1];
        xyz.z = gridData.getValueZ()[0][0];
        return xyz;
    }

    private final XYZ getTopRight() {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int ncols = axisX.length;
        int nrows = axisY.length;
        XYZ xyz = new XYZ();
        xyz.x = axisX[ncols-1];
        xyz.y = axisY[nrows-1];
        xyz.z = gridData.getValueZ()[ncols-1][0];
        return xyz;
    }

    private final Direction getVerticalDirection(Point2D p, double prevLevel, double isolevel, Side side) {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int ncols = axisX.length;
        int nrows = axisY.length;
        int xIndex = side == Side.RIGHT ? ncols - 1 : 0;
        double step = (axisY[nrows - 1] - axisY[0]) / (nrows - 1);
        int yIndex =  (int) ((p.getY() - axisY[0]) / step);
        if (yIndex <= 0) return Direction.UP;
        if (yIndex >= (nrows-1)) return Direction.DOWN;
        double[][] z = gridData.getValueZ();
        double upperZ = z[xIndex][nrows - 1 - (yIndex + 1)];
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("getVerticalDirection yIndex=%d xIndexForZ=%d z(y+1)=%f z(y)=%f",
                    yIndex + 1, xIndex, upperZ, z[xIndex][nrows - 1 - (yIndex)]));
        }
        if (isZInInterval(upperZ, prevLevel, isolevel)) return Direction.UP;
        else return Direction.DOWN;
    }

    private final Direction getHorizontalDirection(Point2D p, double prevLevel, double isolevel, Side side) {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int ncols = axisX.length;
        int nrows = axisY.length;
        int yIndexForZ = side == Side.BOTTOM ? nrows - 1 : 0;
        double step = (axisX[ncols - 1] - axisX[0]) / (ncols - 1);
        int xIndex =  (int) ((p.getX() - axisX[0]) / step);
        if (xIndex <= 0) return Direction.RIGHT;
        if (xIndex >= (ncols-1)) return Direction.LEFT;
        double[][] z = gridData.getValueZ();
        double rightZ = z[xIndex + 1][yIndexForZ];
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("getHorizontalDirection xIndex=%d yIndexForZ=%d z(x+1)=%f z(x)=%f",
                    xIndex + 1, yIndexForZ, rightZ, z[xIndex][yIndexForZ]));
        }
        if (isZInInterval(rightZ, prevLevel, isolevel)) return Direction.RIGHT;
        else return Direction.LEFT;
    }


    private final XYZ getBottomRight() {
        double[] axisX = gridData.getAxX();
        double[] axisY = gridData.getAxY();
        int ncols = axisX.length;
        int nrows = axisY.length;
        XYZ xyz = new XYZ();
        xyz.x = axisX[ncols-1];
        xyz.y = axisY[0];
        xyz.z = gridData.getValueZ()[ncols-1][nrows-1];
        return xyz;
    }

    private List<Point2D> closePolyline(List<Point2D> pointList, Point2D pn) {
        pointList.add(pn);
        return pointList;
    }

    private List<Point2D> closePolyline(List<Point2D> pointList, Side side) {
        addPoint(pointList, pointList.get(0), side);
        return pointList;
    }

    private Side getSide(Point2D p1) {
        if (logger.isLoggable(Level.FINEST)) logger.finest("getSide p " + PolygonTools.point2DToWkt(p1));
        double x = p1.getX();
        double y = p1.getY();
        double dx1 = Math.abs(x - gridData.getMinX());
        double dx2 = Math.abs(gridData.getMaxX() - x);
        double min = dx2 < dx1 ? dx2 : dx1;
        if (logger.isLoggable(Level.FINEST)) logger.finest("getSide dx " + dx1 + " " + dx2 + " " + min);
        double dy1 = Math.abs(y - gridData.getMinY());
        if (dy1 < min) min = dy1;
        double dy2 = Math.abs(gridData.getMaxY() - y);
        if (dy2 < min) min = dy2;
        if (logger.isLoggable(Level.FINEST)) logger.finest("getSide dy " + dy1 + " " + dy2 + " " + min);
        if (min == dx1) return Side.LEFT;
        else if (min == dx2) return Side.RIGHT;
        else if (min == dy1) return Side.BOTTOM;
        else if (min == dy2) return Side.TOP;
        else throw new RuntimeException("strange exception");
    }

    private List<List<Point2D>> getLineListCopy(List<List<Point2D>> listOfPointList) {
        List<List<Point2D>> r = new ArrayList<>();
        for (List<Point2D> pointList : listOfPointList) {
            if (pointList.size() > 1) {
                if (!isClosed(pointList)) {
                    List<Point2D> copy = new ArrayList<>(pointList);
                    r.add(copy);
                }
            }
        }
        return r;
    }
}
