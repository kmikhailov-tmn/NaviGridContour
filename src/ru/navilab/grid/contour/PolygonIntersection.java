package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

import java.util.*;
import java.util.logging.Logger;

import static ru.navilab.grid.contour.Level2PolygonTransformer.poly2str;

/**
 * Created by: Mikhailov_KG
 * Date: 31.07.2020
 */
public class PolygonIntersection {
    private List<Polygon> polygons;
    private Map<Polygon,Set<Polygon>> intersectMap = new Hashtable<>();
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public PolygonIntersection(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public List<Geometry> intersect() {
        List<Geometry> result = new ArrayList<>();
        Geometry[] geometries = polygons.toArray(new Geometry[polygons.size()]);
        for (int i = 0; i < geometries.length; i++) {
            Geometry current = geometries[i];
            if (current == null) continue;
            for (int j = i + 1; j < geometries.length; j++) {
                Geometry further = geometries[j];
                if (further == null) continue;
                try {
                    if (current.covers(further)) {
                        logger.finest("current.covers(further) c " + poly2str(current));
                        logger.finest("\t  f " + poly2str(further));
                        geometries[i] = current = current.difference(further);
                        logger.finest("\t  r " + poly2str(geometries[i]));
                        geometries[j] = null;
                    } else if (further.covers(current)) {
                        logger.finest("further.covers(current) f " + poly2str(further));
                        logger.finest("\t c " + poly2str(current));
                        geometries[j] = further = further.difference(current);
                        logger.finest("\t r " + poly2str(geometries[j]));
                        geometries[i] = null;
                        break;
                    } else if (current.intersects(further)) {
                        Geometry currentBoundary = current.getBoundary();
                        Geometry furtherBoundary = further.getBoundary();
                            logger.finest("current.intersects(further) c " + poly2str(current));
                        logger.finest("\t  f " + poly2str(further));
                        Geometry newGeom = current.intersection(further);
                        if (newGeom.getArea() > 0) {
                            geometries[i] = current = newGeom;
                            logger.finest("\t  r " + poly2str(geometries[i]));
                            geometries[j] = null;
                        } else {
                            if (current.getArea() > further.getArea()) {
                                geometries[j] = null;
                            } else {
                                geometries[i] = null;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(this.getClass().getName()).severe(e.getLocalizedMessage());
                }
            }
            if (geometries[i] != null) result.add(geometries[i]);
        }
        return result;
    }

    public Collection<Set<Polygon>> generateIntersectLists() {
        for (int i = 0; i < polygons.size(); i++) {
            Polygon p1 = polygons.get(i);
            for (int j = i+1; j < polygons.size(); j++) {
                Polygon p2 = polygons.get(j);
                try {
                    if (p1.intersects(p2)) remember(p1, p2);
                } catch (TopologyException e) {
                }
            }
        }
        Collection<Set<Polygon>> values = intersectMap.values();
        return values;
    }

    private void remember(Polygon p1, Polygon p2) {
        logger.finest("intersects " + p1.getNumPoints() + " " + p1.toText());
        logger.finest("intersectsVs " + p2.getNumPoints() + " " + p2.toText());
        Set<Polygon> polygons1 = intersectMap.get(p1);
        Set<Polygon> polygons2 = intersectMap.get(p2);
        if (polygons1 == null && polygons2 != null) {
            polygons1 = polygons2;
            intersectMap.put(p1, polygons1);
        }
        if (polygons2 == null && polygons1 != null) {
            polygons2 = polygons1;
            intersectMap.put(p2, polygons2);
        }
        if (polygons1 == null && polygons2 == null) {
            intersectMap.put(p1, polygons1 = new HashSet<>());
            polygons2 = polygons1;
            intersectMap.put(p2, polygons2);
        }
        if (polygons1 != null && polygons2 != null) {
            polygons1.addAll(polygons2);
            for (Polygon polygon : polygons2) {
                intersectMap.put(polygon, polygons1);
            }
        }
        polygons1.add(p1);
        polygons1.add(p2);
    }
}
