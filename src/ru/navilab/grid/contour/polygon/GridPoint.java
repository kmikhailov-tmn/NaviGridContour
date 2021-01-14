package ru.navilab.grid.contour.polygon;

import java.util.Locale;
import java.util.Objects;

/**
 * Created by: Mikhailov_KG
 * Date: 22.07.2020
 */
public class GridPoint {
    private final double x;
    private final double y;
    private GridPoint next;
    private GridPoint prev;

    public GridPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public GridPoint getNext() {
        return next;
    }

    public void setNext(GridPoint next) {
        this.next = next;
    }

    public GridPoint getPrev() {
        return prev;
    }

    public void setPrev(GridPoint prev) {
        this.prev = prev;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridPoint gridPoint = (GridPoint) o;
        return Double.compare(gridPoint.x, x) == 0 &&
                Double.compare(gridPoint.y, y) == 0;
    }

    public boolean isDirectLinkedTo(GridPoint p) {
        return (prev == p || next == p);
    }

    public void removeBothLinks(GridPoint p) {
        if ((next == p) && (p.getPrev() == this)) {
            next = null;
            p.setPrev(null);
        } else if ((prev == p) && (p.getNext() == this)) {
            prev = null;
            p.setNext(null);
        }
    }


    public boolean hasNext() {
        return next != null;
    }

    public boolean hasPrev() {
        return prev != null;
    }

    /**
     * Working only when one link between two points is broken
     * @param p
     */
    public void reverseOrder(GridPoint p) {
        if (prev == p) {
            GridPoint savedNext = next;
            prev = savedNext;
            next = p;
            if (savedNext != null) savedNext.reverseOrder(this);
        } else if (next == p) {
            GridPoint savedPrev = prev;
            next = savedPrev;
            prev = p;
            if (savedPrev != null) savedPrev.reverseOrder(this);
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "POINT(%.1f %.1f)", x, y);
    }
}
