package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;

public class GraphicConveyor {

    public static Matrix4f scale(float scaleX, float scaleY, float scaleZ) {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 0, scaleX);
        result.set(1, 1, scaleY);
        result.set(2, 2, scaleZ);
        return result;
    }

    public static Matrix4f scale(float scale) {
        return scale(scale, scale, scale);
    }


    public static Matrix4f translate(float tx, float ty, float tz) {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 3, tx);
        result.set(1, 3, ty);
        result.set(2, 3, tz);
        return result;
    }

    public static Matrix4f translate(Vector3f translation) {
        return translate(translation.getX(), translation.getY(), translation.getZ());
    }


    public static Matrix4f rotateX(float angle) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.set(1, 1, cos);
        result.set(1, 2, -sin);
        result.set(2, 1, sin);
        result.set(2, 2, cos);

        return result;
    }

    public static Matrix4f rotateY(float angle) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.set(0, 0, cos);
        result.set(0, 2, sin);
        result.set(2, 0, -sin);
        result.set(2, 2, cos);

        return result;
    }

    public static Matrix4f rotateZ(float angle) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.set(0, 0, cos);
        result.set(0, 1, -sin);
        result.set(1, 0, sin);
        result.set(1, 1, cos);

        return result;
    }

    public static Matrix4f rotate(float angle, Vector3f axis) {
        axis = axis.normalized();
        float x = axis.getX();
        float y = axis.getY();
        float z = axis.getZ();

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float oneMinusCos = 1 - cos;

        Matrix4f result = Matrix4f.identity();

        result.set(0, 0, cos + x*x*oneMinusCos);
        result.set(0, 1, x*y*oneMinusCos - z*sin);
        result.set(0, 2, x*z*oneMinusCos + y*sin);

        result.set(1, 0, y*x*oneMinusCos + z*sin);
        result.set(1, 1, cos + y*y*oneMinusCos);
        result.set(1, 2, y*z*oneMinusCos - x*sin);

        result.set(2, 0, z*x*oneMinusCos - y*sin);
        result.set(2, 1, z*y*oneMinusCos + x*sin);
        result.set(2, 2, cos + z*z*oneMinusCos);

        return result;
    }



    public static Matrix4f rotateScaleTranslate() {
        return Matrix4f.identity();
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f za = target.subtract(eye).normalized();
        Vector3f xa = up.cross(za).normalized();
        Vector3f ya = za.cross(xa).normalized();

        Matrix4f translationMatrix = Matrix4f.identity();
        translationMatrix.set(0, 3, -eye.getX());
        translationMatrix.set(1, 3, -eye.getY());
        translationMatrix.set(2, 3, -eye.getZ());


        Matrix4f rotationMatrix = Matrix4f.identity();

        rotationMatrix.set(0, 0, xa.getX());
        rotationMatrix.set(1, 0, xa.getY());
        rotationMatrix.set(2, 0, xa.getZ());

        rotationMatrix.set(0, 1, ya.getX());
        rotationMatrix.set(1, 1, ya.getY());
        rotationMatrix.set(2, 1, ya.getZ());

        rotationMatrix.set(0, 2, za.getX());
        rotationMatrix.set(1, 2, za.getY());
        rotationMatrix.set(2, 2, za.getZ());

        // Видовая матрица: V = P * T
        return rotationMatrix.multiply(translationMatrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        Matrix4f result = Matrix4f.zero();

        float tanHalfFov = (float) Math.tan(fov * 0.5F);
        float range = nearPlane - farPlane;


        float f = 1.0f / tanHalfFov;

        result.set(0, 0, f / aspectRatio);
        result.set(1, 1, f);
        result.set(2, 2, (farPlane + nearPlane) / range);
        result.set(2, 3, (2 * farPlane * nearPlane) / range);
        result.set(3, 2, -1.0f);

        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {

        float x = vertex.getX();
        float y = vertex.getY();
        float z = vertex.getZ();

        float resultX = matrix.get(0, 0) * x + matrix.get(0, 1) * y +
                matrix.get(0, 2) * z + matrix.get(0, 3);
        float resultY = matrix.get(1, 0) * x + matrix.get(1, 1) * y +
                matrix.get(1, 2) * z + matrix.get(1, 3);
        float resultZ = matrix.get(2, 0) * x + matrix.get(2, 1) * y +
                matrix.get(2, 2) * z + matrix.get(2, 3);
        float resultW = matrix.get(3, 0) * x + matrix.get(3, 1) * y +
                matrix.get(3, 2) * z + matrix.get(3, 3);

        if (Math.abs(resultW) > 1e-10) {
            return new Vector3f(resultX / resultW, resultY / resultW, resultZ / resultW);
        } else {
            return new Vector3f(resultX, resultY, resultZ);
        }
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        float screenX = vertex.getX() * width + width / 2.0F;
        float screenY = vertex.getY() * height + height / 2.0F;
        return new Vector2f(screenX, screenY);
    }
}