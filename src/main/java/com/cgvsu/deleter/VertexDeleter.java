package com.cgvsu.deleter;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class VertexDeleter {

    public static void deleteVertices(Model model, Set<Integer> verticesToDelete) {
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

        PolygonDeleter.deletePolygons(model, polygonsToDelete, true);
    }
}
