package com.cgvsu.removers;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class VertexRemover {

    public static void deleteVertices(Model model, Set<Integer> verticesToDelete, boolean removeInitiallyFreeVertices){
        Set<Integer> polygonsToDelete = new HashSet<>();
        for(int i=0;i<model.polygons.size();i++){
            for(int vi:model.polygons.get(i).getVertexIndices()){
                if(verticesToDelete.contains(vi)){
                    polygonsToDelete.add(i);
                    break;
                }
            }
        }
        PolygonRemover.deletePolygons(model,polygonsToDelete,true);

        if(removeInitiallyFreeVertices){
            Set<Integer> used = new HashSet<>();
            for(Polygon p:model.polygons) used.addAll(p.getVertexIndices());
            Map<Integer,Integer> vMap = rebuildList(model.vertices,used);
            for(Polygon p:model.polygons) remapIndices(p.getVertexIndices(),vMap);
        }
    }

    private static <T> Map<Integer,Integer> rebuildList(ArrayList<T> list, Set<Integer> toKeep){
        ArrayList<T> newList = new ArrayList<>();
        Map<Integer,Integer> map = new HashMap<>();
        int newIdx=0;
        for(int i=0;i<list.size();i++){
            if(toKeep.contains(i)){
                newList.add(list.get(i));
                map.put(i,newIdx++);
            }
        }
        list.clear();
        list.addAll(newList);
        return map;
    }

    private static void remapIndices(ArrayList<Integer> indices, Map<Integer,Integer> map){
        for(int i=0;i<indices.size();i++){
            if(map.containsKey(indices.get(i))) indices.set(i,map.get(indices.get(i)));
        }
    }
}
