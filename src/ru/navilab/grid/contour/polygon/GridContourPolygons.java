package ru.navilab.grid.contour.polygon;

import com.vividsolutions.jts.geom.Geometry;
import ru.navilab.grid.contour.GridAdapter;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * https://en.wikipedia.org/wiki/Marching_squares
 * Triangle version of isobands
 */
public final class GridContourPolygons {
    private static final int C_222 = 26;
    public static final int C_000 = 0;
    public static final int C_022 = 8;
    public static final int C_200 = 18;
    public static final int C_011 = 4;
    public static final int C_211 = 22;
    public static final int C_111 = 13;
    public static final int C_202 = 20;
    public static final int C_020 = 6;
    public static final int C_101 = 10;
    public static final int C_121 = 16;
    public static final int C_220 = 24;
    private static final int C_002 = 2;
    public static final int C_110 = 12;
    public static final int C_112 = 14;

    public static final int C_122 = 17;
    public static final int C_100 = 9;

    public static final int C_212 = 23;
    public static final int C_010 = 3;

    public static final int C_221 = 25;
    public static final int C_001 = 1;

    public static final int C_102 = 11;
    public static final int C_120 = 15;

    public static final int C_021 = 7;
    public static final int C_201 = 19;

    public static final int C_210 = 21;
    public static final int C_012 = 5;

    private final double[] axX;
    private final double[] axY;
    private final double[][] z;
    private int ncol;
    private int nrow;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private double min;
    private double max;
    private double[] xyarr = new double[6*2];
    private int arrIndex = 0;
    private boolean debugMode;
    private int debugCount = 1;

    public GridContourPolygons(GridAdapter gridData) {
        axX = gridData.getAxX();
        axY = gridData.getAxY();
        ncol = axX.length;
        nrow = axY.length;
        z = gridData.getValueZ();
    }

    public final Collection<Geometry> drawPolygons(double min, double max) {
        this.min = min;
        this.max = max;
        byte[][] binaryImage = createBinaryImage(min, max);
        GeomCellTable geomCellTable = new GeomCellTable();
        int count = 0;
        full: for (int r = 0; r < nrow - 1; r++) {
            for (int c = 0; c < ncol - 1; c++) {
                GridTriangle triangle1 = triangle(binaryImage, c, r, c + 1, r, c, r + 1);// left upper triangle
                if (debugMode) {
                    if (arrIndex > 0) count++;
                    if (count >= debugCount) break full;
                }
                GridTriangle triangle2 = triangle(binaryImage, c + 1, r, c + 1, r + 1, c, r + 1);// right lower triangle

                if (triangle1 != null || triangle2 != null) {
                    if (triangle1 != null) {
                        GeomCell leftCell = getLeftCell(geomCellTable, c, r);
                        if (leftCell != null) {
                            if (leftCell.getTriangle2() != null) {
                                leftCell.getTriangle2().union(triangle1);
                            }
                        }
                        GeomCell bottomCell = getBottomCell(geomCellTable, c, r);
                        if (bottomCell != null) {
                            if (bottomCell.getTriangle2() != null) {
                                bottomCell.getTriangle2().union(triangle1);
                            }
                        }
                        if (triangle2 != null) triangle1.union(triangle2);
                    }
                    GeomCell geomCell = new GeomCell(triangle1, triangle2);
                    geomCellTable.put(c, r, geomCell);
                }
                if (debugMode) {
                    if (arrIndex > 0) count++;
                    if (count >= debugCount) break full;
                }
            }
        }
        return geomCellTable.getAllGeometries();
    }

    private GeomCell getBottomCell(GeomCellTable geomCellTable, int c, int r) {
        if (r-1 >= 0) return geomCellTable.get(c, r-1);
        return null;
    }

    private GeomCell getLeftCell(GeomCellTable geomCellTable, int c, int r) {
        if (c-1 >= 0) return geomCellTable.get(c - 1, r);
        return null;
    }

    private byte[][] createBinaryImage(double min, double max) {
        byte[][] binaryImage = new byte[ncol][nrow];

        for (int c = 0; c < ncol; c++) {
            for (int r = 0; r < nrow; r++) {
                double val = z[c][r];
                if (val < min) binaryImage[c][r] = 0;
                else if (val > max) binaryImage[c][r] = 2;
                else binaryImage[c][r] = 1;
            }

        }
        return binaryImage;
    }

    /* saved - do not remove
    private Geometry inflate(Geometry geom) {
        BufferParameters bufferParameters = new BufferParameters();
        bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
        bufferParameters.setJoinStyle(BufferParameters.JOIN_MITRE);
        Geometry buffered = BufferOp.bufferOp(geom,.0001, bufferParameters);
        buffered.setUserData(geom.getUserData());
        return buffered;
    }*/

