package thut.core.client.render.x3d;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class Material
{
    public final String     name;
    public String           texture;
    public ResourceLocation texresource;
    public Vector3f         diffuseColor;
    public Vector3f         specularColor;
    public Vector3f         emissiveColor;
    public float            emissiveMagnitude;
    public float            ambientIntensity;
    public float            shininess;
    public float            transparency;

    boolean                 depth;

    boolean                 colour_mat;
    boolean                 light;
    float[]                 oldLight = { -1, -1 };

    public Material(String name)
    {
        this.name = name;
    }

    public Material(String name, String texture, Vector3f diffuse, Vector3f specular, Vector3f emissive, float ambient,
            float shiny, float transparent)
    {
        this.name = name;
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.ambientIntensity = ambient;
        this.shininess = shiny;
        this.transparency = transparent;
        this.emissiveMagnitude = Math.min(1, (float) (emissiveColor.length() / Math.sqrt(3)) / 0.8f);
    }

    private FloatBuffer makeBuffer(float value)
    {
        FloatBuffer ret = BufferUtils.createFloatBuffer(1 + 4);
        ret.put(new float[] { value });
        return ret;
    }

    private FloatBuffer makeBuffer(Vector3f vector)
    {
        FloatBuffer ret = BufferUtils.createFloatBuffer(3 + 4);
        ret.put(new float[] { vector.x, vector.y, vector.z });
        return ret;
    }

    public void postRender()
    {
        if (!depth) GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (!colour_mat) GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        if (!light) GL11.glDisable(GL11.GL_LIGHTING);
        else GL11.glEnable(GL11.GL_LIGHTING);
        if (emissiveMagnitude != 0 && oldLight[0] != -1 && oldLight[1] != -1)
        {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldLight[0], oldLight[1]);
        }
    }

    public void preRender()
    {
        depth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        colour_mat = GL11.glGetBoolean(GL11.GL_COLOR_MATERIAL);
        light = GL11.glGetBoolean(GL11.GL_LIGHTING);

        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, makeBuffer(ambientIntensity));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, makeBuffer(diffuseColor));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, makeBuffer(specularColor));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, makeBuffer(shininess));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, makeBuffer(emissiveColor));

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        if (emissiveMagnitude != 0)
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            oldLight[0] = OpenGlHelper.lastBrightnessX;
            oldLight[1] = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 * emissiveMagnitude,
                    OpenGlHelper.lastBrightnessY);
        }
        else
        {
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
}
