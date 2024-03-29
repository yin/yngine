package sk.yin.yngine.scene;

import static sk.yin.yngine.render.lights.GLLightRepository.isGLLight;

import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.vecmath.Vector3f;

import sk.yin.yngine.render.lights.GLLightRepository;
import sk.yin.yngine.render.shaders.ShaderProgram;
import sk.yin.yngine.scene.attributes.ISceneAttribute;

/**
 * General light usable for all GL core lighting models. May be
 * extended for custom lighting models to use with custom shaders.
 *
 * @author Matej 'Yin' Gagyi (yinotaurus+yngine-src@gmail.com)
 */
public class GenericLightNode implements ILightNode {

    public Vector3f position = new Vector3f(),
            direction = new Vector3f();
    public float ambient[], diffuse[], specular[],
            cutoff = 180.0f, spotExp = 1.0f;
    private int glLight = -1;
    private LightType type = LightType.Point;
    private float attenuationConstant = 1.0f,
            attenuationLinear = 0.0f,
            attenuationQuadratic = 0.0f;

    public GenericLightNode() {
    }

    public GenericLightNode(LightType type) {
        this.type = type;
    }

    public void cutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    public void stopExp(float spotExp) {
        this.spotExp = spotExp;
    }

    public void attenuationConstant(float attenuationConstant) {
        this.attenuationConstant = attenuationConstant;
    }

    public void attenuationLinear(float attenuationLinear) {
        this.attenuationLinear = attenuationLinear;
    }

    public void attenuationQuadratic(float attenuationQuadratic) {
        this.attenuationQuadratic = attenuationQuadratic;
    }

    public enum LightType {

        Directional(0.0f),
        Point(1.0f),
        Spot(1.0f);
        public float w;

        LightType(float w) {
            this.w = w;
        }
    }

    /**
     * Configures the GL light with the light parameters.
     */
    public void turnOn(GL2 gl, int glLight) {
        if (isGLLight(glLight)) {
            gl.glEnable(glLight);

            if (ambient != null) {
                gl.glLightfv(glLight, GLLightingFunc.GL_AMBIENT, ambient, 0);
            } else {
                gl.glLightfv(glLight, GLLightingFunc.GL_AMBIENT, new float[]{0f, 0f, 0f, 0f}, 0);
            }
            if (diffuse != null) {
                gl.glLightfv(glLight, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
            } else {
                gl.glLightfv(glLight, GLLightingFunc.GL_DIFFUSE, new float[]{0f, 0f, 0f, 0f}, 0);
            }
            if (specular != null) {
                gl.glLightfv(glLight, GLLightingFunc.GL_SPECULAR, specular, 0);
            } else {
                gl.glLightfv(glLight, GLLightingFunc.GL_SPECULAR, new float[]{0f, 0f, 0f, 0f}, 0);
            }
            
            gl.glLightf(glLight, GLLightingFunc.GL_CONSTANT_ATTENUATION, attenuationConstant);
            gl.glLightf(glLight, GLLightingFunc.GL_LINEAR_ATTENUATION, attenuationLinear);
            gl.glLightf(glLight, GLLightingFunc.GL_QUADRATIC_ATTENUATION, attenuationQuadratic);

            switch (type) {
                case Directional:
                    gl.glLightfv(glLight, GLLightingFunc.GL_POSITION, new float[]{
                                direction.x, direction.y, direction.z, type.w}, 0);
                    break;
                case Point:
                    gl.glLightfv(glLight, GLLightingFunc.GL_POSITION, new float[]{
                                position.x, position.y, position.z, type.w}, 0);
                    break;
                case Spot:
                    gl.glLightfv(glLight, GLLightingFunc.GL_POSITION, new float[]{
                                position.x, position.y, position.z, type.w}, 0);
                    gl.glLightfv(glLight, GLLightingFunc.GL_SPOT_DIRECTION, new float[]{
                                direction.x, direction.y, direction.z}, 0);
                    gl.glLightf(glLight, GLLightingFunc.GL_SPOT_CUTOFF, cutoff);
                    gl.glLightf(glLight, GLLightingFunc.GL_SPOT_EXPONENT, spotExp);
                    break;
                default:
                    // TODO(yin): Maybe custom lights?
            }
        }
    }

    public void turnOff(GL2 gl, int glLight) {
        if (isGLLight(glLight)) {
            // TODO(yin): Free Allocated GL light[i]
            gl.glDisable(glLight);
        }
    }

    public void update(float deltaTime) {
    }

    /**
     * Call to this method allocates a light number to enable
     * in GL state and adjust all the GL parameters for the
     * light by calling <code>turnOn</code>.
     */
    // TODO(yin): This has to be moved to renderer.
    public void render(GL2 gl) {
        if (glLight == -1) {
            glLight = GLLightRepository.instance().allocate();
        }
        if (glLight != -1) {
            turnOn(gl, glLight);
        }
        renderLight(gl);
    }

    /**
     * This method is a temporary helper method for draving
     * the light for debuging purposes. Except it to decome
     * deprecated in the future.
     */
    public void renderLight(GL2 gl) {
        gl.glDisable(GLLightingFunc.GL_LIGHTING);
        ShaderProgram.unuseCurrent(gl);
        switch (type) {
            case Directional:
                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(0.0f, 0.0f, 0.0f);
                gl.glVertex3f(0.0f, 0.0f, 0.0f);
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(position.x, position.y, position.z);
                gl.glEnd();
                break;
            case Point:
                gl.glBegin(GL2.GL_POINTS);
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(position.x, position.y, position.z);
                gl.glEnd();
                break;
            case Spot:
                Vector3f p = position,
                        d = new Vector3f();
                d.set(direction);
                d.scale(4);

                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(p.x, p.y, p.z);
                gl.glColor3f(0.2f, 0.2f, 0.2f);
                gl.glVertex3f(p.x+d.x, p.y+d.y, p.z+d.z);

                /* Spot wireframe
                d.normalize();
                //d.
                d.cross(d, new Vector3f(0.0f, 1.0f, 0.0f));
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(p.x, p.y, p.z);
                gl.glColor3f(0.4f, 0.2f, 0.2f);
                gl.glVertex3f(p.x+d.x, p.y+d.y, p.z+d.z);
                */
                gl.glEnd();

        }
        gl.glEnable(GLLightingFunc.GL_LIGHTING);
    }

    public void onAdded(SceneGraph graph, ISceneAttribute parent) {
    }

    public void onRemoved(SceneGraph graph) {
    }

    public <X extends ISceneAttribute> X attribute(Class<X> type) {
        return null;
    }

    public <X extends ISceneAttribute> X attribute(Class<X> type, String query) {
        return null;
    }

    public boolean addChild(ISceneNode child) {
        return false;
    }

    public void direction(Vector3f direction) {
        this.direction.set(direction);
        this.direction.normalize();
    }

    public void position(Vector3f position) {
        this.position.set(position);
    }

    public float[] ambient() {
        return ambient;
    }

    public void ambient(float[] ambient) {
        this.ambient = ambient;
    }

    public float[] diffuse() {
        return diffuse;
    }

    public void diffuse(float[] diffuse) {
        this.diffuse = diffuse;
    }

    public float[] specular() {
        return specular;
    }

    public void specular(float[] specular) {
        this.specular = specular;
    }
}
