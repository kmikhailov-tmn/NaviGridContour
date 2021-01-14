package ru.navilab.grid.contour;

import javax.swing.*;
import java.awt.*;

/**
 * Created by: Mikhailov_KG
 * Date: 24.07.2020
 */
public class WktShow {
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                WktShowForm wktShowForm = new WktShowForm();
                JPanel mainPanel = wktShowForm.getMainPanel();
                JFrame frame = new JFrame("Wkt");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
                frame.setPreferredSize(new Dimension(800,600));
                frame.pack();
                frame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
}
