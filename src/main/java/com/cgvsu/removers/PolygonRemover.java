package com.cgvsu.removers;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class PolygonRemover {

    public static void deletePolygons(
            Model model,
            Set<Integer> polygonIndicesToDelete,
            boolean deleteFreeVertices
    ) {

        Set<Integer> allInitiallyUsedVertexes = new HashSet<>();
        for (Polygon p : model.polygons) {
            allInitiallyUsedVertexes.addAll(p.getVertexIndices());
        }

        Set<Integer> allInitiallyUsedTextureVertexes = new HashSet<>();
        for (Polygon p : model.polygons) {
            allInitiallyUsedTextureVertexes.addAll(p.getTextureVertexIndices());
        }

        Set<Integer> allInitiallyUsedNormals = new HashSet<>();
        for (Polygon p : model.polygons) {
            allInitiallyUsedNormals.addAll(p.getNormalIndices());
        }

        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (int i = 0; i < model.polygons.size(); i++) {
            if (!polygonIndicesToDelete.contains(i)) {
                newPolygons.add(model.polygons.get(i));
            }
        }
        model.polygons = newPolygons;

        if (!deleteFreeVertices) return;

        Set<Integer> currentlyUsedVertices = new HashSet<>();
        for (Polygon p : model.polygons) {
            currentlyUsedVertices.addAll(p.getVertexIndices());
        }

        Set<Integer> currentlyUsedVerticesT = new HashSet<>();
        for (Polygon p : model.polygons) {
            currentlyUsedVerticesT.addAll(p.getTextureVertexIndices());
        }

        Set<Integer> currentlyUsedNormal = new HashSet<>();
        for (Polygon p : model.polygons) {
            currentlyUsedNormal.addAll(p.getNormalIndices());
        }

        Set<Integer> verticesToKeep = new HashSet<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            if (currentlyUsedVertices.contains(i) || !allInitiallyUsedVertexes.contains(i)) {
                verticesToKeep.add(i);
            }
        }

        Set<Integer> verticesTToKeep = new HashSet<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            if (currentlyUsedVerticesT.contains(i) || !allInitiallyUsedTextureVertexes.contains(i)) {
                verticesTToKeep.add(i);
            }
        }

        Set<Integer> normalToKeep = new HashSet<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            if (currentlyUsedNormal.contains(i) || !allInitiallyUsedNormals.contains(i)) {
                normalToKeep.add(i);
            }
        }

        Map<Integer, Integer> vMap = rebuildList(model.vertices, verticesToKeep);
        Map<Integer, Integer> vtMap = rebuildList(model.textureVertices, verticesTToKeep);
        Map<Integer, Integer> vnMap = rebuildList(model.normals, normalToKeep);

        for (Polygon p : model.polygons) {
            remapVertices(p.getVertexIndices(), vMap);
            remapVertices(p.getTextureVertexIndices(), vtMap);
            remapVertices(p.getNormalIndices(), vnMap);
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

    private static void remapVertices(ArrayList<Integer> indices, Map<Integer, Integer> map) {
        if (indices.isEmpty()) return;
        for (int i = 0; i < indices.size(); i++) {
            Integer newIndex = map.get(indices.get(i));
            if (newIndex != null) {
                indices.set(i, newIndex);
            }
        }
    }
}
