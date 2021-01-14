package ru.navilab.grid.contour.old;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import ru.navilab.grid.contour.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;



public class GridHeightTestVisualMain {
    private static class Relation<T> {
        T parent;
        List<T> childList;
    }
    private GridAdapter gridData;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public static void main(String[] args) throws Exception {
        new GridHeightTestVisualMain().run(args[0]);
    }

    public void showPolylines(double step) {
        DebugPolylineConsumer polylineConsumer = new DebugPolylineConsumer();
        GridContourIsolines contourIsolines = new GridContourIsolines(gridData, polylineConsumer);
        double minZ = Math.max(gridData.getMinZ(), 0);
        double maxZ = gridData.getMaxZ();
        int count = (int) ((maxZ - minZ) / step);
        if (count > 15) {
            count = 12;
            step = (maxZ - minZ) / count;
        }
        SortedMap<Double, List<List<Point2D>>> resultMap = new TreeMap<>();
        for (int i = 1; i <= count; i++) {
            double isolevel = (float) Math.ceil(minZ + i * (maxZ - minZ) / count);
            logger.fine("isolevel " + isolevel);
            contourIsolines.drawIsoline(isolevel);
            List<List<Point2D>> listOfPointList = polylineConsumer.getListOfPointList();
            resultMap.put(isolevel, new ArrayList<>(listOfPointList));
            polylineConsumer.clear();
        }

        SimplePolygonViewPanel viewPanel = new SimplePolygonViewPanel(resultMap);
        viewPanel.setMetadataCollection(polylineConsumer.getMetadataCollection());
        JFrame frame = new JFrame("lines");

        PolygonForm polygonForm = new PolygonForm(new ArrayList<>(resultMap.keySet()));
        polygonForm.addPolygonListener(new PolygonListener() {
            @Override
            public void polygonSelected(double isolevel) {
                viewPanel.setSelected(isolevel);
                viewPanel.repaint();
            }
        });
        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                double x = viewPanel.toMapX(e.getX());
                double y = viewPanel.toMapY(e.getY());
                Point2D.Double p = new Point2D.Double(x, y);
                polygonForm.getXyLabel().setText(" p=" + p);
            }
        });


        frame.getContentPane().add(polygonForm.getMainPanel(), BorderLayout.EAST);
        frame.getContentPane().add(viewPanel, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
        frame.setVisible(true);
    }

    public void run(String fileName) throws Exception {
        GridReader gridReader = new GridReader();
        gridData = gridReader.readGridData(fileName);

        showPolylines(1);

        TestPolygonConsumer polygonConsumer = new TestPolygonConsumer();
        OldGridPlotPolygons gridPlotPolygons = new OldGridPlotPolygons(gridData, polygonConsumer);
        gridPlotPolygons.plotPolygon(1f);

        Map<Double, List<List<Point2D>>> resultMap = polygonConsumer.getResultMap();
        SimplePolygonViewPanel viewPanel = new SimplePolygonViewPanel(resultMap);
        JFrame frame = new JFrame("polygons");

        PolygonForm polygonForm = new PolygonForm(new ArrayList<>(resultMap.keySet()));
        polygonForm.addButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPolygonAction(viewPanel, resultMap);
            }
        });
        polygonForm.addPolygonListener(new PolygonListener() {
            @Override
            public void polygonSelected(double level) {
                viewPanel.setSelected(level);
                viewPanel.repaint();
            }
        });
        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                double x = viewPanel.toMapX(e.getX());
                double y = viewPanel.toMapY(e.getY());
                Point2D.Double p = new Point2D.Double(x, y);
                polygonForm.getXyLabel().setText(" p=" + p);
            }
        });
        frame.getContentPane().add(polygonForm.getMainPanel(), BorderLayout.EAST);
        frame.getContentPane().add(viewPanel, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
        frame.setVisible(true);
    }

    private void runPolygonAction(SimplePolygonViewPanel viewPanel, Map<Double, List<List<Point2D>>> resultMap) {
        Double isolevel = viewPanel.getIsolevel();
        if (isolevel != null) {
            List<List<Point2D>> prevListOfPointList = resultMap.get(isolevel - 1);
            List<List<Point2D>> listOfPointList = resultMap.get(isolevel);
            List<Polygon> curPolygonList = PolygonTools.buildPolygonList(listOfPointList);
            show(curPolygonList, "cur " + isolevel);
            List<Polygon> prevPolygonList = PolygonTools.buildPolygonList(prevListOfPointList);
            show(prevPolygonList, "prev " + isolevel);
            List<? extends Geometry> viewPolygonList = PolygonTools.makeHeightPolygons(curPolygonList, prevPolygonList);
            show(viewPolygonList, "diff " + isolevel + " " + viewPolygonList.size());
        }
    }

    private void show(List<? extends Geometry> geometries, String title) {
        PolygonViewPanel polygonViewPanel = new PolygonViewPanel(geometries);
        JFrame polyListFrame = new JFrame(title);
        polyListFrame.getContentPane().add(polygonViewPanel, BorderLayout.CENTER);
        polyListFrame.setPreferredSize(new Dimension(600,400));
        polyListFrame.pack();
        polyListFrame.setVisible(true);
    }


}
