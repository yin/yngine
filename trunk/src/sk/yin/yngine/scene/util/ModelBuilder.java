package sk.yin.yngine.scene.util;

import sk.yin.yngine.math.Model;
import sk.yin.yngine.math.Triple;
import sk.yin.yngine.math.Point3f;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.TexCoord2f;

public class ModelBuilder {
    private List<Point3f> vertices = new ArrayList<Point3f>();
    // TODO(mgagyi): This needs to be moved to the generators needing it.
    public List<Integer> verticleCache = new ArrayList<Integer>();
    private List<Point3f> colors = new ArrayList<Point3f>();
    private List<Point3f> normals = new ArrayList<Point3f>();
    private List<TexCoord2f> texCoords = new ArrayList<TexCoord2f>();
    private List<Triple> faceTriangles = new ArrayList<Triple>();
    private List<Triple> faceColors = new ArrayList<Triple>();
    private List<Triple> faceNormals = new ArrayList<Triple>();
    private List<Triple> faceTexCoords = new ArrayList<Triple>();

    public ModelBuilder() {
    }

    public int addVertex(Point3f v) {
        int idx = vertices.indexOf(v);
        if (idx == -1) {
            vertices.add(v.copy());
            idx = vertices.size() - 1;
        }
        verticleCache.add(idx);
        return idx;
    }

    public int addColor(Point3f c) {
        int idx = colors.indexOf(c);
        if (idx == -1) {
            colors.add(c.copy());
            idx = colors.size() - 1;
        }
        return idx;
    }

    public int addNormal(Point3f n) {
        int idx = normals.indexOf(n);
        if (idx == -1) {
            normals.add(n.copy());
            idx = normals.size() - 1;
        }
        return idx;
    }

    public int addTexCoord(TexCoord2f texCoord) {
        int idx = texCoords.indexOf(texCoord);
        if (idx == -1) {
            texCoords.add(texCoord);
            idx = texCoords.size() - 1;
        }
        return idx;
    }

    public int addFace(Triple t) {
        return addFace(t, false);
    }

    public int addFace(Triple t, boolean mapFace) {
        if (mapFace) {
            t = mapFaceIndexes(t);
        }

        int idx = faceTriangles.indexOf(t);
        if (idx == -1) {
            idx = faceTriangles.size();
            faceTriangles.add(t);
        }
        return idx;
    }

    public void appendNormalIndexes(Triple normals) {
        faceNormals.add(normals);
    }

    public void appendColorIndexes(Triple colors) {
        faceColors.add(colors);
    }

    public void appendTexCoordIndexes(Triple texCoords) {
        faceTexCoords.add(texCoords);
    }

    public void addAndAppendColor(float r, float g, float b) {
        appendColorIndexes(new Triple(addColor(new Point3f(r, g, b))));
    }

    public void appendVerticesColor(int offset) {
        Triple f = faceTriangles.get(faceTriangles.size()-1),
            c = new Triple(
                f.idx1+offset,
                f.idx2+offset,
                f.idx3+offset
            );
        appendColorIndexes(c);
    }

    /**
     * Maps vertex indexes of a given face to model vertex indexes.
     * @param t
     * @return
     */
    protected Triple mapFaceIndexes(Triple t) {
        return new Triple(verticleCache.get(t.idx1), verticleCache.get(t.idx2), verticleCache.get(t.idx3));
    }

    public void clearVertexCache() {
        verticleCache.clear();
    }

    public void moveVerticesToRadius(float radius) {
        for (Point3f v : vertices) {
            v.absolute(radius);
        }
    }

    public Model toModel() {
        boolean hasNormals = normals.size() > 0;
        boolean hasColors = colors.size() > 0;
        boolean hasTexCoords = texCoords.size() > 0;
        int flen = 3
                + (hasNormals ? 3 : 0)
                + (hasColors ? 3 : 0)
                + (hasTexCoords ? 3 : 0);

        float[] vs = new float[vertices.size() * 3],
                ns = null,
                cs = null,
                tcs = null;
        int[] fs = new int[faceTriangles.size() * flen];

        if(hasNormals)
            ns = new float[normals.size() * 3];
        if(hasColors)
            cs = new float[colors.size() * 3];
        if(hasTexCoords)
            tcs = new float[texCoords.size() * 2];

        for (int i = 0; i < vertices.size(); i++) {
            Point3f v = vertices.get(i);
            vs[3 * i] = v.x;
            vs[3 * i + 1] = v.y;
            vs[3 * i + 2] = v.z;
        }
        for (int i = 0; i < colors.size(); i++) {
            Point3f c = colors.get(i);
            cs[3 * i] = c.x;
            cs[3 * i + 1] = c.y;
            cs[3 * i + 2] = c.z;
        }
        for (int i = 0; i < normals.size(); i++) {
            Point3f n = normals.get(i);
            ns[3 * i] = n.x;
            ns[3 * i + 1] = n.y;
            ns[3 * i + 2] = n.z;
        }
        for (int i = 0; i < texCoords.size(); i++) {
            TexCoord2f tc = texCoords.get(i);
            tcs[2 * i] = tc.x;
            tcs[2 * i + 1] = tc.y;
        }
        for (int i = 0; i < faceTriangles.size(); i++) {
            Triple t = faceTriangles.get(i);
            int fi = flen * i;
            fs[fi++] = t.idx1*3;
            fs[fi++] = t.idx2*3;
            fs[fi++] = t.idx3*3;

            if(hasNormals) {
                if(i < faceNormals.size()) {
                    t = faceNormals.get(i);
                    fs[fi++] = t.idx1*3;
                    fs[fi++] = t.idx2*3;
                    fs[fi++] = t.idx3*3;
                 } else {
                    fs[fi++] = fs[fi++] = fs[fi++] = -1;
                 }
            }
            if(hasColors) {
                if(i < faceColors.size()) {
                    t = faceColors.get(i);
                    fs[fi++] = t.idx1*3;
                    fs[fi++] = t.idx2*3;
                    fs[fi++] = t.idx3*3;
                 } else {
                    fs[fi++] = fs[fi++] = fs[fi++] = -1;
                 }
            }
            if(hasTexCoords) {
                if(i < faceTexCoords.size()) {
                    t = faceTexCoords.get(i);
                    fs[fi++] = t.idx1*2;
                    fs[fi++] = t.idx2*2;
                    fs[fi++] = t.idx3*2;
                 } else {
                    fs[fi++] = fs[fi++] = fs[fi++] = -1;
                 }
            }
        }

        return new Model(vs, ns, cs, tcs, fs);
    }
}
