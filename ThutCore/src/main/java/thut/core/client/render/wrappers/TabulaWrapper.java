package thut.core.client.render.wrappers;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.model.tabula.TabulaModel;
import thut.core.client.render.tabula.model.tabula.TabulaModelParser;

public class TabulaWrapper extends ModelBase
{
    final IModelRenderer<?> renderer;
    final TabulaModelParser parser;
    final TabulaModel       model;
    public boolean          statusRender = false;
    public String           phase        = "";

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
        GlStateManager.pushMatrix();
        ModelJson modelj = parser.modelMap.get(model);
        modelj.changer = renderer.getAnimationChanger();
        modelj.texturer = renderer.getTexturer();
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
        GlStateManager.disableCull();
        TabulaModelParser pars = ((TabulaModelParser) parser);
        ModelJson modelj = pars.modelMap.get(model);
        if (!statusRender) modelj.texturer = renderer.getTexturer();
        else modelj.texturer = null;
        modelj.changer = renderer.getAnimationChanger();
        float partialTick = ageInTicks = entityIn.ticksExisted;
        if (modelj.changer != null)
        {
            phase = modelj.changer.modifyAnimation((EntityLiving) entityIn, partialTick, phase);
        }
        boolean inSet = false;
        if (modelj.animationMap.containsKey(phase) || (inSet = renderer.getAnimations().containsKey(phase)))
        {
            if (!inSet) modelj.startAnimation(phase);
            else modelj.startAnimation(renderer.getAnimations().get(phase));
        }
        else if (modelj.isAnimationInProgress())
        {
            modelj.stopAnimation();
        }
        renderer.scaleEntity(entityIn, model, ageInTicks - entityIn.ticksExisted);
    }

    /** Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method. */
    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_,
            float partialTickTime)
    {
        // set.parser.modelMap.get(set.model).setLivingAnimations(entitylivingbaseIn,
        // p_78086_2_, p_78086_3_,
        // partialTickTime);
    }
}
