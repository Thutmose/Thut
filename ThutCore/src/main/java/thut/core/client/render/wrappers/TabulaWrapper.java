package thut.core.client.render.wrappers;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.model.tabula.TabulaModel;
import thut.core.client.render.tabula.model.tabula.TabulaModelParser;

public class TabulaWrapper extends ModelBase
{
    final IModelRenderer<?> renderer;
    final TabulaModelParser parser;
    final TabulaModel       model;
    private ModelJson       modelj;
    private boolean         init = false;

    public TabulaWrapper(TabulaModel model, TabulaModelParser parser, IModelRenderer<?> renderer)
    {
        this.model = model;
        this.parser = parser;
        this.renderer = renderer;
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        if (model == null || parser == null) { return; }
        checkInit();
        GlStateManager.pushMatrix();
        modelj.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    /** Sets the model's various rotation angles. For bipeds, par1 and par2 are
     * used for animating the movement of arms and legs, where par1 represents
     * the time(so that arms and legs swing back and forth) and par2 represents
     * how "far" arms and legs can swing at most. */
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn)
    {
        if (model == null || parser == null) { return; }
        checkInit();
        GlStateManager.disableCull();
        float partialTick = ageInTicks - entityIn.ticksExisted;
        IAnimationHolder animate = AnimationHelper.getHolder(entityIn);
        String phase = animate != null ? animate.getPendingAnimation() : "idle";
        if (phase == null) phase = "idle";
        if (modelj.changer != null)
        {
            phase = modelj.changer.modifyAnimation((MobEntity) entityIn, partialTick, phase);
        }
        boolean inSet = false;
        if (animate != null)
        {
            if (modelj.animationMap.containsKey(phase) || (inSet = renderer.getAnimations().containsKey(phase)))
            {
                if (!inSet) modelj.startAnimation(phase, animate);
                else modelj.startAnimation(renderer.getAnimations().get(phase), animate);
            }
            else if (modelj.isAnimationInProgress(animate))
            {
                modelj.stopAnimation(animate);
            }
        }
        renderer.scaleEntity(entityIn, model, ageInTicks - entityIn.ticksExisted);
    }

    /** Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method. */
    @Override
    public void setLivingAnimations(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float partialTickTime)
    {
        checkInit();
        if (!Minecraft.getInstance().isGamePaused())
        {
            IAnimationHolder animate = AnimationHelper.getHolder(entity);
            modelj.setToInitPose();
            if (animate != null && !animate.getPlaying().isEmpty())
            {
                for (Animation animation : animate.getPlaying())
                    modelj.updateAnimation(animate, animation, entity.ticksExisted, partialTickTime, limbSwing);
            }
        }
    }

    private void checkInit()
    {
        if (init) return;
        init = true;
        modelj = parser.modelMap.get(model);
        modelj.changer = renderer.getAnimationChanger();
        modelj.texturer = renderer.getTexturer();
    }
}
