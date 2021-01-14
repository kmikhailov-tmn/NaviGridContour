package ru.navilab.grid.contour;

/**
 * Created by: Mikhailov_KG
 * Date: 28.12.2020
 */
public interface IsolineMetadataConsumer {
    void endPolylineMetadata(IsolineMetadata metadata);
    void consumePointMetadata(IsolineMetadata metadata);
}
