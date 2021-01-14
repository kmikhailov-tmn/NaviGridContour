package ru.navilab.grid.contour;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

public class DSAAGridReader implements GridLoadAdapter {
    private static final String FILE_NOT_CORRECT = "can't read DSAA grid format ";
    private double[] axisX;
    private double[] axisY;
    private int countX;
    private int countY;
    private double maxX;
    private double maxY;
    private double maxZ;
    private double minX;
    private double minY;
    private double minZ;
    private double xinc;
    private double yinc;

    public boolean fileFormatMatch(String filename) throws GridLoadException {
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(filename));
            boolean signatureIsOk = checkSignature(lineNumberReader);
            lineNumberReader.close();
            return signatureIsOk;
        } catch (IOException e) {
            throw new GridLoadException(e.getMessage());
        }
    }

    public String getFileFormatDescription() {
        return "Surfer ASCII grid format";
    }

    public GridAdapter readGridData(String filename) throws GridLoadException {
        final String line = null;
        try {
            final LineNumberReader in = new LineNumberReader(new FileReader(filename));
            loadLabel(in);
            double[] axisX = makeAxisX();
            double[] axisY = makeAxisY();
            double[][] zvalues = readZvalues(in);
            in.close();
            XYZGrid xyzGrid = new XYZGrid(axisX, axisY, zvalues, -9999);
            xyzGrid.setMinZ(minZ);
            xyzGrid.setMaxZ(maxZ);
            return xyzGrid;
        } catch (final IOException e) {
            throw new GridLoadException(e.getLocalizedMessage());
        } catch (final NumberFormatException e) {
            throw new GridLoadException(FILE_NOT_CORRECT + "NumberFormatException > " + line);
        }
    }

    double[] makeAxisX() {
        double[] result = new double[countX];
        for (int i = 0; i < result.length; i++) result[i] = minX + xinc * i;
        return result;
    }

    double[] makeAxisY() {
        double[] result = new double[countY];
        for (int i = 0; i < result.length; i++) result[i] = minY + yinc * i;
        return result;
    }

    private void loadLabel(final LineNumberReader in) throws IOException, GridLoadException {
        boolean signatureIsOk = checkSignature(in);
        if (!signatureIsOk) throw new GridLoadException(FILE_NOT_CORRECT);
        for (int i = 2; i < 6; i++) {
            readGridParameters(in.readLine(), in.getLineNumber());
        }
    }

    private boolean checkSignature(final LineNumberReader in) throws IOException {
        return in.readLine().equals("DSAA");
    }

    private double[][] readZvalues(final LineNumberReader in) throws NumberFormatException, IOException {
        String line;
        int i = -1, j = 0;
        double[][] valuesZ = new double[countX][countY];
        while ((line = in.readLine()) != null) {
            final StringTokenizer tokener = new StringTokenizer(line, " ");
            while (tokener.hasMoreTokens()) {
                if (i++ == (countX - 1)) {
                    i = 0;
                    j++;
                }
                final String nextToken = tokener.nextToken();
                valuesZ[i][j] = parseDouble(nextToken);
            }
        }
        return valuesZ;
    }

    private final static double parseDouble(String str) {
        return Double.parseDouble(str.replace(",", "."));
    }

    private void readGridParameters(final String line, final int lineNumber) throws GridLoadException {
        final StringTokenizer tokener = new StringTokenizer(line, " ");
        if (tokener.countTokens() != 2) throw new GridLoadException(FILE_NOT_CORRECT + "countTokens");
        final double firstValue = parseDouble(tokener.nextToken());
        final double secondValue = parseDouble(tokener.nextToken());
        if (lineNumber == 2) {
            countX = (int) firstValue;
            countY = (int) secondValue;
        } else if (lineNumber == 3) {
            minX = firstValue;
            maxX = secondValue;
        } else if (lineNumber == 4) {
            minY = firstValue;
            maxY = secondValue;
        } else if (lineNumber == 5) {
            minZ = firstValue;
            maxZ = secondValue;
        }
        xinc = (maxX - minX) / countX;
        yinc = (maxY - minY) / countY;
    }
}
