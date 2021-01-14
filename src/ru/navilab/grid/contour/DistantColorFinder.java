package ru.navilab.grid.contour;

import java.awt.*;
import java.util.Random;

/**
 * Created by Mikhailov_KG on 11.09.2018.
 */
public class DistantColorFinder {
    public static final int COLOR_MAX = 255;
    public static final int MAX_DISTANCE_LEVEL = 40000;
    private Random random = new Random();

    public Color findDistantColor(Color startColor, java.util.List<Color> colorList) {
        Color newColor = startColor;
        double maxDistance = 0;
        Color selectedColor = negateColor(startColor);
        for (int i=0; i < 10; i++) {
            newColor = createColor(newColor);
            double minDistance = Double.MAX_VALUE;
            for (Color color : colorList) {
                double d = distance(newColor, color);
                if (d < minDistance) {
                    minDistance = d;
                }
            }
            if (minDistance > maxDistance) {
                maxDistance = minDistance;
                selectedColor = newColor;
            }
            if (maxDistance > MAX_DISTANCE_LEVEL) break;
        };
        return selectedColor;
    }

    public static Color negateColor(Color color) {
        int red = 255 - color.getRed();
        int green = 255 - color.getGreen();
        int blue = 255 - color.getBlue();
        return new Color(red, green, blue);
    }

    private double distance(Color c1, Color c2) {
        int red = c1.getRed() - c2.getRed();
        int green = c1.getGreen() - c2.getGreen();
        int blue = c1.getBlue() - c2.getBlue();
        return red * red + green * green + blue * blue;
    }

    private Color createColor(Color color) {
        int red = norm(color.getRed() + random.nextInt(COLOR_MAX));
        int green = norm(color.getGreen() + random.nextInt(COLOR_MAX));
        int blue = norm(color.getBlue() + random.nextInt(COLOR_MAX));
        return new Color(red, green, blue);
    }

    private int norm(int i) {
        if (i > 255) return i - COLOR_MAX;
        else return i;
    }
}
