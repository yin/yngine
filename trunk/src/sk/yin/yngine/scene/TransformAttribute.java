package sk.yin.yngine.scene;

import com.bulletphysics.linearmath.Transform;
import javax.media.opengl.GL;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import sk.yin.yngine.scene.ISceneAttribute.RenderStage;

/**
 * Static scene transformation. Can be changed anytime from outside, but won't
 * change by itself on any event except in the setter method.
 *
 * @author Matej 'Yin' Gagyi (yinotaurus+yngine-src@gmail.com)
 */
public class TransformAttribute implements ITransformAttribute {
    Transform transform;

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render(GL gl, RenderStage stage) {
        switch(stage) {
            case PRERENDER:
                transform(gl);
                break;
            case POSTRENDER:
                transformEnd(gl);
                break;
        }
    }

    public void transform(GL gl) {
        if (transform != null) {
            Vector3f origin = transform.origin;
            Matrix3f basis = transform.basis;
            gl.glPushMatrix();

            if (origin != null) {
                gl.glTranslatef(origin.x, origin.y, origin.z);
            }
            if (basis != null) {
                gl.glMultMatrixf(new float[]{
                            basis.m00, basis.m01, basis.m02, 0f,
                            basis.m10, basis.m11, basis.m12, 0f,
                            basis.m20, basis.m21, basis.m22, 0f,
                            0f, 0f, 0f, 1f
                        }, 0);
            }
        }
    }

    public void transformEnd(GL gl) {
        gl.glPopMatrix();
    }

    @Override
    public Vector3f origin() {
        return transform.origin;
    }

    @Override
    public void origin(Vector3f origin) {
        transform.origin.set(origin);
    }

    @Override
    public Matrix3f basis() {
        return transform.basis;
    }

    @Override
    public void basis(Matrix3f basis) {
        transform.basis.set(basis);
    }

}