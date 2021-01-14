package ru.navilab.grid.contour;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Mikhailov_KG
 * Date: 24.07.2020
 */
public class WktShowForm {
    private final DefaultListModel<Geometry> listModel;
    private final List<Color> colorList = new ArrayList<>();
    private JPanel mainPanel;
    private JSplitPane splitPane;
    private JButton colorButton;
    private JTextArea wktTextArea;
    private JButton addButton;
    private JButton removeButton;
    private JList list1;
    private JButton testButton;
    private JCheckBox labelCheckBox;
    private MapViewComponent mapViewComponent = new MapViewComponent();
    private Color lastColor = Color.BLUE;
    private DistantColorFinder colorFinder = new DistantColorFinder();

    WktShowForm() {
        getNextColor();
        splitPane.setRightComponent(mapViewComponent);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddButton();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveButton();
            }

        });

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onColorButton();
            }
        });

        listModel = new DefaultListModel<>();
        list1.setModel(listModel);
    }

    private Color getNextColor() {
        Color distantColor = colorFinder.findDistantColor(Color.BLUE, colorList);
        colorList.add(distantColor);
        lastColor = distantColor;
        return distantColor;
    }

    private void onColorButton() {
        Color color = JColorChooser.showDialog(mainPanel, "Choose", lastColor);
        if (color != null) lastColor = color;
    }

    private void onRemoveButton() {
        try {
            int selectedIndex = list1.getSelectedIndex();
            if (selectedIndex != -1) {
                Geometry geometry = listModel.get(selectedIndex);
                listModel.remove(selectedIndex);
                mapViewComponent.remove(geometry);
            }
        } catch (RuntimeException e) {
            showException(e);
        }
    }


    private void onAddButton() {
        try {
            final WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wktTextArea.getText());
            listModel.addElement(geom);
            mapViewComponent.setDrawLabelFlag(labelCheckBox.isSelected());
            mapViewComponent.add(geom, lastColor);
            wktTextArea.setText("");
            getNextColor();
        } catch (ParseException | RuntimeException e) {
            showException(e);
        }
    }

    private void showException(Exception e) {
        JOptionPane.showMessageDialog(mainPanel, e.getLocalizedMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
