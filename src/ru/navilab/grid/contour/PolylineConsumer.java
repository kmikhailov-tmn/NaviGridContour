package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PolylineConsumer implements IsolineConsumer {
    protected List<List<Point2D>> listOfPointList = new ArrayList<>();
    protected List<Point2D> list = new ArrayList<>();

    @Override
    public void startPolyline(double isovalue) {
        list = new ArrayList<>();
    }

    @Override
    public void consumePoint(double x, double y) {
        list.add(new Point2D.Double(x, y));
    }

    @Override
    public void endPolyline(double isovalue) {
        listOfPointList.add(list);
    }

    public List<List<Point2D>> getListOfPointList() {
        return listOfPointList;
    }

    public void clear() {
        listOfPointList.clear();
    }
}
