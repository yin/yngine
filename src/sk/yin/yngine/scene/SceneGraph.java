package sk.yin.yngine.scene;

import javax.media.opengl.GL2;

import sk.yin.yngine.scene.attributes.ISceneAttribute;
import sk.yin.yngine.scene.camera.LookAtCamera;

/**
 * Class representing the main anchor, or root of the scene graph.
 *
 * @author Matej 'Yin' Gagyi (yinotaurus+yngine-src@gmail.com)
 */
public class SceneGraph {
    private ISceneNode root = new GenericSceneNode(new ISceneAttribute[] {});
    private LookAtCamera camera;

    public void frame(GL2 gl, float td) {
        root.update(td);

        if(camera != null)
            camera.transform(gl);

        root.render(gl);

        if(camera != null)
            camera.transformEnd(gl);
    }

    public void addChild(ISceneNode node) {
        root.addChild(node);
    }

    public LookAtCamera getCamera() {
        return camera;
    }

    public void setCamera(LookAtCamera camera) {
        this.camera = camera;
    }
}
