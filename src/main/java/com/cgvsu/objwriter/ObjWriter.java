package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ObjWriter {

    public static void saveModel(Model model, String filename) throws IOException {
        if (model == null) throw new IOException("Invalid model provided!");
        Model copy = flattenModel(model);
        try (FileWriter fileWriter = new FileWriter(filename)) {
            writeHeader(fileWriter, copy);
            writeVertices(fileWriter, copy.vertices);
            writeTextureCoordinates(fileWriter, copy.textureVertices);
            writeNormals(fileWriter, copy.normals);
            writePolygons(fileWriter, copy);
        }
    }

    private static Model flattenModel(Model model) {
        Model copy = new Model();
        copy.vertices.addAll(model.vertices);
        copy.textureVertices.addAll(model.textureVertices);
        copy.normals.addAll(model.normals);
        for (Polygon p : model.polygons) {
            Polygon newP = new Polygon();
            newP.getVertexIndices().addAll(p.getVertexIndices());
            newP.getTextureVertexIndices().addAll(p.getTextureVertexIndices());
            newP.getNormalIndices().addAll(p.getNormalIndices());
            copy.polygons.add(newP);
        }
        return copy;
    }

    private static void writeHeader(FileWriter writer, Model model) throws IOException {
        writer.write("# Created by ObjWriter\n");
        writer.write("# Vertices: " + model.vertices.size() + "\n");
        writer.write("# Texture coordinates: " + model.textureVertices.size() + "\n");
        writer.write("# Normals: " + model.normals.size() + "\n");
        writer.write("# Polygons: " + model.polygons.size() + "\n\n");
    }

    private static void writeVertices(FileWriter writer, ArrayList<Vector3f> vertices) throws IOException {
        for (Vector3f v : vertices) writer.write(String.format(Locale.US,"v %.6f %.6f %.6f\n",v.getX(),v.getY(),v.getZ()));
        if (!vertices.isEmpty()) writer.write("\n");
    }

    private static void writeTextureCoordinates(FileWriter writer, ArrayList<Vector2f> textures) throws IOException {
        for (Vector2f uv : textures) writer.write(String.format(Locale.US,"vt %.6f %.6f\n",uv.getX(),uv.getY()));
        if (!textures.isEmpty()) writer.write("\n");
    }

    private static void writeNormals(FileWriter writer, ArrayList<Vector3f> normals) throws IOException {
        for (Vector3f n : normals) writer.write(String.format(Locale.US,"vn %.6f %.6f %.6f\n",n.getX(),n.getY(),n.getZ()));
        if (!normals.isEmpty()) writer.write("\n");
    }

    private static void writePolygons(FileWriter writer, Model model) throws IOException {
        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vIndices = polygon.getVertexIndices();
            ArrayList<Integer> tIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> nIndices = polygon.getNormalIndices();
            writer.write(constructPolygonString(vIndices,tIndices,nIndices)+"\n");
        }
    }

    private static String constructPolygonString(ArrayList<Integer> vIndices, ArrayList<Integer> tIndices, ArrayList<Integer> nIndices){
        StringBuilder polygonBuilder = new StringBuilder("f");
        for(int i=0;i<vIndices.size();i++){
            polygonBuilder.append(" ").append(vIndices.get(i)+1);
            boolean hasTex = tIndices!=null && i<tIndices.size();
            boolean hasNorm = nIndices!=null && i<nIndices.size();
            if(hasTex && hasNorm) polygonBuilder.append("/").append(tIndices.get(i)+1).append("/").append(nIndices.get(i)+1);
            else if(hasTex) polygonBuilder.append("/").append(tIndices.get(i)+1);
            else if(hasNorm) polygonBuilder.append("//").append(nIndices.get(i)+1);
        }
        return polygonBuilder.toString();
    }
}
