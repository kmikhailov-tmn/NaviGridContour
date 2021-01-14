package ru.navilab.grid.contour.old;

import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlPolygonWriter implements OldPolygonConsumer {
    private static final int OUTPUT_SRID = 4326;
    private int inputSrid;
    private final String tableName;
    private final String isovalueColumnName;
    private final String geometryColumnName;
    private PrintWriter out;
    private List<List<Point2D>> polygonList = new ArrayList<>();
    private String geometryName = "POLYGON";
    private double startLevel;
    private double endLevel;

    public SqlPolygonWriter(int inputSrid, String tableName, String isovalueColumnName,
                            String geometryColumnName, Writer out) {
        this.inputSrid = inputSrid;
        this.tableName = tableName;
        this.isovalueColumnName = isovalueColumnName;
        this.geometryColumnName = geometryColumnName;
        this.out = new PrintWriter(out);
    }
    @Override
    public void startPolygon(double startLevel, double endLevel) {
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        polygonList = new ArrayList<>();
    }

    @Override
    public void consumePolygon(List<Point2D> polygon) {
        polygonList.add(polygon);
    }

    @Override
    public void endPolygon() {
        String insertStatement = String.format(Locale.US,"insert into %s (%s,%s) values " +
                        "(%.0f, ST_Transform(ST_GeomFromEWKT('SRID=%d;%s('",
                tableName, isovalueColumnName, geometryColumnName,
                endLevel, inputSrid, geometryName);
        out.println(insertStatement);
        int k = 0;
        for (List<Point2D> point2DList : polygonList) {
            if (k++ > 0) out.println("','");
            out.println("'('");
            int i = 0;
            for (Point2D p : point2DList) {
                out.print("'");
                if (i++ > 0) out.print(",");
                out.println(String.format(Locale.US, "%.1f %.1f'", p.getX(), p.getY()));
            }
            out.println("')'");
        }
        out.println("')')," + OUTPUT_SRID + "));");
    }
}
