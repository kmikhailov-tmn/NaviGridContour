package ru.navilab.grid.contour.old;


import java.awt.geom.Point2D;
import java.util.*;

public class TestPolygonConsumer implements OldPolygonConsumer {
    private Map<Double, List<List<Point2D>>> resultMap = new LinkedHashMap<>();
    private double startLevel;
    private double endLevel;
    private List<List<Point2D>> listOfPointList;

    @Override
    public void startPolygon(double startLevel, double endLevel) {
        System.err.println("start " + startLevel + " " + endLevel);
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        listOfPointList = new ArrayList<>();
        resultMap.put(endLevel, listOfPointList);
    }

    @Override
    public void consumePolygon(List<Point2D> polygon) {
        listOfPointList.add(polygon);
    }

    @Override
    public void endPolygon() {
    }

    public Map<Double, List<List<Point2D>>> getResultMap() {
        return resultMap;
    }
}
