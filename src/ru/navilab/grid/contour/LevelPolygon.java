package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Polygon;

import java.util.List;

/**
 * Created by: Mikhailov_KG
 * Date: 04.08.2020
 */
public class LevelPolygon {
    private final float level;
    private final List<Polygon> polygonList;

    public LevelPolygon(float level, List<Polygon> polygonList) {
        this.level = level;
        this.polygonList = polygonList;
    }

    public float getLevel() {
        return level;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }
}
