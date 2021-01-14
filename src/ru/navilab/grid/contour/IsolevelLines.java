package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.List;

public class IsolevelLines {
    private final float isolevel;
    private final List<List<Point2D>> listOfPointList;

    public IsolevelLines(float isolevel, List<List<Point2D>> listOfPointList) {
        this.isolevel = isolevel;
        this.listOfPointList = listOfPointList;
    }

    public float getIsolevel() {
        return isolevel;
    }

    public List<List<Point2D>> getListOfPointList() {
        return listOfPointList;
    }
}
