package ru.navilab.grid.contour.old;

import ru.navilab.grid.contour.*;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Logger;

public final class OldGridPlotPolygons {
    private GridAdapter gridData;
    private OldPolygonConsumer polygonConsumer;
    private Logger logger = Logger.getLogger(this.getClass().getName());


    public OldGridPlotPolygons(GridAdapter gridData, OldPolygonConsumer polygonConsumer) {
        this.gridData = gridData;
        this.polygonConsumer = polygonConsumer;
    }

    public void plotPolygon(float step) {
        PolylineConsumer polylineConsumer = new PolylineConsumer();
        GridContourIsolines contourIsolines = new GridContourIsolines(gridData, polylineConsumer);
        double minZ = Math.max(gridData.getMinZ(), 0);
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

        IsolineCloser isolineCloser = new IsolineCloser(isolevelList, gridData);
//        drop this code
//        List<IsolevelLines> closedIsolevelLines = isolineCloser.buildClosedIsolevelLines();

//        List<HeightPolygons> unorderedPolygonList = group(closedIsolevelLines);

//        List<HeightPolygons> orderedList = PolygonOrderer.order(unorderedPolygonList);
//        pushPolygonsToConsumer(orderedList);
    }

    private void pushPolygonsToConsumer(List<HeightPolygons> orderedList) {
        for (HeightPolygons heightPolygons : orderedList) {
            polygonConsumer.startPolygon(heightPolygons.getStartLevel(), heightPolygons.getEndLevel());
            List<HeightPolygon> polygonList = heightPolygons.getPolygonList();
            for (HeightPolygon heightPolygon : polygonList) {
                polygonConsumer.consumePolygon(heightPolygon.getPolygon());
            }
            polygonConsumer.endPolygon();
        }
    }

    private List<HeightPolygons> group(List<IsolevelLines> closedIsolines) {
        List<HeightPolygons> result = new ArrayList<>();
        double minZ = Math.max(gridData.getMinZ(), 0);
        double prevLevel = minZ;
        for (IsolevelLines isolevel : closedIsolines) {
            float level = isolevel.getIsolevel();
            List<List<Point2D>> listOfPointList = isolevel.getListOfPointList();
            logger.fine("isolevel " + isolevel.getIsolevel() + " " + listOfPointList.size());
            List<HeightPolygon> polygonList = new ArrayList<>();
            for (List<Point2D> polygon : listOfPointList) {
                HeightPolygon heightPolygon = new HeightPolygon(prevLevel, level, polygon);
                polygonList.add(heightPolygon);
            }
            result.add(new HeightPolygons(prevLevel, level, polygonList));
            prevLevel = level;
        }
        return result;
    }
}