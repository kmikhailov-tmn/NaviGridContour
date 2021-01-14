package ru.navilab.grid.contour.polygon;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.*;

/**
 * Created by: Mikhailov_KG
 * Date: 21.07.2020
 */
public final class GeomCellTable {
    private Map<Long,GeomCell> map = Collections.synchronizedMap(new Hashtable<>());

    public GeomCellTable() {
    }

    public void put(int column, int row, GeomCell cell) {
        long key = toKey(column, row);
        map.put(key, cell);
    }

    private long toKey(int column, int row) {
        return ((long)column << 32) + (long)row;
    }

    public GeomCell get(int column, int row) {
        return map.get(toKey(column, row));
    }

    public Collection<Geometry> getAllGeometries() {
        Collection<GeomCell> values = map.values();
        HashSet<GridTriangle> triangleHashSet = new HashSet<>();
        for (GeomCell cell : values) {
            addTriangle(triangleHashSet, cell.getTriangle1());
            addTriangle(triangleHashSet, cell.getTriangle2());
        }
        List<Geometry> geometries = new ArrayList<>(triangleHashSet.size());
        GeometryFactory geometryFactory = new GeometryFactory();
        for (GridTriangle gridTriangle : triangleHashSet) {
            Geometry geometry = gridTriangle.buildGeometry(geometryFactory);
            if (geometry != null) geometries.add(geometry);
        }
        return geometries;
    }

    private void addTriangle(HashSet<GridTriangle> triangleHashSet, GridTriangle triangle1) {
        if (triangle1 != null) {
            if (triangle1.getUnionTriangle() != null) {
                triangleHashSet.add(triangle1.getUnionTriangle());
            } else {
                triangleHashSet.add(triangle1);
            }
        }
    }
}
