package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphicConveyorTest {

    private static final float EPS = 1e-6f;

    // =========================
    // SCALE
    // =========================

    @Test
    void scalePositive() {
        Matrix4f m = GraphicConveyor.scale(2, 3, 4);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 1, 1));

        assertEquals(2, r.getX(), EPS);
        assertEquals(3, r.getY(), EPS);
        assertEquals(4, r.getZ(), EPS);
    }

    @Test
    void scaleWithZero() {
        Matrix4f m = GraphicConveyor.scale(0, 2, 2);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(5, 5, 5));

        assertEquals(0, r.getX(), EPS);
        assertEquals(10, r.getY(), EPS);
        assertEquals(10, r.getZ(), EPS);
    }

    // =========================
    // TRANSLATE
    // =========================

    @Test
    void translatePositive() {
        Matrix4f m = GraphicConveyor.translate(1, 2, 3);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(0, 0, 0));

        assertEquals(1, r.getX(), EPS);
        assertEquals(2, r.getY(), EPS);
        assertEquals(3, r.getZ(), EPS);
    }

    @Test
    void translateNegative() {
        Matrix4f m = GraphicConveyor.translate(-1, -2, -3);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 1, 1));

        assertEquals(0, r.getX(), EPS);
        assertEquals(-1, r.getY(), EPS);
        assertEquals(-2, r.getZ(), EPS);
    }

    // =========================
    // ROTATE X
    // =========================

    @Test
    void rotateX90() {
        Matrix4f m = GraphicConveyor.rotateX((float) Math.PI / 2);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(0, 1, 0));

        assertEquals(0, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(-1, r.getZ(), EPS);
    }

    // =========================
    // ROTATE Y
    // =========================

    @Test
    void rotateY90() {
        Matrix4f m = GraphicConveyor.rotateY((float) Math.PI / 2);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(0, 0, 1));

        assertEquals(1, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(0, r.getZ(), EPS);
    }

    // =========================
    // ROTATE Z
    // =========================

    @Test
    void rotateZ180() {
        Matrix4f m = GraphicConveyor.rotateZ((float) Math.PI);
        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 0, 0));

        assertEquals(-1, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(0, r.getZ(), EPS);
    }

    // =========================
    // ORDER OF TRANSFORMS
    // =========================

    @Test
    void scaleThenTranslate() {
        Matrix4f s = GraphicConveyor.scale(2, 2, 2);
        Matrix4f t = GraphicConveyor.translate(1, 0, 0);

        Matrix4f m = t.multiply(s);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 0, 0));

        assertEquals(3, r.getX(), EPS); // (1*2)+1
    }

    // =========================
    // MODEL MATRIX
    // =========================

    @Test
    void modelMatrixFullTransform() {
        Matrix4f m = GraphicConveyor.createModelMatrix(
                new Vector3f(1, 2, 3),
                new Vector3f(0, (float) Math.PI / 2, 0),
                new Vector3f(2, 2, 2)
        );

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 0, 0));

        assertEquals(1, r.getX(), EPS);
        assertEquals(2, r.getY(), EPS);
        assertEquals(1, r.getZ(), EPS);
    }

    // =========================
    // LOOK AT
    // =========================

    @Test
    void lookAtCameraAtOrigin() {
        Matrix4f view = GraphicConveyor.lookAt(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0)
        );

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(view, new Vector3f(0, 0, 0));

        assertEquals(0, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(5, r.getZ(), EPS);
    }

    // =========================
    // PERSPECTIVE (BASIC CHECK)
    // =========================

    @Test
    void perspectiveMatrixStructure() {
        Matrix4f p = GraphicConveyor.perspective(
                (float) Math.PI / 2, 1.0f, 1.0f, 100.0f
        );

        assertEquals(1.0f, p.get(3, 2), EPS);
        assertEquals(0.0f, p.get(3, 3), EPS);
    }
}
