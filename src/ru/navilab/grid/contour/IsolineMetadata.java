package ru.navilab.grid.contour;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by: Mikhailov_KG
 * Date: 28.12.2020
 */
public interface IsolineMetadata {
    IsolineMetadata NULL = new IsolineMetadata() {
        @Override
        public Collection<String> getPropertyNameCollection() {
            return Collections.emptyList();
        }

        @Override
        public String getPropertyValue(String property) {
            return null;
        }
    };

    Collection<String> getPropertyNameCollection();
    String getPropertyValue(String property);
}
