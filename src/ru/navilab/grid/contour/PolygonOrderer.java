package ru.navilab.grid.contour;

import java.util.*;

public class PolygonOrderer {
    private Map<HeightPolygon,Relation> slaveRelationMap = new Hashtable<>();
    private Map<HeightPolygon,Relation> masterRelationMap = new Hashtable<>();
    private Map<HeightPolygon,HeightPolygons> polygonsMap = new Hashtable<>();

    private static class Index implements Comparable {
        HeightPolygon polygon;
        int sortIndex;

        public Index(HeightPolygon polygon, int sortIndex) {
            this.polygon = polygon;
            this.sortIndex = sortIndex;
        }

        @Override
        public int compareTo(Object o) {
            if (o != null && o instanceof Index) {
                Index idx = (Index) o;
                return Integer.compare(this.sortIndex, idx.sortIndex);
            } else {
                return -1;
            }
        }
    }

    private static class Relation {
        HeightPolygon polygon;
        Relation outerRelation;
        List<Relation> childs = new ArrayList<>();

        public Relation(HeightPolygon polygon) {
            this.polygon = polygon;
        }

        public Relation(HeightPolygon polygon, Relation outerRelation) {
            this.polygon = polygon;
            this.outerRelation = outerRelation;
        }

        public boolean containSlave(HeightPolygon slavePolygon) {
            return polygon == slavePolygon;
        }

        public void setOuterRelation(Relation outerRelation) {
            this.outerRelation = outerRelation;
        }

        public Relation getOuterRelation() {
            return outerRelation;
        }

        public void addChild(Relation relation) {
            childs.add(relation);
        }

        public List<Relation> getChilds() {
            return childs;
        }
    }

    public static List<HeightPolygons> order(List<HeightPolygons> unorderedList) {
        return new PolygonOrderer().orderImpl(unorderedList);
    }

    private List<HeightPolygons> orderImpl(List<HeightPolygons> unorderedList) {
        List<HeightPolygon> slavePolygonList = new ArrayList<>();
        for (HeightPolygons polygons : unorderedList) {
            HeightPolygon masterPolygon = polygons.getFirst();
            polygonsMap.put(masterPolygon, polygons);
            for (HeightPolygon slavePolygon : slavePolygonList) {
                findRelations(polygons, masterPolygon, slavePolygon);
            }
            slavePolygonList.add(masterPolygon);
        }
        return sort(unorderedList);
    }

    private List<HeightPolygons> sort(List<HeightPolygons> unorderedList) {
        List<Index> indexList = new ArrayList<>();
        int sortLevel = 1;

        Set<HeightPolygon> orderedPolygonSet = new HashSet<>();
        Set<HeightPolygon> slavePolygons = slaveRelationMap.keySet();
        for (HeightPolygon slavePolygon : slavePolygons) {
            if (!orderedPolygonSet.contains(slavePolygon)) {
                Relation relation = slaveRelationMap.get(slavePolygon);
                Relation topOuterRelation = getTopOuterRelation(relation);
                sortLevel = addRelation2IndexList(indexList, sortLevel, topOuterRelation, orderedPolygonSet);
            }
        }
        addOther(indexList, sortLevel++, unorderedList);
        Collections.sort(indexList);

        List<HeightPolygons> resultList = new ArrayList<>();
        for (Index index : indexList) {
            HeightPolygon polygon = index.polygon;
            HeightPolygons heightPolygons = polygonsMap.get(polygon);
            resultList.add(heightPolygons);
        }
        return resultList;
    }

    private Relation getTopOuterRelation(Relation relation) {
        Relation result = relation;
        do {
            relation = relation.getOuterRelation();
            if (relation != null) result = relation;
        } while (relation != null);
        return result;
    }

    private void addOther(List<Index> indexList, int sortLevel, List<HeightPolygons> unorderedList) {
        Set<HeightPolygon> addedPolygons = new HashSet<>();
        for (Index index : indexList) addedPolygons.add(index.polygon);
        for (HeightPolygons heightPolygons : unorderedList) {
            HeightPolygon masterPolygon = heightPolygons.getFirst();
            if (!addedPolygons.contains(masterPolygon)) {
                indexList.add(new Index(masterPolygon, sortLevel));
            }
        }
    }

    private int addRelation2IndexList(List<Index> indexList, int sortLevel, Relation relation, Set<HeightPolygon> orderedPolygonSet) {
        indexList.add(new Index(relation.polygon, sortLevel));
        orderedPolygonSet.add(relation.polygon);
        List<Relation> childs = relation.getChilds();
        for (Relation childRelation : childs) {
            return addRelation2IndexList(indexList, ++sortLevel, childRelation, orderedPolygonSet);
        }
        return sortLevel++;
    }

    private void findRelations(HeightPolygons polygons, HeightPolygon masterPolygon, HeightPolygon slavePolygon) {
        if (masterPolygon.contain(slavePolygon)) {
            Relation relation = slaveRelationMap.get(slavePolygon);
            if (relation == null) {
                relation = createSlaveRelation(slavePolygon);
                Relation outerRelation = getCreateMasterRelation(masterPolygon);
                relation.setOuterRelation(outerRelation);
                outerRelation.addChild(relation);
            } else {
                Relation outerRelation;
                do {
                    outerRelation = relation.getOuterRelation();
                    if (outerRelation != null) relation = outerRelation;
                } while (outerRelation != null);
                if (masterPolygon != relation.polygon && masterPolygon.contain(relation.polygon)) {
                    Relation newOuterRelation = getCreateMasterRelation(masterPolygon);
                    relation.setOuterRelation(newOuterRelation);
                    newOuterRelation.addChild(relation);
                    if (!slaveRelationMap.containsKey(relation.polygon)) {
                        slaveRelationMap.put(relation.polygon, relation);
                    }
                }
            }
        }
    }

    private Relation createSlaveRelation(HeightPolygon polygon) {
        Relation relation = new Relation(polygon);
        slaveRelationMap.put(polygon, relation);
        return relation;
    }

    private Relation getCreateMasterRelation(HeightPolygon polygon) {
        Relation relation = masterRelationMap.get(polygon);
        if (relation == null) {
            relation = new Relation(polygon);
            masterRelationMap.put(polygon, relation);
        }
        return relation;
    }
}
