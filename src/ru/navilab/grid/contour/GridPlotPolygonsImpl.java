package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Logger;

import static com.vividsolutions.jts.operation.union.CascadedPolygonUnion.union;

public class GridPlotPolygonsImpl implements GridPlotPolygons {
    private final GridAdapter gridData;
    private final PolygonConsumer consumer;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static class L {
        double min;
        double max;
        List<Geometry> geomList = new ArrayList<>();

        public L(double min, double max, List<Geometry> geomList) {
            this.min = min;
            this.max = max;
            this.geomList = geomList;
        }
    }

    GridPlotPolygonsImpl(GridAdapter gridData, PolygonConsumer consumer) {
        this.gridData = gridData;
        this.consumer = consumer;
    }

    @Override
    public void plotPolygon(double step) {
        List<IsolevelLines> isolevelList = getIsolevelLines(step);
        IsolineCloser isolineCloser = new IsolineCloser(isolevelList, gridData);
        List<LevelPolygon> closedIsolines = isolineCloser.buildClosedIsolevelLines();
        diffAndPush2Consumer(closedIsolines);
    }

    private List<IsolevelLines> getIsolevelLines(double step) {
        PolylineConsumer polylineConsumer = new PolylineConsumer();
        GridContourIsolines contourIsolines = new GridContourIsolines(gridData, polylineConsumer);
        double minZ = getMinZ();
        double maxZ = gridData.getMaxZ();
        int count = (int) ((maxZ - minZ) / step);
        List<IsolevelLines> isolevelList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            float isolevel = (float) Math.ceil(minZ + i * (maxZ - minZ) / count);
            logger.fine("isolevel " + isolevel);
            contourIsolines.drawIsoline(isolevel);

            List<List<Point2D>> listOfPointList = polylineConsumer.getListOfPointList();
            isolevelList.add(new IsolevelLines(isolevel, new ArrayList<>(listOfPointList)));
            polylineConsumer.clear();
        }
        return isolevelList;
    }

    private double getMinZ() {
        return Math.max(gridData.getMinZ(), 0);
    }

    private void diffAndPush2Consumer(List<LevelPolygon> closedIsolines) {
        double prevLevel = getMinZ();
        List<Polygon> prevLevelPolygons = null;
        List<L> resultList = new ArrayList<>();
        for (LevelPolygon isolevel : closedIsolines) {
            float level = isolevel.getLevel();
            logger.fine("level " + level);
            List<Polygon> currentLevelPolygons = new ArrayList<>();
            List<Polygon> polygonList = isolevel.getPolygonList();
            for (Polygon polygon : polygonList) {
                logger.finest(polygon.getNumPoints() + " " + polygon.toText());
            }
            currentLevelPolygons.addAll(polygonList);
            logger.fine("isolevel " + level + " " + polygonList.size());
//            List<Polygon> currentLevelPolygons = PolygonTools.buildPolygonList(listOfPointList);
            if (prevLevelPolygons != null) {
                Level2PolygonTransformer transformer = new Level2PolygonTransformer();
                List<Geometry> levelPolygonList = transformer.transform(currentLevelPolygons, prevLevelPolygons);
                //sortGeom(levelPolygonList);
                if (levelPolygonList.size() > 0) {
                    //cleanDups(levelPolygonList);
                    resultList.add(new L(prevLevel, level, levelPolygonList));
                }
            }
            prevLevel = level;
            prevLevelPolygons = currentLevelPolygons;
        }

        sortL(resultList);
        for (L level : resultList) {
            consumer.consumeLevel(level.min, level.max, level.geomList);
        }
    }

    private void cleanDups(List<Geometry> levelPolygonList) {
        List<Integer> deleteList = new ArrayList<>();
        for (int i = 0; i < levelPolygonList.size(); i++) {
            for (int j = i+1; j < levelPolygonList.size(); j++) {
                try {
                    if (levelPolygonList.get(i).equals(levelPolygonList.get(j))) deleteList.add(i);
                } catch (TopologyException e) {
                    deleteList.add(i);
                }
            }
        }
        for (Integer integer : deleteList) {
            levelPolygonList.remove(integer);
        }
    }

    private void sortL(List<L> resultList) {
        Collections.sort(resultList, new Comparator<L>() {
            @Override
            public int compare(L o1, L o2) {
                try {
                    Geometry g1 = getGeometry(o1);
                    Geometry g2 = getGeometry(o2);
                    if (g1.equalsTopo(g2)) return 0;
                    return g1.covers(g2) ? -1 : 1;
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    public Geometry getGeometry(L l) {
        List<Geometry> g = l.geomList;
        Geometry geometry = new GeometryFactory().buildGeometry(g);
        return geometry.union();
    }

    private void sortGeom(List<Geometry> levelPolygonList) {
        Collections.sort(levelPolygonList, new Comparator<Geometry>() {
            @Override
            public int compare(Geometry o1, Geometry o2) {
                try {
                    if (o1 instanceof Polygon && o2 instanceof Polygon) {
                        Polygon p1 = (Polygon) o1;
                        Polygon p2 = (Polygon) o2;
                        if (p1.equals(p2)) return 0;
                        else return p1.getExteriorRing().covers(p2.getExteriorRing()) ? -1 : 1;
                    } else {
                        if (o1.equals(o2)) return 0;
                        else return o1.covers(o2) ? -1 : 1;
                    }
                } catch (TopologyException e) {
                    Geometry boundary1 = o1.getBoundary();
                    Geometry boundary2 = o2.getBoundary();
                    try {
                        if (boundary1.equalsTopo(boundary2)) return 0;
                        return boundary1.covers(boundary2) ? -1 : 1;
                    } catch (Exception e1) {
                        System.err.println("sort - giving up");
                        return 0;
                    }
                }
            }
        });
    }
}
