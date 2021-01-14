package ru.navilab.grid.contour;

import ru.navilab.grid.contour.old.OldGridPlotPolygons;
import ru.navilab.grid.contour.old.SqlPolygonWriter;

import java.io.FileWriter;

public class GridReaderTest
{
    public static void main(String[] args) throws Exception
    {
        String fileName = args[0];//"C:\\Users\\Oshibkov_DE\\Desktop\\gridTest\\structure US1 Roxar 2013.1.3.grd";
        CPS3GridReader gridReader = new CPS3GridReader();
        GridAdapter gridData = gridReader.readGridData(fileName);
        int a = 0;
    }
}
