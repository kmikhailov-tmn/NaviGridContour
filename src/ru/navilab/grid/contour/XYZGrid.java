package ru.navilab.grid.contour;

public class XYZGrid implements GridAdapter {
    private double[][] zvalues;
    private double[] axisX;
    private double[] axisY;
    private double minZ;
    private double maxZ;
    private double missingZ;

    public XYZGrid(double xmin, double ymin, double step, int ncol, int nrow, double[][] zvalues, double missingZ) {
        this.missingZ = missingZ;
        double[] x = new double[ncol];
        for (int c = 0; c < ncol; c++) x[c] = xmin + c * step;
        double[] y = new double[nrow];
        for (int r = 0; r < nrow; r++) y[r] = ymin + r * step;
        init(x, y, zvalues);
    }

    public XYZGrid(double[] axisX, double[] axisY, double[][] zvalues, double missingZ) {
        this.missingZ = missingZ;
        init(axisX, axisY, zvalues);
    }

    private void init(double[] axisX, double[] axisY, double[][] zvalues) {
        this.zvalues = zvalues;
        int first = 0;
        for (int c = 0; c < axisX.length; c++) {
            for (int r = 0; r < axisY.length; r++) {
                double z = zvalues[c][r];
                if (z == missingZ) continue;
                if (first++ == 0) minZ = maxZ = z;
                else {
                    if (z < minZ) minZ = z;
                    if (z > maxZ) maxZ = z;
                }
            }
        }
        this.axisX = axisX;
        this.axisY = axisY;
    }

    public double[] getAxX() {
        return axisX;
    }

    public double[] getAxY() {
        return axisY;
    }

    public double[][] getValueZ() {
        return zvalues;
    }

    @Override
    public double getMinX() {
        return axisX[0];
    }

    @Override
    public double getMinY() {
        return axisY[0];
    }

    @Override
    public double getMaxX() {
        return axisX[axisX.length - 1];
    }

    @Override
    public double getMaxY() {
        return axisY[axisY.length - 1];
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    @Override
    public double getMissingZ() {
        return missingZ;
    }
}
