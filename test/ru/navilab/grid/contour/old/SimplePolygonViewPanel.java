package ru.navilab.grid.contour.old;

import ru.navilab.grid.contour.IsolineMetadataCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class SimplePolygonViewPanel extends JPanel {
    private double dw;
    private double dh;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private int w;
    private int h;
    private boolean first = true;
    private Map<Double, List<List<Point2D>>> resultMap;
    private Double isolevel = -1d;
    private IsolineMetadataCollection metadataCollection = IsolineMetadataCollection.NULL;

    public SimplePolygonViewPanel(Map<Double, List<List<Point2D>>> resultMap) {
        this.resultMap = resultMap;
        Set<Double> isolevelList = resultMap.keySet();
        for (Double isolevel : isolevelList) {
            List<List<Point2D>> polylines = resultMap.get(isolevel);
            for (List<Point2D> list : polylines) {
                for (Point2D p : list) {
                    addPoint(p);
                }
            }
        }
        init();
    }


    private void init() {
        dw = maxX - minX;
        dh = maxY - minY;
    }

    private void addPoint(Point2D p) {
        double x = p.getX();
        double y = p.getY();
        if (first) {
            maxX = minX = x;
            maxY = minY = y;
            first = false;
        } else {
            if (x > maxX) maxX = x;
            if (x < minX) minX = x;
            if (y > maxY) maxY = y;
            if (y < minY) minY = y;
        }
    }

    public double toMapY(int y) {
        return minY - (y - h - 25) * dh / h;
    }

    public double toMapX(int x) {
        return (x - 25) * dw / w + minX;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        w = size.width - 50;
        h = size.height - 50;

        g2.setColor(Color.BLACK);
        List<GeneralPath> selectedList = new ArrayList<>();
        Set<Double> keySet = resultMap.keySet();
        for (Double isolevel : keySet) {
            List<List<Point2D>> listOfPointList = resultMap.get(isolevel);
            for (List<Point2D> pointList : listOfPointList) {
                GeneralPath gp = new GeneralPath();
                int i = 0;
                for (Point2D p : pointList) {
                    double x = p.getX();
                    double y = p.getY();
                    double px = (x - minX) / dw * w + 25;
                    double py = h - (y - minY) / dh * h + 25;
                    if (i++ == 0) gp.moveTo(px, py);
                    else gp.lineTo(px, py);
                }
//            gp.closePath();
                if (Math.abs(this.isolevel - isolevel) < 0.01) selectedList.add(gp);
                else if (isolevel == -1f) g2.draw(gp);
            }
        }
        g2.setColor(Color.RED);
        for (GeneralPath generalPath : selectedList) {
            System.err.println("selected " + generalPath);
            g2.draw(generalPath);
        }

        //g.drawLine(0, 0, 100, 100);
    }

    public void setSelected(Double isolevel) {
        this.isolevel = isolevel;
    }

    public Double getIsolevel() {
        return isolevel;
    }

    public void setMetadataCollection(IsolineMetadataCollection metadataCollection) {
        this.metadataCollection = metadataCollection;
    }
}
