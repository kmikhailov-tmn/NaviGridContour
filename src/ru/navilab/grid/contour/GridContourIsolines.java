package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * https://en.wikipedia.org/wiki/Marching_squares
 */
public final class GridContourIsolines {
    private final double[] axX;
    private final double[] axY;
    private final double missingZ;
    private IsolineConsumer isolineConsumer;
    private final double[][] z;
    private byte[][] lookupTable;
    private double isovalue;
    private int ncol;
    private int nrow;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private static class PathElement {
        int col;
        int row;
        private byte lookupCode;
        private Direction direction;

        public PathElement(int col, int row, byte lookupCode, Direction direction) {
            this.col = col;
            this.row = row;
            this.lookupCode = lookupCode;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof PathElement)) return false;
            PathElement elem = (PathElement) obj;
            return elem.col == col && elem.row == row;
        }
    }

    public GridContourIsolines(GridAdapter gridData, IsolineConsumer isolineConsumer) {
        axX = gridData.getAxX();
        axY = gridData.getAxY();
        ncol = axX.length;
        nrow = axY.length;
        z = gridData.getValueZ();
        missingZ = gridData.getMissingZ();
        this.isolineConsumer = isolineConsumer;
    }

    public final void drawIsoline(double isovalue) {
        this.isovalue = isovalue;

        byte[][] binaryImage = new byte[ncol][nrow];
        for (int c = 0; c < ncol; c++) {
            for (int r = 0; r < nrow; r++) {
                double val = z[c][r];
                if (val == missingZ) continue;
                if (val < isovalue) binaryImage[c][r] = 1;
            }
        }
        lookupTable = new byte[ncol][nrow];
        for (int r = 0; r < nrow - 1; r++) {
            for (int c = 0; c < ncol - 1; c++) {
                byte leftTop = binaryImage[c][r];
                byte rightTop = binaryImage[c+1][r];
                byte rightBottom = binaryImage[c+1][r+1];
                byte leftBottom = binaryImage[c][r+1];
                byte result = (byte)(leftBottom & 0b1);
                if (rightBottom > 0) result |= 0b10;
                if (rightTop > 0) result |= 0b100;
                if (leftTop > 0) result |= 0b1000;
                lookupTable[c][r] = result;
            }
        }

        int pathCount = 0;
        for (int r = 0; r < nrow - 1; r++) {
            for (int c = 0; c < ncol - 1; c++) {
                byte lookupCode = lookupTable[c][r];
                switch (lookupCode) {
                    case 1:
                    case 6:
                    case 9:
                    case 14:
                        addPath(startPath(c, r, Direction.DOWN));
                        pathCount++;
                        break;
                    case 2:
                    case 5:
                    case 10:
                    case 13:
                        List<PathElement> path1 = startPath(c, r, Direction.DOWN);
                        if (path1.size() == 1 || isClosedPath(path1)) addPath(path1);
                        else {
                            List<PathElement> path2 = startPath(c, r, Direction.RIGHT);
                            if (path2.size() > 1) {
                                Collections.reverse(path1);
                                makePath(path1, path2);
                            } else {
                                addPath(path1);
                            }
                        }
                        pathCount++;
                        break;
                    case 3:
                    case 4:
                    case 11:
                    case 12:
                        addPath(startPath(c, r, Direction.RIGHT));
                        pathCount++;
                        break;
                }
            }
        }
    }

    private void makePath(List<PathElement> path1, List<PathElement> path2) {
        isolineConsumer.startPolyline(isovalue);
        int path2size = path2.size();
        if (path2size == 0) path1.remove(path1.size() - 1);
        drawPath(path1, false);
        if (path2size > 0) drawPath(path2, true);
        isolineConsumer.endPolyline(isovalue);
    }

    private void drawPath(List<PathElement> path1, boolean reverseDirection) {
        Point2D.Double p1 = null;
        Point2D.Double p2 = new Point2D.Double();
        int i=0;
        for (PathElement e : path1) {
            if (i++ == 0 && reverseDirection) continue; // skip first element of path2
            Direction direction = e.direction;
            if (reverseDirection) direction = direction.reversed();
            Side s1 = null;
            Side s2 = null;
            switch (e.lookupCode) {
                case 1:
                case 14:
                    if (direction == Direction.RIGHT) {
                        s1 = Side.LEFT; s2 = Side.BOTTOM;
                    } else {
                        s2 = Side.LEFT; s1 = Side.BOTTOM;
                    }
                    break;
                case 2:
                case 13:
                    if (direction == Direction.UP) {
                        s1 = Side.BOTTOM; s2 = Side.RIGHT;
                    } else {
                        s2 = Side.BOTTOM; s1 = Side.RIGHT;
                    }
                    break;
                case 3:
                case 12:
                    if (direction == Direction.RIGHT) {
                        s1 = Side.LEFT; s2 = Side.RIGHT;
                    } else {
                        s2 = Side.LEFT; s1 = Side.RIGHT;
                    }
                    break;
                case 4:
                case 11:
                    if (direction == Direction.DOWN || direction == Direction.RIGHT) {
                        s1 = Side.TOP; s2 = Side.RIGHT;
                    } else {
                        s2 = Side.TOP; s1 = Side.RIGHT;
                    }
                    break;
                case 5:
                    if (direction == Direction.RIGHT) {
                        s1 = Side.LEFT; s2 = Side.TOP;
                    } else if (direction == Direction.DOWN) {
                        s2 = Side.LEFT; s1 = Side.TOP;
                    } else if (direction == Direction.LEFT) {
                        s1 = Side.RIGHT; s2 = Side.BOTTOM;
                    } else {
                        s2 = Side.RIGHT; s1= Side.BOTTOM;
                    }
                    break;
                case 6:
                case 9:
                    if (direction == Direction.UP) {
                        s1 = Side.BOTTOM; s2 = Side.TOP;
                    } else {
                        s2 = Side.BOTTOM; s1 = Side.TOP;
                    }
                    break;
                case 7:
                case 8:
                    if (direction == Direction.RIGHT) {
                        s1 = Side.LEFT; s2 = Side.TOP;
                    } else {
                        s2 = Side.LEFT; s1 = Side.TOP;
                    }
                    break;
                case 10:
                    if (direction == Direction.RIGHT) {
                        s1 = Side.LEFT; s2 = Side.BOTTOM;
                    } else if (direction == Direction.UP) {
                        s2 = Side.LEFT; s1 = Side.BOTTOM;
                    } else if (direction == Direction.LEFT) {
                        s1 = Side.RIGHT; s2 = Side.TOP;
                    } else {
                        s2 = Side.RIGHT; s1 = Side.TOP;
                    }
                    break;
            }
            if (p1 == null) {
                p1 = new Point2D.Double();
                interpolate(e, s1, p1);
                if (!reverseDirection) isolineConsumer.consumePoint(p1.x, p1.y);
            } else { // copy previous values
                p1.x = p2.x;
                p1.y = p2.y;
            }
            interpolate(e, s2, p2);
            isolineConsumer.consumePoint(p2.x, p2.y);
        }
    }

    private void interpolate(PathElement e, Side side, Point2D.Double p) {
        switch (side) {
            case LEFT: interpolateLeft(p, e); break;
            case RIGHT: interpolateRight(p, e); break;
            case TOP: interpolateTop(p, e); break;
            case BOTTOM: interpolateBottom(p, e); break;
        }
    }

    private void interpolateTop(Point2D.Double p1, PathElement e) {
        linearInterpolation(p1, e.col, e.row, e.col+1, e.row);
    }

    private void interpolateRight(Point2D.Double p2, PathElement e) {
        linearInterpolation(p2, e.col+1, e.row, e.col+1, e.row+1);
    }

    private void interpolateBottom(Point2D.Double p2, PathElement e) {
        linearInterpolation(p2, e.col, e.row+1, e.col+1, e.row+1);
    }

    private void interpolateLeft(Point2D.Double p1, PathElement e) {
        linearInterpolation(p1, e.col, e.row, e.col, e.row+1);
    }

    private final void linearInterpolation(Point2D.Double p, int c1, int r1, int c2, int r2) {
        double z1 = z[c1][r1];
        double z2 = z[c2][r2];
        double k = 0;
        if (z1 != missingZ && z2 != missingZ && z2 != z1) {
            k = (isovalue - z1) / (z2 - z1);
        }
        if (c1 == c2) {
            p.x = axX[c1];
            double y1 = axY[nrow-1-r1];
            double y2 = axY[nrow-1-r2];
            p.y = y1 + k * (y2 - y1);
        } else {
            p.y = axY[nrow-1-r1];
            double x1 = axX[c1];
            double x2 = axX[c2];
            p.x = x1 + k * (x2 - x1);
        }
    }

    private final static boolean isClosedPath(List<PathElement> path) {
        PathElement pe1 = path.get(0);
        PathElement pe2 = path.get(path.size() - 1);
        return pe1.equals(pe2);
    }

    private void addPath(List<PathElement> path1) {
        makePath(path1, Collections.EMPTY_LIST);
    }

    private List<PathElement> startPath(int c, int r, Direction direction) {
        List<PathElement> list = new ArrayList<PathElement>();
        byte startLookupCode = disambiguate(c, r, lookupTable[c][r]);
        PathElement element = new PathElement(c, r, startLookupCode, direction);
        list.add(element);
        int newX = c + direction.getDx();
        int newY = r + direction.getDy();
        while (direction != Direction.DONE && isInBounds(newX, newY)) {
            byte lookupCode = lookupTable[newX][newY];
            if (lookupCode != 0 && lookupCode != 15) {
                byte lookupCodeOk = disambiguate(c, r, lookupCode);
                list.add(new PathElement(newX, newY, lookupCodeOk, direction));
            }
            direction = getNext(direction, newX, newY);
            eraseLookupCode(direction, newX, newY);
            newX += direction.getDx();
            newY += direction.getDy();
            if (newX == c && newY == r) {
                list.add(new PathElement(newX, newY, startLookupCode, direction)); // for flagging that path is closed
                break;
            }
        }
        return list;
    }

    private void eraseLookupCode(Direction direction, int column, int row) {
        byte lookupCode = disambiguate(column, row, lookupTable[column][row]);
        switch (lookupCode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            case 12:
            case 13:
            case 14:
                lookupTable[column][row] = 0;
                break;
            case 5:
                if (direction == Direction.DOWN || direction == Direction.RIGHT) lookupTable[column][row] = 2;
                else if (direction == Direction.UP || direction == Direction.LEFT) lookupTable[column][row] = 8;
                break;
            case 10:
                if (direction == Direction.DOWN || direction == Direction.RIGHT) lookupTable[column][row] = 1;
                else if (direction == Direction.UP || direction == Direction.LEFT) lookupTable[column][row] = 4;
                break;
        }
    }

    private final boolean isInBounds(int newX, int newY) {
        return newX >= 0 && newY >= 0 && (newX < (ncol - 1)) && (newY < (nrow - 1));
    }

    private Direction getNext(Direction direction, int column, int row) {
        byte lookupCode = lookupTable[column][row];
        lookupCode = disambiguate(column, row, lookupCode);
        switch (lookupCode) {
            case 0:
            case 15: return Direction.DONE;
            case 1:
            case 14:
                if (direction == Direction.RIGHT) return Direction.DOWN;
                else return Direction.LEFT;
            case 2:
            case 13:
                if (direction == Direction.UP) return Direction.RIGHT;
                else return Direction.DOWN;
            case 3:
            case 12:
                if (direction == Direction.RIGHT) return Direction.RIGHT;
                else return Direction.LEFT;
            case 4:
            case 11:
                if (direction == Direction.DOWN) return Direction.RIGHT;
                else return Direction.UP;
            case 5:
                if (direction == Direction.DOWN) return Direction.LEFT;
                else if (direction == Direction.LEFT) return Direction.DOWN;
                else if (direction == Direction.UP) return Direction.RIGHT;
                else if (direction == Direction.RIGHT) return Direction.UP;
            case 6:
            case 9:
                if (direction == Direction.DOWN) return Direction.DOWN;
                else return Direction.UP;
            case 7:
            case 8:
                if (direction == Direction.DOWN) return Direction.LEFT;
                else return Direction.UP;
            case 10:
                if (direction == Direction.DOWN) return Direction.RIGHT;
                else if (direction == Direction.LEFT) return Direction.UP;
                else if (direction == Direction.UP) return Direction.LEFT;
                else if (direction == Direction.RIGHT) return Direction.DOWN;
        }
        return Direction.DONE;
    }

    private byte disambiguate(int column, int row, byte lookupCode) {
        if (lookupCode == 5) {
            float centralValue = getCentralValue(column, row);
            if (centralValue < isovalue) lookupCode = 10;
        } else if (lookupCode == 10) {
            float centralValue = getCentralValue(column, row);
            if (centralValue < isovalue) lookupCode = 5;
        }
        return lookupCode;
    }

    /**
     *
     * @return simple average
     */
    private final float getCentralValue(int c, int r) {
        double sum = 0;
        int count = 0;

        double v = z[c][r];
        if (v != missingZ) {
            sum += v;
            count++;
        }

        v = z[c+1][r];
        if (v != missingZ) {
            sum += v;
            count++;
        }

        v = z[c+1][r+1];
        if (v != missingZ) {
            sum += v;
            count++;
        }

        v = z[c][r+1];
        if (v != missingZ) {
            sum += v;
            count++;
        }

        float avg = (float) z[c][r];
        if (count > 0) avg = (float) (sum / count);
        if (logger.isLoggable(Level.FINER)) logger.finer("getCentralValue " + c + " " + r + " = " + avg);
        return avg;
    }
}
