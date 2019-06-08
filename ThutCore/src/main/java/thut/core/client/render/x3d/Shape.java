package thut.core.client.render.x3d;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.model.TextureCoordinate;
import thut.core.client.render.model.Vertex;

public class Shape
{
    private int                meshId  = 0;
    private boolean            hasTextures;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    private Material           material;
    public String              name;
    private double[]           uvShift = { 0, 0 };

    public Shape(Integer[] order, Vertex[] vert, Vertex[] norm, TextureCoordinate[] tex)
    {
        this.order = order;
        this.vertices = vert;
        this.normals = norm;
        this.textureCoordinates = tex;
        hasTextures = tex != null;
    }

    void addTris(IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate;
        Vector3f[] normalList = new Vector3f[order.length];
        boolean flat = true;
        if (texturer != null) flat = texturer.isFlat(name);
        if (flat)
        {
            // Calculate the normals for each triangle.
            for (int i = 0; i < order.length; i += 3)
            {
                Vector3f v1, v2, v3;
                vertex = vertices[order[i]];
                v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
                vertex = vertices[order[i + 1]];
                v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
                vertex = vertices[order[i + 2]];
                v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
                Vector3f a = new Vector3f(v2);
                a.sub(v1);
                Vector3f b = new Vector3f(v3);
                b.sub(v1);
                Vector3f c = new Vector3f();
                c.cross(a, b);
                c.normalize();
                normalList[i] = c;
                normalList[i + 1] = c;
                normalList[i + 2] = c;
            }
            GL11.glShadeModel(GL11.GL_FLAT);
        }
        else
        {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        if (!hasTextures)
        {
            GlStateManager.disableTexture2D();
        }

        GL11.glBegin(GL11.GL_TRIANGLES);
        int n = 0;
        for (Integer i : order)
        {
            if (hasTextures)
            {
                textureCoordinate = textureCoordinates[i];
                GL11.glTexCoord2d(textureCoordinate.u, textureCoordinate.v);
            }
            vertex = vertices[i];
            if (flat)
            {
                Vector3f norm = normalList[n];
                GL11.glNormal3f(norm.x, norm.y, norm.z);
            }
            else
            {
                normal = normals[i];
                GL11.glNormal3f(normal.x, normal.y, normal.z);
            }
            n++;
            GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
        }
        GL11.glEnd();

        if (!flat) GL11.glShadeModel(GL11.GL_FLAT);

        if (!hasTextures)
        {
            GlStateManager.enableTexture2D();
        }
    }

    private void compileList(IPartTexturer texturer)
    {
        if (!GL11.glIsList(meshId))
        {
            if (material != null && texturer != null && !texturer.hasMapping(material.name) && material.texture != null)
                texturer.addMapping(material.name, material.texture);
            meshId = GL11.glGenLists(1);
            GL11.glNewList(meshId, GL11.GL_COMPILE);
            addTris(texturer);
            GL11.glEndList();
        }
    }

    public void renderShape(IPartTexturer texturer)
    {
        // Compiles the list if the meshId is invalid.
        compileList(texturer);
        boolean textureShift = false;
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.applyTexture(name);
            if (textureShift = texturer.shiftUVs(name, uvShift))
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glTranslated(uvShift[0], uvShift[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        if (material != null)
        {
            material.preRender();
        }
        // Call the list
        GL11.glCallList(meshId);
        GL11.glFlush();
        if (material != null)
        {
            material.postRender();
        }

        // Reset Texture Matrix if changed.
        if (textureShift)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    public void setMaterial(Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
