package ru.navilab.grid.contour;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

public class ZMapGridReader implements GridLoadAdapter {
    private static final String FILE_NOT_CORRECT = "can't read ASCII ZMAP grid format";
    private int countX;
    private int countY;
    private double maxX;
    private double maxY;
    private double minX;
    private double minY;
    private double xinc;
    private double yinc;
    /**
     * количество значений (токенов) в одной строчке данных
     */
    private int nodesInLine;
    /**
     * количество символов на одно значение z
     */
    private int nodeLength;
    /**
     * null значение
     */
    private double missingZ = -9999;

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
        return "ASCII ZMAP grid format";
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
            XYZGrid xyzGrid = new XYZGrid(axisX, axisY, zvalues, missingZ);
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
        String s = in.readLine();
        while (s.startsWith("!")) {
            s = in.readLine();
        }
        if (!s.startsWith("@")) {
            throw new GridLoadException(FILE_NOT_CORRECT + " expected @ token");
        }
        readGridParameters(s, 1);
        for (int i = 2; i <= 5; i++) {
            readGridParameters(in.readLine(), i);
        }
    }

    private boolean checkSignature(final LineNumberReader in) throws IOException {
        String s = in.readLine();
        if (s.startsWith("!") || s.startsWith("@")) return true;
        return false;
    }

    private double[][] readZvalues(final LineNumberReader in) throws NumberFormatException, IOException {
        String line;
        int lineOffset;
        double[][] valuesZ = new double[countX][countY];
        for (int i = 0; i < countX; i++) {
            line = in.readLine();
            lineOffset = 0;
            for (int j = 0; j < countY; j++) {
                if (lineOffset >= nodesInLine) {
                    line = in.readLine();
                    lineOffset = 0;
                }
                String field = line.substring(lineOffset * nodeLength, ++lineOffset * nodeLength);
                valuesZ[i][j] = parseDouble(field.trim());
            }
        }
        return valuesZ;
    }

    private final static double parseDouble(String str) {
        return Double.parseDouble(str.replace(",", "."));
    }

    private void readGridParameters(final String line, final int lineNumber) throws GridLoadException {
        final StringTokenizer tokener = new StringTokenizer(line, ",");
        int countTokens = tokener.countTokens();
        if (lineNumber == 1) {
            if (countTokens != 3) throw new GridLoadException(FILE_NOT_CORRECT + "countTokens");
            tokener.nextToken();
            tokener.nextToken();
            nodesInLine = parseHeaderInt(tokener.nextToken());
        } else if (lineNumber == 2) {
            if (countTokens != 4 && countTokens != 5) throw new GridLoadException(FILE_NOT_CORRECT + "countTokens");
            nodeLength = parseHeaderInt(tokener.nextToken());
            missingZ = parseHeaderDouble(tokener.nextToken());
        } else if (lineNumber == 3) {
            if (countTokens != 6) throw new GridLoadException(FILE_NOT_CORRECT + "countTokens");
            countY = parseHeaderInt(tokener.nextToken());
            countX = parseHeaderInt(tokener.nextToken());
            minX = parseHeaderDouble(tokener.nextToken());
            maxX = parseHeaderDouble(tokener.nextToken());
            minY = parseHeaderDouble(tokener.nextToken());
            maxY = parseHeaderDouble(tokener.nextToken());
            xinc = (maxX - minX) / countX;
            yinc = (maxY - minY) / countY;
        } else if (lineNumber == 4) {
            if (countTokens != 3) throw new GridLoadException(FILE_NOT_CORRECT + "countTokens");
        } else if (lineNumber == 5) {
            if (!line.startsWith("@")) throw new GridLoadException(FILE_NOT_CORRECT + " expected @ token");
        }
    }

    private final static int parseHeaderInt(String string) {
        return Integer.parseInt(string.trim());
    }

    private final static double parseHeaderDouble(String string) {
        return parseDouble(string.trim());
    }
}