package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by: Mikhailov_KG
 * Date: 28.12.2020
 */
public class DebugPolylineConsumer extends PolylineConsumer
        implements IsolineMetadataConsumer {
    private Point2D.Double point;
    private Map<Point2D,IsolineMetadata> pointMetadata = Collections.synchronizedMap(new Hashtable<>());
    private Map<List<?>,IsolineMetadata> lineMetadata = Collections.synchronizedMap(new Hashtable<>());

    @Override
    public void consumePoint(double x, double y) {
        point = new Point2D.Double(x, y);
        list.add(point);
    }

    @Override
    public void endPolylineMetadata(IsolineMetadata metadata) {
        lineMetadata.put(list, metadata);
    }

    @Override
    public void consumePointMetadata(IsolineMetadata metadata) {
        pointMetadata.put(point, metadata);
    }


    public IsolineMetadataCollection getMetadataCollection() {
        return new IsolineMetadataCollection() {
            public IsolineMetadata getPointMetadata(Point2D p) {
                return pointMetadata.get(p);
            }

            public IsolineMetadata getLineMetadata(List<?> list) {
                return lineMetadata.get(list);
            }
        };
    }
}
