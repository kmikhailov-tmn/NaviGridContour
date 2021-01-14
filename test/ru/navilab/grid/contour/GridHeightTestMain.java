package ru.navilab.grid.contour;

import ru.navilab.grid.contour.old.OldGridPlotPolygons;
import ru.navilab.grid.contour.old.SqlPolygonWriter;

import java.io.FileWriter;

public class GridHeightTestMain {

    public static void main(String[] args) throws Exception {
        String fileName = args[0];
        CPS3GridReader gridReader = new CPS3GridReader();
        GridAdapter gridData = gridReader.readGridData(fileName);
        FileWriter sqlWriter = new FileWriter("generated_p.sql");
        SqlPolygonWriter polygonConsumer = new SqlPolygonWriter(28412, "test_polygon",
                "isolevel", "the_geom", sqlWriter);
        OldGridPlotPolygons gridPlotPolygons = new OldGridPlotPolygons(gridData, polygonConsumer);
        gridPlotPolygons.plotPolygon(1f);
        sqlWriter.close();
    }
}
