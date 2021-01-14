package ru.navilab.grid.contour;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

public final class HeightPolygon {
    private final double startLevel;
    private final double endLevel;
    private final List<Point2D> polygon;

    public HeightPolygon(double startLevel, double endLevel, List<Point2D> polygon) {
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.polygon = polygon;
    }

    public List<Point2D> getPolygon() {
        return polygon;
    }

    public double getStartLevel() {
        return startLevel;
    }

    public double getEndLevel() {
        return endLevel;
    }

    public final boolean contain(HeightPolygon slavePolygon) {
        List<Point2D> polygon1 = slavePolygon.getPolygon();
        Point2D p = polygon1.get(polygon1.size() / 2);
        return isPointInPolygon(p, polygon);
    }

    public final static boolean isPointInPolygon(Point2D p, List<Point2D> polygonPointList) {
        int k = 0;
        int i = 0;
        double x = p.getX();
        double y = p.getY();
        int size = polygonPointList.size();
        for (int j = size - 1; i < size; j = i++) {
            Point2D pi = polygonPointList.get(i);
            Point2D pj = polygonPointList.get(j);
            if (pi.equals(p) || pj.equals(p)) return true;
            double iy = pi.getY();
            double jy = pj.getY();
            double ix = pi.getX();
            double jx = pj.getX();
            if (((iy <= y) && (y < jy)) || ((jy <= y) && (y < iy))) {
                if (x < ((jx - ix) * (y - iy) / (jy - iy)) + ix) {
                    k++;
                }
            }
        }
        return k % 2 != 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeightPolygon that = (HeightPolygon) o;
        return that.polygon.equals(this.polygon);
    }

    @Override
    public int hashCode() {
        return polygon.hashCode();
    }
}
