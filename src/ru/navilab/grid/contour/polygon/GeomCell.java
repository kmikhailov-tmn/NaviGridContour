package ru.navilab.grid.contour.polygon;

import com.vividsolutions.jts.geom.Geometry;

import java.util.List;
import java.util.Objects;

/**
 * Created by: Mikhailov_KG
 * Date: 21.07.2020
 */
public class GeomCell {
    private final GridTriangle triangle1;
    private final GridTriangle triangle2;

    public GeomCell(GridTriangle triangle1, GridTriangle triangle2) {
        this.triangle1 = triangle1;
        this.triangle2 = triangle2;
    }

    public GridTriangle getTriangle1() {
        return triangle1;
    }

    public GridTriangle getTriangle2() {
        return triangle2;
    }
}
