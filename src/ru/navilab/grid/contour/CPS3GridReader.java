package ru.navilab.grid.contour;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class CPS3GridReader implements GridLoadAdapter {
    public static final String MSMODL = "->MSMODL";
    public static final String DIGIT_EXCEPTION = "can't parse the digit ";
    public static final String WRONG_FORMAT = "wrong file format ";
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private double xinc;
    private double yinc;
    private int ncol;
    private int nrow;
    private double valueMin;
    private double valueMax;
    private double missingZ;


    double[] makeAxisX() {
        double[] result = new double[ncol];
        for (int i = 0; i < result.length; i++) result[i] = xmin + xinc * i;
        return result;
    }

    double[] makeAxisY() {
        double[] result = new double[nrow];
        for (int i = 0; i < result.length; i++) result[i] = ymin + yinc * i;
        return result;
    }

    public boolean fileFormatMatch(String filename) throws GridLoadException {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(filename));
            checkHeaderSignature(reader);
            reader.close();
            return true;
        } catch (GridLoadException e) {
            return false;
        } catch (IOException e) {
            throw new GridLoadException(e.getMessage());
        }
    }

    public String getFileFormatDescription() {
        return ".GRD CPS3 format";
    }

    public GridAdapter readGridData(String filename) throws GridLoadException {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(filename));
            readHeader(reader);
            double[] axisX = makeAxisX();
            double[] axisY = makeAxisY();
            double[][] zvalues = readZvalues(reader);
            reader.close();
            XYZGrid xyzGrid = new XYZGrid(axisX, axisY, zvalues, missingZ);
            xyzGrid.setMinZ(valueMin);
            xyzGrid.setMaxZ(valueMax);
            return xyzGrid;
        } catch (IOException e) {
            throw new GridLoadException("IO error: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new GridLoadException("Runtime exception: " + e.getMessage());
        }
    }

    private double[][] readZvalues(LineNumberReader reader) throws IOException, GridLoadException {
        try {
            double[][] zvalues = new double[ncol][nrow];
            int col = 0;
            int row = 0;

            String startRow = reader.readLine();
            if (startRow.contains("->"))
                startRow = reader.readLine();

            for (String line = startRow; line != null; line = reader.readLine()) {
                try {
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        String stringValue = st.nextToken();
                        //if (!missingZ.equals(stringValue)) {
                        zvalues[col][row] = parseDouble(stringValue);
                        //}
                        row++;
                        if (row >= nrow) {
                            row = 0;
                            col++;
                        }
                    }
                } catch (NoSuchElementException e) {
                    throw new GridLoadException(WRONG_FORMAT + line);
                }
            }
            return zvalues;
        } catch (OutOfMemoryError e) {
            throw new GridLoadException("out of memory while reading the grid " + e.getMessage());
        }
    }

    private void readHeader(LineNumberReader reader) throws IOException, GridLoadException {
        int status = 0,i = 0;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            try {
                if (i++ > 100) throw new GridLoadException("error reading header, read count > 100");
                if (line.startsWith("!")) {
                    continue;
                } else if (line.startsWith("FSASCI")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken(); // FSASCII
                    st.nextToken(); // skip 0
                    st.nextToken(); // skip 1
                    st.nextToken(); // skip COMPUTED or "+1E31" or "COMPUTED" or "Lines" or "77" and so on
                    String missingZ1 = st.nextToken();
                    String missingZ2 = st.nextToken();
                    missingZ = parseDouble("0".equals(missingZ1) ? missingZ2 : missingZ1);
                    status |= 0b1000;
                } else if (line.startsWith("FSLIMI")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    xmin = parseDouble(st.nextToken());
                    xmax = parseDouble(st.nextToken());
                    ymin = parseDouble(st.nextToken());
                    ymax = parseDouble(st.nextToken());
                    valueMin = parseDouble(st.nextToken());
                    valueMax = parseDouble(st.nextToken());
                    status |= 0b1;
                } else if (line.startsWith("FSNROW")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    nrow = parseInt(st.nextToken());
                    ncol = parseInt(st.nextToken());
                    status |= 0b10;
                } else if (line.startsWith("FSXINC")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    xinc = parseDouble(st.nextToken());
                    yinc = parseDouble(st.nextToken());
                    status |= 0b100;
                } else if (line.startsWith("FSATTR")) {
                    continue;
                    //StringTokenizer st = new StringTokenizer(line);
                    //st.nextToken();
                    //attr1 = parseDouble(st.nextToken());
                    //attr2 = parseDouble(st.nextToken());
                    //status |= 0b10000;
                }
                if (status == 0b1111)
                {
                    // Теперь шапка заканчивает читаться не в тот момент, когда нашли
                    // символ "->", а когда найдены все вышестоящие атрибуты
                    break;
                }
                //else if (line.startsWith("->")) break;
            } catch (NoSuchElementException e) {
                throw new GridLoadException(WRONG_FORMAT + line);
            }
        }
        if (status != 0b1111) throw new GridLoadException("error reading header - not all attributes has read: " + status);
    }

    private final static int parseInt(String s) throws GridLoadException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new GridLoadException(DIGIT_EXCEPTION + s);
        } catch (NullPointerException e) {
            throw new GridLoadException(DIGIT_EXCEPTION + s);
        }
    }

    private final static double parseDouble(String s) throws GridLoadException {
        try {
            return Double.parseDouble(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new GridLoadException(DIGIT_EXCEPTION + s);
        } catch (NullPointerException e) {
            throw new GridLoadException(DIGIT_EXCEPTION + s);
        }
    }

    private void checkHeaderSignature(LineNumberReader reader) throws IOException, GridLoadException {
        String line = reader.readLine();
        if (!line.startsWith("FSASCI")) wrongFormat(line);
        line = reader.readLine();
        if (!line.startsWith("FSATTR")) wrongFormat(line);
    }

    private void wrongFormat(String line) throws GridLoadException {
        throw new GridLoadException(WRONG_FORMAT + line);
    }
}
