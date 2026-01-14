package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphicConveyorTest {

    private static final float EPS = 1e-5f;


    @Test
    void testScaleMatrixDiagonal() {
        Matrix4f m = GraphicConveyor.scale(2, 3, 4);

        assertEquals(2, m.get(0, 0), EPS);
        assertEquals(3, m.get(1, 1), EPS);
        assertEquals(4, m.get(2, 2), EPS);
        assertEquals(1, m.get(3, 3), EPS);
    }

    @Test
    void testScaleVector() {
        Matrix4f m = GraphicConveyor.scale(2, 3, 4);
        Vector3f v = new Vector3f(1, 1, 1);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, v);

        assertEquals(2, r.getX(), EPS);
        assertEquals(3, r.getY(), EPS);
        assertEquals(4, r.getZ(), EPS);
    }

    @Test
    void testTranslateMatrix() {
        Matrix4f m = GraphicConveyor.translate(5, -2, 10);

        assertEquals(5, m.get(0, 3), EPS);
        assertEquals(-2, m.get(1, 3), EPS);
        assertEquals(10, m.get(2, 3), EPS);
    }

    @Test
    void testTranslateVector() {
        Matrix4f m = GraphicConveyor.translate(1, 2, 3);
        Vector3f v = new Vector3f(4, 5, 6);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, v);

        assertEquals(5, r.getX(), EPS);
        assertEquals(7, r.getY(), EPS);
        assertEquals(9, r.getZ(), EPS);
    }



    @Test
    void testRotateX90Degrees() {
        Matrix4f m = GraphicConveyor.rotateX((float) Math.PI / 2);
        Vector3f v = new Vector3f(0, 1, 0);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, v);

        assertEquals(0, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(1, r.getZ(), EPS);
    }


    @Test
    void testRotateY90Degrees() {
        Matrix4f m = GraphicConveyor.rotateY((float) Math.PI / 2);
        Vector3f v = new Vector3f(1, 0, 0);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, v);

        assertEquals(0, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(-1, r.getZ(), EPS);
    }


    @Test
    void testRotateZ90Degrees() {
        Matrix4f m = GraphicConveyor.rotateZ((float) Math.PI / 2);
        Vector3f v = new Vector3f(1, 0, 0);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, v);

        assertEquals(0, r.getX(), EPS);
        assertEquals(1, r.getY(), EPS);
        assertEquals(0, r.getZ(), EPS);
    }


    @Test
    void testLookAtMovesEyeToOrigin() {
        Vector3f eye = new Vector3f(1, 2, 3);
        Vector3f target = new Vector3f(1, 2, 4);

        Matrix4f view = GraphicConveyor.lookAt(eye, target);

        Vector3f transformedEye =
                GraphicConveyor.multiplyMatrix4ByVector3(view, eye);

        assertEquals(0, transformedEye.getX(), EPS);
        assertEquals(0, transformedEye.getY(), EPS);
        assertEquals(0, transformedEye.getZ(), EPS);
    }


    @Test
    void testPerspectiveMatrixBasicProperties() {
        Matrix4f p = GraphicConveyor.perspective(
                (float) Math.PI / 2,
                1.0f,
                1.0f,
                100.0f
        );

        assertNotEquals(0, p.get(0, 0), EPS);
        assertNotEquals(0, p.get(1, 1), EPS);
        assertEquals(1.0f, p.get(3, 2), EPS);
    }


    @Test
    void testMultiplyIdentity() {
        Matrix4f id = Matrix4f.identity();
        Vector3f v = new Vector3f(7, 8, 9);

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(id, v);

        assertEquals(7, r.getX(), EPS);
        assertEquals(8, r.getY(), EPS);
        assertEquals(9, r.getZ(), EPS);
    }


    @Test
    void testVertexToPointCenter() {
        Vector3f v = new Vector3f(0, 0, 0);
        Vector2f p = GraphicConveyor.vertexToPoint(v, 800, 600);

        assertEquals(399.5f, p.getX(), EPS);
        assertEquals(299.5f, p.getY(), EPS);
    }

    @Test
    void testVertexToPointTopRight() {
        Vector3f v = new Vector3f(1, 1, 0);
        Vector2f p = GraphicConveyor.vertexToPoint(v, 800, 600);

        assertEquals(799, p.getX(), EPS);
        assertEquals(0, p.getY(), EPS);
    }

    @Test
    void testCreateModelMatrixOnlyTranslation() {
        Matrix4f m = GraphicConveyor.createModelMatrix(
                new Vector3f(1, 2, 3),
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 1)
        );

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(0, 0, 0));

        assertEquals(1, r.getX(), EPS);
        assertEquals(2, r.getY(), EPS);
        assertEquals(3, r.getZ(), EPS);
    }

    @Test
    void testCreateModelMatrixScaleThenTranslate() {
        Matrix4f m = GraphicConveyor.createModelMatrix(
                new Vector3f(1, 0, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(2, 2, 2)
        );

        Vector3f r = GraphicConveyor.multiplyMatrix4ByVector3(m, new Vector3f(1, 0, 0));

        assertEquals(3, r.getX(), EPS);
        assertEquals(0, r.getY(), EPS);
        assertEquals(0, r.getZ(), EPS);
    }
}
