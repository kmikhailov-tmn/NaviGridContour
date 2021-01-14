package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by: Mikhailov_KG
 * Date: 31.07.2020
 */
public class Level2PolygonTransformer {


    private Logger logger = Logger.getLogger(this.getClass().getName());

    public List<Geometry> transform(List<Polygon> currentLevelPolygons, List<Polygon> prevLevelPolygons) {
        PolygonIntersection intersectionCur = new PolygonIntersection(currentLevelPolygons);
        List<Geometry> currentPolygons = intersectionCur.intersect();
        for (Geometry currentPolygon : currentPolygons) {
            logger.finest(poly2str(currentPolygon));
        }
        logger.finest("intersect prev");
        PolygonIntersection intersectionPrev = new PolygonIntersection(prevLevelPolygons);
        List<Geometry> prevPolygons = intersectionPrev.intersect();
        for (Geometry p : prevPolygons) {
            logger.finest(poly2str(p));
        }
        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < currentPolygons.size(); i++) {
            Geometry current = currentPolygons.get(i);
//            List<Geometry> diffList = new ArrayList<>();
            for (int j = 0; j < prevPolygons.size(); j++) {
                Geometry prev = prevPolygons.get(j);
                try {
                    if (current.intersects(prev)) {
                        logger.finest("current.intersects(prev) cur = " + poly2str(current));
                        logger.finest("current.intersects(prev) prev = " + poly2str(prev));
                        Geometry symDifference = current.symDifference(prev);
                        logger.finest("symDifference = " + poly2str(symDifference));
//                        diffList.add(symDifference);
                        current = symDifference;
                    }
                } catch (Exception e) {
                    Logger.getLogger(this.getClass().getName()).severe(e.getLocalizedMessage());
//                    diffList.add(prev);
                }
            }
            result.add(current);
//            if (diffList.size() > 0) result.addAll(diffList);
//            else result.add(current);
        }
        return result;
    }

    static String poly2str(Geometry current) {
        return current.getNumPoints() + " " + current.toText();
    }

}
