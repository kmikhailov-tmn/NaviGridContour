package ru.navilab.grid.contour;

public interface GridAdapter {
    GridAdapter NULL = new GridAdapter() {
        public double[] getAxX() {
            return new double[0];
        }

        public double[] getAxY() {
            return new double[0];
        }

        public double[][] getValueZ() {
            return new double[0][0];
        }

        @Override
        public double getMinX() {
            return 0;
        }

        @Override
        public double getMinY() {
            return 0;
        }

        @Override
        public double getMaxX() {
            return 0;
        }

        @Override
        public double getMaxY() {
            return 0;
        }

        @Override
        public double getMinZ() {
            return 0;
        }

        @Override
        public double getMaxZ() {
            return 0;
        }

        @Override
        public double getMissingZ() {
            return -9999;
        }

    };

    double[] getAxX();

    double[] getAxY();

    double[][] getValueZ();

    double getMinX();

    double getMinY();

    double getMaxX();

    double getMaxY();

    double getMinZ();

    double getMaxZ();

    double getMissingZ();
}
