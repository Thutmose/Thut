package thut.core.client.render.wrappers;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.model.IRetexturableModel;
import thut.core.client.render.tabula.components.Animation;

public class ModelWrapper extends ModelBase implements IModel
{
    public final ModelHolder       model;
    public final IModelRenderer<?> renderer;
    public IModel                  imodel;
    protected float                rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    protected float                rotateAngleX   = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;

    public ModelWrapper(ModelHolder model, IModelRenderer<?> renderer)
    {
        this.model = model;
        this.renderer = renderer;
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        HeadInfo info = imodel.getHeadInfo();
        if (info != null)
        {
            info.currentTick = entityIn.ticksExisted;
        }
        IAnimationChanger animChanger = renderer.getAnimationChanger();
        Set<String> excluded = Sets.newHashSet();
        if (animChanger != null) for (String partName : imodel.getParts().keySet())
        {
            if (animChanger.isPartHidden(partName, entityIn, false))
            {
                excluded.add(partName);
            }
        }
        for (String partName : imodel.getParts().keySet())
        {
            IExtendedModelPart part = imodel.getParts().get(partName);
            if (part == null) continue;
            int[] rgbab = part.getRGBAB();
            if (animChanger != null)
            {
                int default_ = new Color(rgbab[0], rgbab[1], rgbab[2], rgbab[3]).getRGB();
                int rgb = animChanger.getColourForPart(partName, entityIn, default_);
                if (rgb != default_)
                {
                    Color col = new Color(rgb);
                    rgbab[0] = col.getRed();
                    rgbab[1] = col.getGreen();
                    rgbab[2] = col.getBlue();
                }
            }
            part.setRGBAB(rgbab);
            try
            {
                if (renderer.getTexturer() != null)
                {
                    renderer.getTexturer().bindObject(entityIn);
                }
                if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(renderer.getTexturer());
                if (part.getParent() == null)
                {
                    GlStateManager.pushMatrix();
                    part.renderAllExcept(renderer, excluded.toArray(new String[excluded.size()]));
                    GlStateManager.popMatrix();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        if (info != null)
        {
            info.lastTick = entityIn.ticksExisted;
        }
    }

    /** Sets the model's various rotation angles. For bipeds, par1 and par2 are
     * used for animating the movement of arms and legs, where par1 represents
     * the time(so that arms and legs swing back and forth) and par2 represents
     * how "far" arms and legs can swing at most. */
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn)
    {
        HeadInfo info = imodel.getHeadInfo();
        if (info != null)
        {
            info.headPitch = headPitch;
            info.headYaw = netHeadYaw;
        }
        transformGlobal(renderer.getAnimation(entityIn), entityIn, Minecraft.getMinecraft().getRenderPartialTicks(),
                netHeadYaw, headPitch);
    }

    /** Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method. */
    @Override
    public void setLivingAnimations(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount,
            float partialTickTime)
    {
        if (renderer.getAnimationChanger() != null) renderer.setAnimation(renderer.getAnimationChanger()
                .modifyAnimation((EntityLiving) entityIn, partialTickTime, renderer.getAnimation(entityIn)), entityIn);
        applyAnimation(entityIn, AnimationHelper.getHolder(entityIn), renderer, partialTickTime, limbSwing);
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return imodel.getParts();
    }

    @Override
    public void preProcessAnimations(Collection<List<Animation>> collection)
    {
        imodel.preProcessAnimations(collection);
    }

    private final Vector5 rots = new Vector5();

    protected void transformGlobal(String currentPhase, Entity entity, float partialTick, float rotationYaw,
            float rotationPitch)
    {
        Vector5 rotations = renderer.getRotations();
        if (rotations == null)
        {
            rotations = rots;
        }
        this.setRotationAngles(rotations.rotations);
        this.setOffset(renderer.getRotationOffset());
        float dy = rotationPointY - 1.5f;
        this.rotate();
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.translate(0, 0, dy);
        this.translate();
        renderer.scaleEntity(entity, this, partialTick);
    }

    protected void rotate()
    {
        GlStateManager.rotate(rotateAngle, rotateAngleX, rotateAngleY, rotateAngleZ);
    }

    private void translate()
    {
        GlStateManager.translate(rotationPointX, rotationPointY, rotationPointZ);
    }

    public void setRotationAngles(Vector4 rotations)
    {
        rotateAngle = rotations.w;
        rotateAngleX = rotations.x;
        rotateAngleY = rotations.y;
        rotateAngleZ = rotations.z;
    }

    public void setRotationPoint(float par1, float par2, float par3)
    {
        this.rotationPointX = par1;
        this.rotationPointY = par2;
        this.rotationPointZ = par3;
    }

    @Override
    public void setOffset(Vector3 point)
    {
        setRotationPoint((float) point.x, (float) point.y, (float) point.z);
    }

    @Override
    public Set<String> getHeadParts()
    {
        return imodel.getHeadParts();
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return imodel.getHeadInfo();
    }

    @Override
    public void applyAnimation(Entity entity, IAnimationHolder animate, IModelRenderer<?> renderer, float partialTicks,
            float limbSwing)
    {
        imodel.applyAnimation(entity, animate, renderer, partialTicks, limbSwing);
    }

}
