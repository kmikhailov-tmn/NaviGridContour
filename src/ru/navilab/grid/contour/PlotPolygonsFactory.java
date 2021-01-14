package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import ru.navilab.grid.contour.polygon.GridContourPolygons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class PlotPolygonsFactory {
    public static GridPlotPolygons createPlotPolygons(GridAdapter gridData, PolygonConsumer consumer) {
        return new GridPlotPolygonsImpl(gridData, consumer);
    }

    public static GridPlotPolygons failedTry(GridAdapter gridData, PolygonConsumer consumer) {
        Logger logger = Logger.getLogger(PlotPolygonsFactory.class.getName());
        return new GridPlotPolygons() {
            @Override
            public void plotPolygon(double step) {
                double minZ = Math.max(gridData.getMinZ(), 0);
                double maxZ = gridData.getMaxZ();
                int count = (int) ((maxZ - minZ) / step);
                double min = minZ;
                GridContourPolygons gridContourPolygons = new GridContourPolygons(gridData);
                for (int i = 1; i <= count; i++) {
                    double max = Math.ceil(minZ + i * (maxZ - minZ) / count);
                    logger.fine("plot polygons " + min + " " + max);
                    Collection<Geometry> geometries = gridContourPolygons.drawPolygons(min, max);
                    consumer.consumeLevel(min, max, new ArrayList<>(geometries));
                    min = max;
                }
            }
        };
    }

    public static GridPlotPolygons createPlotPolygonsOld(GridAdapter gridData, PolygonConsumer consumer) {
        return new GridPlotPolygonsImpl(gridData, consumer);
    }

}
