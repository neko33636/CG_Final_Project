package com.cgvsu.removers;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class VertexRemover {

    public static void deleteVertices(
            Model model,
            Set<Integer> verticesToDelete,
            boolean removeInitiallyFreeVertices
    ) {

        Set<Integer> initiallyUsedVertices = new HashSet<>();
        for (Polygon p : model.polygons) {
            initiallyUsedVertices.addAll(p.getVertexIndices());
        }

        Set<Integer> polygonsToDelete = new HashSet<>();
        for (int i = 0; i < model.polygons.size(); i++) {
            Polygon p = model.polygons.get(i);
            for (Integer vi : p.getVertexIndices()) {
                if (verticesToDelete.contains(vi)) {
                    polygonsToDelete.add(i);
                    break;
                }
            }
        }

        PolygonRemover.deletePolygons(model, polygonsToDelete, true);

        if (removeInitiallyFreeVertices) {

            Set<Integer> verticesToKeep = new HashSet<>();
            for (int i = 0; i < model.vertices.size(); i++) {
                if (!verticesToDelete.contains(i)
                        || initiallyUsedVertices.contains(i)) {
                    verticesToKeep.add(i);
                }
            }

            Map<Integer, Integer> vMap =
                    rebuildList(model.vertices, verticesToKeep);

            for (Polygon p : model.polygons) {
                remapVertices(p.getVertexIndices(), vMap);
            }
        }
    }

    private static <T> Map<Integer, Integer> rebuildList(
            ArrayList<T> list,
            Set<Integer> toKeep
    ) {
        ArrayList<T> newList = new ArrayList<>();
        Map<Integer, Integer> map = new HashMap<>();
        int newIndex = 0;

        for (int i = 0; i < list.size(); i++) {
            if (toKeep.contains(i)) {
                newList.add(list.get(i));
                map.put(i, newIndex++);
            }
        }

        list.clear();
        list.addAll(newList);
        return map;
    }

    private static void remapVertices(
            ArrayList<Integer> indices,
            Map<Integer, Integer> map
    ) {
        for (int i = 0; i < indices.size(); i++) {
            Integer newIndex = map.get(indices.get(i));
            if (newIndex != null) {
                indices.set(i, newIndex);
            }
        }
    }
}
