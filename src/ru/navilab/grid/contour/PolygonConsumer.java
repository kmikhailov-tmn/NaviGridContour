package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.geom.Point2D;
import java.util.List;

public interface PolygonConsumer {
    void consumeLevel(double startLevel, double endLevel, List<? extends Geometry> polygonList);
}
