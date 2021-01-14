package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import ru.navilab.grid.contour.old.SimplePolygonViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class GridPlotVisualTest implements PolygonConsumer {
    private GridAdapter gridData;
    private List<PolyLevel> list = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new GridPlotVisualTest().run(args[0]);
    }

    public void showPolylines(double step) {
        PolylineConsumer polylineConsumer = new PolylineConsumer();
        GridContourIsolines contourIsolines = new GridContourIsolines(gridData, polylineConsumer);
        double minZ = Math.max(gridData.getMinZ(), 0);
        double maxZ = gridData.getMaxZ();
        int count = (int) ((maxZ - minZ) / step);
        SortedMap<Double, List<List<Point2D>>> resultMap = new TreeMap<>();
        for (int i = 1; i <= count; i++) {
            double isolevel = (float) Math.ceil(minZ + i * (maxZ - minZ) / count);
            System.err.println("isolevel " + isolevel);
            contourIsolines.drawIsoline(isolevel);

            List<List<Point2D>> listOfPointList = polylineConsumer.getListOfPointList();
            resultMap.put(isolevel, new ArrayList<>(listOfPointList));
            polylineConsumer.clear();
        }

        SimplePolygonViewPanel viewPanel = new SimplePolygonViewPanel(resultMap);
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

        GridPlotPolygons gridPlotPolygons = PlotPolygonsFactory.createPlotPolygonsOld(gridData, this);
        gridPlotPolygons.plotPolygon(1d);

        PolygonViewPanel viewPanel = new PolygonViewPanel(Collections.emptyList());
        JFrame frame = new JFrame("polygons");

        PolygonForm polygonForm = new PolygonForm(createDoubleList());
        polygonForm.addButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Geometry> geomList = new ArrayList<>();
                for (PolyLevel polyLevel : list) {
                    geomList.addAll(polyLevel.getPolygonList());
                }
                viewPanel.setPolygonList(geomList);
                viewPanel.repaint();
            }
        });
        polygonForm.addPolygonListener(new PolygonListener() {
            @Override
            public void polygonSelected(double level) {
                PolyLevel polyLevel = findLevel(level);
                System.err.println("find " + polyLevel);
                if (polyLevel != null) {
                    viewPanel.setPolygonList(polyLevel.getPolygonList());
                    viewPanel.repaint();
                }
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

    private PolyLevel findLevel(double level) {
        for (PolyLevel polyLevel : list) {
            if (polyLevel.getEndLevel() == level) return polyLevel;
        }
        return null;
    }

    private List<Double> createDoubleList() {
        List<Double> r = new ArrayList<>();
        for (PolyLevel polyLevel : list) {
            double endLevel = polyLevel.getEndLevel();
            System.err.println(endLevel);
            r.add(endLevel);

        }
        return r;
    }

    @Override
    public void consumeLevel(double startLevel, double endLevel, List<? extends Geometry> polygonList) {
        list.add(new PolyLevel(startLevel, endLevel, polygonList));
    }
}
