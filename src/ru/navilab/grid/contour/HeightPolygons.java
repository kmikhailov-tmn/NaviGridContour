package ru.navilab.grid.contour;

import java.util.ArrayList;
import java.util.List;

public class HeightPolygons {
    private double startLevel;
    private double endLevel;
    private List<HeightPolygon> polygonList = new ArrayList<>();

    public HeightPolygons(double startLevel, double endLevel, List<HeightPolygon> heightPolygonList) {
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.polygonList = heightPolygonList;
    }

    public HeightPolygon getFirst() {
        return polygonList.get(0);
    }

    public double getStartLevel() {
        return startLevel;
    }

    public double getEndLevel() {
        return endLevel;
    }

    public List<HeightPolygon> getPolygonList() {
        return polygonList;
    }
}
