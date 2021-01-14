package ru.navilab.grid.contour;

import ru.navilab.grid.contour.old.ListenerList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.*;

public class PolygonForm {
    private JList list1;
    private JButton onButton;
    private JButton offButton;
    private JPanel mainPanel;
    private JLabel xyLabel;
    private JButton buttonAction;
    private ListenerList<PolygonListener> listenerList = new ListenerList<>();

    public PolygonForm(List<Double> levelCollection) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        int i=0;
        for (Double level : levelCollection) {
            String str = String.format("%d %.2f", i++, level);
            listModel.addElement(str);
        }
        list1.setModel(listModel);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = list1.getSelectedIndex();
                Double level = levelCollection.get(index);
                System.err.println(level);
                listenerList.fireEvent(new ListenerList.FireHandler<PolygonListener>() {
                    @Override
                    public void fireEvent(PolygonListener listener) {
                        listener.polygonSelected(level);
                    }
                });
            }
        });
    }

    public void addButtonActionListener(ActionListener l) {
        buttonAction.addActionListener(l);
    }

    public JLabel getXyLabel() {
        return xyLabel;
    }

    public void addPolygonListener(PolygonListener listener) {
        listenerList.add(listener);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