    private GridTriangle triangle(byte[][] binaryImage, int c1, int r1, int c2, int r2, int c3, int r3) {
        arrIndex = 0;
        int lookupCode = toBase3(binaryImage[c1][r1], binaryImage[c2][r2], binaryImage[c3][r3]);
        switch (lookupCode) {
            case C_222:
            case C_000:
                break;
            case C_022:
            case C_200:
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c1, r1, c3, r3, max);
                linearInterpolation(c1, r1, c2, r2, max);
                linearInterpolation(c1, r1, c2, r2, min);
                break;
            case C_011:
            case C_211:
                linearInterpolation(c1, r1, c3, r3, min);
                addXY(axX[c3], axY[nrow - 1 - r3]);
                addXY(axX[c2], axY[nrow - 1 - r2]);
                linearInterpolation(c1, r1, c2, r2, min);
                break;

            case C_111:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                addXY(axX[c2], axY[nrow - 1 - r2]);
                addXY(axX[c3], axY[nrow - 1 - r3]);
                break;

            case C_202:
            case C_020:
                linearInterpolation(c1, r1, c2, r2, min);
                linearInterpolation(c1, r1, c2, r2, max);
                linearInterpolation(c2, r2, c3, r3, max);
                linearInterpolation(c2, r2, c3, r3, min);
                break;
            case C_101:
            case C_121:
                linearInterpolation(c1, r1, c2, r2, min);
                addXY(axX[c1], axY[nrow - 1 - r1]);
                addXY(axX[c3], axY[nrow - 1 - r3]);
                linearInterpolation(c2, r2, c3, r3, min);
                break;

            case C_220:
            case C_002:
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c3, r3, c1, r1, max);
                linearInterpolation(c3, r3, c2, r2, max);
                linearInterpolation(c3, r3, c2, r2, min);
                break;
            case C_110:
            case C_112:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                linearInterpolation(c3, r3, c1, r1, min);
                linearInterpolation(c3, r3, c2, r2, min);
                addXY(axX[c2], axY[nrow - 1 - r2]);
                break;

            case C_122:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c1, r1, c2, r2, min);
                break;

            case C_100:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c1, r1, c2, r2, min);
                break;

            case C_212:
                addXY(axX[c2], axY[nrow - 1 - r2]);
                linearInterpolation(c2, r2, c1, r1, min);
                linearInterpolation(c2, r2, c3, r3, min);
                break;

            case C_010:
                addXY(axX[c2], axY[nrow - 1 - r2]);
                linearInterpolation(c2, r2, c1, r1, min);
                linearInterpolation(c2, r2, c3, r3, min);
                break;


            case C_221:
                addXY(axX[c3], axY[nrow - 1 - r3]);
                linearInterpolation(c3, r3, c1, r1, min);
                linearInterpolation(c3, r3, c2, r2, min);
                break;

            case C_001:
                addXY(axX[c3], axY[nrow - 1 - r3]);
                linearInterpolation(c3, r3, c1, r1, min);
                linearInterpolation(c3, r3, c2, r2, min);
                break;

            /* pentagons */
            case C_102:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                linearInterpolation(c1, r1, c3, r3, max);
                linearInterpolation(c3, r3, c2, r2, max);
                linearInterpolation(c3, r3, c2, r2, min);
                linearInterpolation(c2, r2, c1, r1, min);
                break;
            case C_120:
                addXY(axX[c1], axY[nrow - 1 - r1]);
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c3, r3, c2, r2, min);
                linearInterpolation(c3, r3, c2, r2, max);
                linearInterpolation(c2, r2, c1, r1, max);
                break;
            case C_021:
                addXY(axX[c3], axY[nrow - 1 - r3]);
                linearInterpolation(c3, r3, c2, r2, max);
                linearInterpolation(c2, r2, c1, r1, max);
                linearInterpolation(c2, r2, c1, r1, min);
                linearInterpolation(c3, r3, c1, r1, min);
                break;
            case C_201:
                addXY(axX[c3], axY[nrow - 1 - r3]);
                linearInterpolation(c3, r3, c2, r2, min);
                linearInterpolation(c2, r2, c1, r1, min);
                linearInterpolation(c2, r2, c1, r1, max);
                linearInterpolation(c3, r3, c1, r1, max);
                break;
            case C_210:
                addXY(axX[c2], axY[nrow - 1 - r2]);
                linearInterpolation(c2, r2, c1, r1, max);
                linearInterpolation(c1, r1, c3, r3, max);
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c3, r3, c2, r2, min);
                break;
            case C_012:
                addXY(axX[c2], axY[nrow - 1 - r2]);
                linearInterpolation(c2, r2, c1, r1, min);
                linearInterpolation(c1, r1, c3, r3, min);
                linearInterpolation(c1, r1, c3, r3, max);
                linearInterpolation(c3, r3, c2, r2, max);
                break;
        }
        if (arrIndex == 0) return null;
        return new GridTriangle(xyarr, arrIndex);
    }

    private final static int toBase3(int p1, int p2, int p3) {
        int lookupCode = 3 * 3 * p1;
        lookupCode += 3 * p2;
        lookupCode += p3;
        return lookupCode;
    }

    private final void linearInterpolation(int c1, int r1, int c2, int r2, double v) {
        double z1 = z[c1][r1];
        double z2 = z[c2][r2];
        double k = (v - z1) / (z2 - z1);
        if (k > 1) k = 1;
        else if (k < -1) k = -1;
        double y1 = axY[nrow - 1 - r1];
        double y2 = axY[nrow - 1 - r2];
        double x1 = axX[c1];
        double x2 = axX[c2];
        addXY(x1 + k * (x2 - x1), y1 + k * (y2 - y1));
    }

    private void addXY(double x, double y) {
        xyarr[arrIndex++] = x;
        xyarr[arrIndex++] = y;
    }

    private final boolean isInBounds(int newX, int newY) {
        return newX >= 0 && newY >= 0 && (newX < (ncol - 1)) && (newY < (nrow - 1));
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDebugCount(int debugCount) {
        this.debugCount = debugCount;
    }
}
