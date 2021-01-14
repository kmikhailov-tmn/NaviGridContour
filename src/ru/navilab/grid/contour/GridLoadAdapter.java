package ru.navilab.grid.contour;

interface GridLoadAdapter {
    GridLoadAdapter NULL = new GridLoadAdapter() {
        public boolean fileFormatMatch(String filename) throws GridLoadException {
            return false;
        }

        public String getFileFormatDescription() {
            return "null";
        }

        public GridAdapter readGridData(String filename) throws GridLoadException {
            return GridAdapter.NULL;
        }
    };

    public GridAdapter readGridData(String filename) throws GridLoadException;
    public boolean fileFormatMatch(String filename) throws GridLoadException;
    public String getFileFormatDescription();
}
