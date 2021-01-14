package ru.navilab.grid.contour.old;

import java.awt.geom.Point2D;
import java.util.List;

public interface OldPolygonConsumer {
    void startPolygon(double startLevel, double endLevel);

    void consumePolygon(List<Point2D> polygon);

    void endPolygon();
}
