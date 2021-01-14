package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class JtsTest {
    public static void main(String[] args) {
        new JtsTest().run();
    }

    private void run() {
        GeometryFactory factory = new GeometryFactory();
        float[] coord = new float[10];
        int i=0;
        coord[i++] = 0;
        coord[i++] = 0;

        coord[i++] = 0;
        coord[i++] = 10;

        coord[i++] = 10;
        coord[i++] = 10;

        coord[i++] = 10;
        coord[i++] = 0;

        coord[i++] = 0;
        coord[i++] = 0;

        CoordinateSequence points = new PackedCoordinateSequence.Float(coord, 2);
        LinearRing shell = new LinearRing(points, factory);
        Polygon polygon = new Polygon(shell, null, factory);
        System.err.println(polygon.toText());
    }
}
