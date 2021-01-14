package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * https://en.wikipedia.org/wiki/Marching_squares
 * Triangle version
 */
public final class GridContourIsolines2 {
    private final double[] axX;
    private final double[] axY;
    private IsolineConsumer isolineConsumer;
    private final double[][] z;
    private byte[][] lookupTable;
    private double isovalue;
    private int ncol;
    private int nrow;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private List<Point2D> pointList;


    public GridContourIsolines2(GridAdapter gridData) {
        axX = gridData.getAxX();
        axY = gridData.getAxY();
        ncol = axX.length;
        nrow = axY.length;
        z = gridData.getValueZ();
    }

    public final List<Point2D> drawIsoline(double isovalue) {
        this.isovalue = isovalue;

        byte[][] binaryImage = new byte[ncol][nrow];
        for (int c = 0; c < ncol; c++) {
            for (int r = 0; r < nrow; r++) {
                double val = z[c][r];
                if (val < isovalue) binaryImage[c][r] = 1;
            }
        }
        pointList = new ArrayList<>();
        for (int r = 0; r < nrow - 1; r++) {
            for (int c = 0; c < ncol - 1; c++) {
                pointList.addAll(triangle(binaryImage, c, r,
                        c+1, r,
                        c, r+1));
                pointList.addAll(triangle(binaryImage, c+1, r,
                        c+1, r+1,
                        c, r+1));
            }
        }
        return pointList;
    }

    private List<Point2D> triangle(byte[][] binaryImage, int c1, int r1, int c2, int r2, int c3, int r3) {
        int lookupCode = 2 * 2 * binaryImage[c1][r1];
        lookupCode += 2 * binaryImage[c2][r2];
        lookupCode += binaryImage[c3][r3];
        List<Point2D> pp = new ArrayList<>();
        switch (lookupCode) {
            case 0b111:
            case 0b000:
                break;
            case 0b011:
            case 0b100:
                pp.add(linearInterpolation(c1, r1, c3, r3));
                pp.add(linearInterpolation(c1, r1, c2, r2));
                break;
            case 0b101:
            case 0b010:
                pp.add(linearInterpolation(c1, r1, c2, r2));
                pp.add(linearInterpolation(c2, r2, c3, r3));
                break;
            case 0b110:
            case 0b001:
                pp.add(linearInterpolation(c1, r1, c3, r3));
                pp.add(linearInterpolation(c2, r2, c3, r3));
                break;
        }
        return pp;
    }


    private final Point2D.Double linearInterpolation(int c1, int r1, int c2, int r2) {
        double z1 = z[c1][r1];
        double z2 = z[c2][r2];
        double k = (isovalue - z1) / (z2 - z1);
        Point2D.Double p = new Point2D.Double();
        double y1 = axY[nrow - 1 - r1];
        double y2 = axY[nrow - 1 - r2];
        p.y = y1 + k * (y2 - y1);
        double x1 = axX[c1];
        double x2 = axX[c2];
        p.x = x1 + k * (x2 - x1);
        return p;
    }
}
