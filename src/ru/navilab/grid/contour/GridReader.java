package ru.navilab.grid.contour;

public class GridReader {
    GridLoadAdapter[] gridLoaders = new GridLoadAdapter[] { new CPS3GridReader(), new DSAAGridReader(), new ZMapGridReader() };

    public GridAdapter readGridData(String filename) throws GridLoadException {
        for (GridLoadAdapter gridLoader : gridLoaders) {
            if (gridLoader.fileFormatMatch(filename)) {
                return gridLoader.readGridData(filename);
            }
        }
        throw new GridLoadException("no valid format for grid file :" + filename);
    }
}
