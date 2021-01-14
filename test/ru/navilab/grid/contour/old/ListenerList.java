package ru.navilab.grid.contour.old;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikhailov_KG
 *
 * Created: 16.02.2011
 *
 */
public class ListenerList<L> {
    private List<L> listenerList = new ArrayList<L>();
    private boolean enableFire = true;

    public interface FireHandler<L> {
        void fireEvent(L listener);
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        if (enableFire) {
            List<L> copy = new ArrayList<L>(listenerList);
            for (L l : copy) {
                fireHandler.fireEvent(l);
            }
        }
    }

    public void remove(L listener) {
        listenerList.remove(listener);
    }

    public List<L> getListenerList() {
        return listenerList;
    }

    public void enableFire(boolean enableFire) {
        this.enableFire = enableFire;
    }
}
