package sk.yin.yngine.scene.util;

import javax.vecmath.TexCoord2f;
import sk.yin.yngine.math.Model;
import sk.yin.yngine.math.Triple;
import sk.yin.yngine.math.Point3f;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import sk.yin.yngine.scene.util.ModelBuilder.Decorator;

/**
 * Creates mesh model of a sphere interpolated from a base mesh
 * (e.g. Octahedron).
 *
 * @author Matej 'Yin' Gagyi (yinotaurus+yngine-src@gmail.com)
 */
public class SphereModelGenerator {
    private static SphereModelGenerator instance;
    private static final float octahedronVertices[] = {
        0.0f, -1.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
        -1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 1.0f, 0.0f
    };
    private static final int octahedronFaces[] = {
        0, 1, 2,
        0, 2, 3,
        0, 3, 4,
        0, 4, 1,
        1, 5, 2,
        2, 5, 3,
        3, 5, 4,
        4, 5, 1
    };
    private static final float tetrahedronVertices[] = {
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, 1.0f
    };
    private static final int tetrahedronFaces[] = {
        0, 1, 2,
        1, 0, 3,
        2, 0, 3,
        3, 2, 1
    };
    private static final float cubeVertices[] = {
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f
    };
    private static final int cubeFaces[] = {
        0, 1, 2, 1, 3, 2, // Front
        1, 5, 3, 5, 7, 3, // Right
        3, 7, 2, 7, 2, 6, // Bottom
        7, 6, 5, 6, 4, 5, // Back
        4, 0, 6, 0, 2, 6, // Left
        4, 0, 5, 0, 1, 5 // Top
    };

    protected static class SphereNormalDecorator implements Decorator {
        private ModelBuilder builder;
        private Map<Integer, Integer> vertexNormalMap =
                new HashMap<Integer, Integer>();

        public void setModelBuilder(ModelBuilder builder) {
            this.builder = builder;
        }

        public void onNewVertex(int idx, Point3f vertex) {
            if (builder != null) {
                int i = builder.addNormal(vertex.copy().normalize());
                vertexNormalMap.put(idx, i);
            }
        }

        public void onKnownVertex(int idx, Point3f vertex) {
        }

        public void onNewNormal(int idx, Point3f normal) {
        }

        public void onKnownNormal(int idx, Point3f normal) {
        }

        public void onNewTexCoord(int idx, TexCoord2f texCoord) {
        }

        public void onKnownTexCoord(int idx, TexCoord2f texCoord) {
        }

        public void onNewFace(int idx, Triple face) {
            if (builder != null) {
                Triple normals = new Triple(vertexNormalMap.get(face.idx1),
                        vertexNormalMap.get(face.idx2),
                        vertexNormalMap.get(face.idx3));
                builder.appendNormalIndexes(normals);
            }
        }

        public void onKnownFace(int idx, Triple face) {
        }

        public void onNormalTriple(Triple normals) {
        }
    }

    public enum BasePolyhedron {
        TETRAHEDRON, OCTAHEDRON, CUBE
    };

    private SphereModelGenerator() {
    }

    public static SphereModelGenerator instance() {
        if (instance == null) {
            instance = new SphereModelGenerator();
        }
        return instance;
    }

    public Model createSphere(float r, int iter, ModelBuilder builder) {
        return createSphere(r, iter, builder, BasePolyhedron.OCTAHEDRON);
    }

    public Model createSphere(float r, int iter, ModelBuilder builder,
            BasePolyhedron base) {
        float[] verts;
        int[] faces;

        switch (base) {
            case TETRAHEDRON:
                verts = tetrahedronVertices;
                faces = tetrahedronFaces;
                break;
            case CUBE:
                verts = cubeVertices;
                faces = cubeFaces;
                break;
            case OCTAHEDRON:
            default:
                verts = octahedronVertices;
                faces = octahedronFaces;
                break;
        }

        builder.addDecorator(new SphereNormalDecorator());
        List<Triple> triangles = createFaceTriangles(iter);
        for (int i = 0; i < faces.length; i += 3) {
            interpolateTriangle(builder, verts, faces[i],
                    faces[i + 1], faces[i + 2], iter);
            for (Triple t : triangles) {
                builder.addFace(t, true);
                builder.appendVerticesColor(0);
            }
        }
        builder.moveVerticesToRadius(r);

        return builder.toModel();
    }

    // TODO(yin): Does documentation sound familiar to anyone?
    protected void interpolateTriangle(ModelBuilder mb, float[] vtx, int vi1,
            int vi2, int vi3, int iter) {
        Point3f v1 =
                new Point3f(vtx[3 * vi1], vtx[3 * vi1 + 1], vtx[3 * vi1 + 2]);
        Point3f v2 =
                new Point3f(vtx[3 * vi2], vtx[3 * vi2 + 1], vtx[3 * vi2 + 2]);
        Point3f v3 =
                new Point3f(vtx[3 * vi3], vtx[3 * vi3 + 1], vtx[3 * vi3 + 2]);
        Point3f vecV = v2.copy().substract(v1).divide((float) (iter + 1));
        Point3f vecH = v3.copy().substract(v2).divide((float) (iter + 1));
        Point3f pBase = v1.copy();
        Point3f pVertex;
        mb.clearVertexCache();

        for (int stepV = 0; stepV < iter + 2; stepV++) {
            if (stepV > 0) {
                pBase.add(vecV);
            }

            pVertex = pBase.copy();
            for (int stepH = 0; stepH < stepV + 1; stepH++) {
                if (stepH > 0) {
                    pVertex.add(vecH);
                }
                mb.addVertex(pVertex);
                // Add normal

                mb.addNormal(pVertex.copy().normalize());
                // Add color
                _generateSomeVertexColor(mb, pVertex);
            }
        }
    }

    protected List<Triple> createFaceTriangles(int iter) {
        // Number of iterated vertices: (iter+1) * (iter+2) / 2;
        int i1 = 0, i2, i3;
        Triple t;
        List<Triple> triangles = new ArrayList<Triple>();
        for (int stepV = 0; stepV < iter + 1; stepV++) {
            for (int stepH = 0; stepH < stepV + 1; stepH++, i1++) {
                i2 = i1 + stepV + 1;
                i3 = i1 + stepV + 2;
                if (stepH > 0) {
                    triangles.add(new Triple(i1 - 1, i2, i1));
                }
                triangles.add(new Triple(i1, i2, i3));
            }
        }
        return triangles;
    }

    public int _generateSomeVertexColor(ModelBuilder mb, Point3f v) {
        Point3f c = new Point3f();
        // TODO(mgagyi): Implement these switches as Strategy pattern
        /*
        float rr = (float) (Math.random() / 3 - (1.0 / 6));
        float rg = (float) (Math.random() / 3 - (1.0 / 6));
        float rb = (float) (Math.random() / 3 - (1.0 / 6));
        c.x = (float) Math.random();
        c.y = (float) Math.random();
        c.z = (float) Math.random();
        /*/
        float rr = (float) Math.sin(Math.PI * v.x * 2) / 4;
        rr = 0;
        c.x = (float) Math.sin(v.x * 3);
        c.y = (float) Math.sin(v.y * 3);
        c.z = (float) Math.sin(v.z * 3);
        //*/

        int idx = mb.addColor(c);

        return idx;
    }
}
