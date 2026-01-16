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

        Set<Integer> allInitiallyUsedVertices = new HashSet<>();
        Set<Integer> allInitiallyUsedTextures = new HashSet<>();
        Set<Integer> allInitiallyUsedNormals = new HashSet<>();
        for (Polygon p : model.polygons) {
            allInitiallyUsedVertices.addAll(p.getVertexIndices());
            if (!p.getTextureVertexIndices().isEmpty())
                allInitiallyUsedTextures.addAll(p.getTextureVertexIndices());
            if (!p.getNormalIndices().isEmpty())
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

        Set<Integer> usedVertices = new HashSet<>();
        Set<Integer> usedTextures = new HashSet<>();
        Set<Integer> usedNormals = new HashSet<>();

        for (Polygon p : model.polygons) {
            usedVertices.addAll(p.getVertexIndices());
            if (!p.getTextureVertexIndices().isEmpty())
                usedTextures.addAll(p.getTextureVertexIndices());
            if (!p.getNormalIndices().isEmpty())
                usedNormals.addAll(p.getNormalIndices());
        }

        Set<Integer> verticesToKeep = new HashSet<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            if (usedVertices.contains(i) || !allInitiallyUsedVertices.contains(i)) {
                verticesToKeep.add(i);
            }
        }

        Set<Integer> texturesToKeep = new HashSet<>();
        for (int i = 0; i < model.textureVertices.size(); i++) {
            if (usedTextures.contains(i) || !allInitiallyUsedTextures.contains(i)) {
                texturesToKeep.add(i);
            }
        }

        Set<Integer> normalsToKeep = new HashSet<>();
        for (int i = 0; i < model.normals.size(); i++) {
            if (usedNormals.contains(i) || !allInitiallyUsedNormals.contains(i)) {
                normalsToKeep.add(i);
            }
        }

        Map<Integer, Integer> vMap = rebuildList(model.vertices, verticesToKeep);
        Map<Integer, Integer> tMap = rebuildList(model.textureVertices, texturesToKeep);
        Map<Integer, Integer> nMap = rebuildList(model.normals, normalsToKeep);

        for (Polygon p : model.polygons) {
            remap(p.getVertexIndices(), vMap);
            remap(p.getTextureVertexIndices(), tMap);
            remap(p.getNormalIndices(), nMap);
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

    private static void remap(ArrayList<Integer> indices, Map<Integer, Integer> map) {
        if (indices == null || indices.isEmpty()) return;
        for (int i = 0; i < indices.size(); i++) {
            Integer ni = map.get(indices.get(i));
            if (ni != null) {
                indices.set(i, ni);
            }
        }
    }
}
