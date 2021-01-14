package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Created by: Mikhailov_KG
 * Date: 28.12.2020
 */
public interface IsolineMetadataCollection {
    IsolineMetadataCollection NULL = new IsolineMetadataCollection() {
        @Override
        public IsolineMetadata getPointMetadata(Point2D p) {
            return IsolineMetadata.NULL;
        }

        @Override
        public IsolineMetadata getLineMetadata(List<?> list) {
            return IsolineMetadata.NULL;
        }
    };


    IsolineMetadata getPointMetadata(Point2D p);

    IsolineMetadata getLineMetadata(List<?> list);
}
