/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.yin.jogl.scene;

import javax.media.opengl.GL;
import sk.yin.jogl.data.Model;

/**
 *
 * @author yin
 */
public class SceneObject implements ISceneNode, ISceneTransformation {
    private Model model;
    private float px, py, pz, rx, ry, rz, r;

    public SceneObject(Model model) {
        this.model = model;
    }

    public void render(GL gl) {
        transform(gl);
        model.render(gl);
        transformEnd(gl);
    }

    public void update(float deltaTime) {
        return;
    }

    public void transform(GL gl) {
        gl.glPushMatrix();
        //if(px!=0 || py!=0 || pz!=0)
            gl.glTranslatef(px, py, pz);
        //if(r!=0 && (rx!=0 || ry!=0 || rz!=0))
            gl.glRotatef(r, rx, ry, rz);
    }

    public void transformEnd(GL gl) {
        gl.glPopMatrix();
    }


    public float getPx() {
        return px;
    }

    public void setPx(float px) {
        this.px = px;
    }

    public float getPy() {
        return py;
    }

    public void setPy(float py) {
        this.py = py;
    }

    public float getPz() {
        return pz;
    }

    public void setPz(float pz) {
        this.pz = pz;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getRx() {
        return rx;
    }

    public void setRx(float rx) {
        this.rx = rx;
    }

    public float getRy() {
        return ry;
    }

    public void setRy(float ry) {
        this.ry = ry;
    }

    public float getRz() {
        return rz;
    }

    public void setRz(float rz) {
        this.rz = rz;
    }
}