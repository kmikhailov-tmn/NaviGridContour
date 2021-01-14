package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

public class PolyLevel {
    private final double startLevel;
    private final double endLevel;
    private final List<? extends Geometry> polygonList;

    public PolyLevel(double startLevel, double endLevel, List<? extends Geometry> polygonList) {
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.polygonList = polygonList;
    }

    public double getStartLevel() {
        return startLevel;
    }

    public double getEndLevel() {
        return endLevel;
    }

    public List<? extends Geometry> getPolygonList() {
        return polygonList;
    }
}
