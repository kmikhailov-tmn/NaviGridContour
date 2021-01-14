package ru.navilab.grid.contour;

import java.io.FileWriter;

public class GridTestMain {
    public static void main(String[] args) throws Exception {
        String fileName = args[0];
        GridReader gridReader = new GridReader();
        GridAdapter gridData = gridReader.readGridData(fileName);
        FileWriter sqlWriter = new FileWriter("generated.sql");
        SqlIsolineConsumer sqlIsolineConsumer = new SqlIsolineConsumer(28412, "test_lines",
                "isolevel", "the_geom", sqlWriter);
        GridContourIsolines contourIsolines = new GridContourIsolines(gridData, sqlIsolineConsumer);
        double minZ = gridData.getMinZ();
        double maxZ = gridData.getMaxZ();
        System.err.println(minZ + " " + maxZ);
        int count = (int) ((maxZ - minZ) / 10);
        for (int i = 0; i < count; i++) {
            float value = (float) Math.ceil(minZ + i * (maxZ - minZ) / count);
            System.err.println(value);
            contourIsolines.drawIsoline(value);
        }
        sqlWriter.close();

//        XYZGrid marchingSquareSample = new XYZGrid(12650000, 6950000, 500, 5, 5,
//                new float[][]{
//                        {1, 1, 1, 1, 1},
//                        {1, 2, 3, 2, 1},
//                        {1, 3, 3, 3, 1},
//                        {1, 2, 3, 2, 1},
//                        {1, 1, 1, 1, 1}
//                }
//        );
        //new GridContourIsolines(marchingSquareSample).drawIsoline(1.5f);

//        XYZGrid mySample = new XYZGrid(12650000, 6950000, 500, 5, 5,
//                new float[][]{
//                        {1, 1, 3, 3, 1},
//                        {1, 1, 3, 3, 1},
//                        {1, 2, 4, 3, 0},
//                        {1, 1, 3, 3, 1},
//                        {1, 1, 2, 3, 1}
//                }
//        );
//        new GridContourIsolines(mySample).drawIsoline(2f);
    }
}
