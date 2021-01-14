package ru.navilab.grid.contour;


public interface IsolineConsumer {
    void startPolyline(double isovalue);
    void consumePoint(double x, double y);
    void endPolyline(double isovalue);
}
