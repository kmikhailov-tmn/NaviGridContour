package ru.navilab.grid.contour;

import java.awt.geom.Point2D;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlIsolineConsumer implements IsolineConsumer {
    private static final int OUTPUT_SRID = 4326;
    private int inputSrid;
    private final String tableName;
    private final String isovalueColumnName;
    private final String geometryColumnName;
    private PrintWriter out;
    private List<Point2D> pointList;
    private String geometryName = "LINESTRING";

    public SqlIsolineConsumer(int inputSrid, String tableName, String isovalueColumnName, String geometryColumnName,
                              Writer out) {
        this.inputSrid = inputSrid;
        this.tableName = tableName;
        this.isovalueColumnName = isovalueColumnName;
        this.geometryColumnName = geometryColumnName;
        this.out = new PrintWriter(out);
    }

    @Override
    public void startPolyline(double isovalue) {
        pointList = new ArrayList<>();
    }

    @Override
    public void consumePoint(double x, double y) {
        pointList.add(new Point2D.Double(x, y));
    }

    @Override
    public void endPolyline(double isovalue) {
        String insertStatement = String.format(Locale.US,"insert into %s (%s,%s) values " +
                        "(%.0f, ST_Transform(ST_GeomFromEWKT('SRID=%d;%s('",
                tableName, isovalueColumnName, geometryColumnName,
                isovalue, inputSrid, geometryName);
        out.println(insertStatement);
        int i = 0;
        for (Point2D p : pointList) {
            out.print("'");
            if (i++ > 0) out.print(",");
            out.println(String.format(Locale.US, "%.1f %.1f'", p.getX(), p.getY()));
        }
        out.println("')')," + OUTPUT_SRID + "));");
    }
}
