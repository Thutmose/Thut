package thut.core.client.render.tabula.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.tabula.model.modelbase.TabulaModelBase;
import thut.core.client.render.tabula.model.modelbase.TabulaRenderer;
import thut.core.client.render.tabula.model.tabula.TabulaModel;

@SideOnly(Side.CLIENT)
public class ModelJson extends TabulaModelBase
{
    public TabulaModel                             tabulaModel;

    public Map<String, TabulaRenderer>             nameMap       = Maps.newHashMap();
    public Map<String, TabulaRenderer>             identifierMap = Maps.newHashMap();

    /** This is an ordered list of CubeGroup Identifiers. It is used to ensure
     * that translucent parts render in the correct order. */
    ArrayList<String>                              groupIdents   = Lists.newArrayList();
    /** Map of CubeGroup Identifiers to Sets of Root parts on the group. Uses
     * the above list to get keys */
    public Map<String, Collection<TabulaRenderer>> groupMap      = Maps.newHashMap();

    /** Map of names to animations, used to get animations for rendering more
     * easily */
    public HashMap<String, List<Animation>>        animationMap  = Maps.newHashMap();

    public Set<Animation>                          playing       = Sets.newHashSet();
    public String                                  playingAnimation;

    public IPartTexturer                           texturer;
    public IAnimationChanger                       changer;

    public ModelJson(TabulaModel model)
    {
        tabulaModel = model;

        textureWidth = model.getTextureWidth();
        textureHeight = model.getTextureHeight();

        ArrayList<Animation> animations = model.getAnimations();

        for (Animation animation : animations)
        {
            List<Animation> anims = animationMap.get(animation.name);
            if (anims == null) animationMap.put(animation.name, anims = Lists.newArrayList());
            anims.add(animation);
        }
        for (CubeInfo c : model.getCubes())
        {
            cube(c, null, "null");
        }

        for (CubeGroup g : model.getCubeGroups())
        {
            cubeGroup(g);
        }
        // The groups come in in the opposite order from what is needed here, so
        // reverse it
        Collections.reverse(groupIdents);

        setInitPose();
    }

    private TabulaRenderer createModelRenderer(CubeInfo cubeInfo)
    {
        TabulaRenderer cube = new TabulaRenderer(this, cubeInfo.txOffset[0], cubeInfo.txOffset[1]);
        cube.name = cubeInfo.name;
        cube.identifier = cubeInfo.identifier;
        cube.setRotationPoint((float) cubeInfo.position[0], (float) cubeInfo.position[1], (float) cubeInfo.position[2]);
        cube.rotateAngleX = (float) Math.toRadians((float) cubeInfo.rotation[0]);
        cube.rotateAngleY = (float) Math.toRadians((float) cubeInfo.rotation[1]);
        cube.rotateAngleZ = (float) Math.toRadians((float) cubeInfo.rotation[2]);

        if (Math.abs(cube.rotateAngleX) < 1e-6) cube.rotateAngleX = 0;
        if (Math.abs(cube.rotateAngleY) < 1e-6) cube.rotateAngleY = 0;
        if (Math.abs(cube.rotateAngleZ) < 1e-6) cube.rotateAngleZ = 0;

        // Allows scaling the cube with the cubeinfo scale.
        cube.scaleX = (float) cubeInfo.scale[0];
        cube.scaleY = (float) cubeInfo.scale[1];
        cube.scaleZ = (float) cubeInfo.scale[2];

        // Allows mirrored textures
        cube.mirror = cubeInfo.txMirror;
        // Allows hidden textures
        cube.isHidden = cubeInfo.hidden;

        if (cubeInfo.metadata != null)
        {
            for (String s : cubeInfo.metadata)
            {
                if (s.equalsIgnoreCase("trans"))
                {
                    cube.transluscent = true;
                }
            }
        }
        // Use cubeInfo.mcScale as the scale, this lets it work properly.
        cube.addBox((float) cubeInfo.offset[0], (float) cubeInfo.offset[1], (float) cubeInfo.offset[2],
                cubeInfo.dimensions[0], cubeInfo.dimensions[1], cubeInfo.dimensions[2], (float) cubeInfo.mcScale);

        return cube;
    }

    private void cube(CubeInfo cube, TabulaRenderer parent, String group)
    {
        TabulaRenderer modelRenderer = createModelRenderer(cube);

        nameMap.put(cube.name, modelRenderer);
        identifierMap.put(cube.identifier, modelRenderer);

        if (parent != null)
        {
            parent.addChild(modelRenderer);
        }

        // Only add root parts to the group set.
        if (parent == null)
        {
            ArrayList<TabulaRenderer> cubes;
            if (groupMap.containsKey(group))
            {
                cubes = (ArrayList<TabulaRenderer>) groupMap.get(group);
            }
            else
            {
                cubes = Lists.newArrayList();
                groupMap.put(group, cubes);
                groupIdents.add(group);
            }
            cubes.add(modelRenderer);

            Collections.sort(cubes, new Comparator<TabulaRenderer>()
            {
                @Override
                public int compare(TabulaRenderer o1, TabulaRenderer o2)
                {
                    String name1 = o1.name;
                    String name2 = o2.name;
                    if (o1.transluscent && !o2.transluscent) return 1;
                    if (o2.transluscent && !o1.transluscent) return -1;
                    return name1.compareTo(name2);
                }
            });
        }
        for (CubeInfo c : cube.children)
        {
            cube(c, modelRenderer, group);
        }
    }

    private void cubeGroup(CubeGroup group)
    {
        for (CubeInfo cube : group.cubes)
        {
            cube(cube, null, group.identifier);
        }

        for (CubeGroup c : group.cubeGroups)
        {
            cubeGroup(c);
        }
    }

    public TabulaRenderer getCube(String name)
    {
        return nameMap.get(name);
    }

    public boolean isAnimationInProgress()
    {
        return playingAnimation != null || !playing.isEmpty();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw,
            float rotationPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale, entity);

        double[] scales = tabulaModel.getScale();
        GL11.glScaled(scales[0], scales[1], scales[2]);

        // Render based on the group, this ensures they are correctly rendered.
        for (String s : groupIdents)
        {
            Collection<TabulaRenderer> cubes = groupMap.get(s);
            for (TabulaRenderer cube : cubes)
            {
                if (cube != null)
                {
                    if (texturer != null) texturer.bindObject(entity);
                    cube.setTexturer(texturer);
                    cube.setAnimationChanger(changer);
                    cube.render(0.0625f, entity);
                }
            }
        }
    }

    /** Sets the model's various rotation angles. For bipeds, limbSwing and
     * limbSwingAmount are used for animating the movement of arms and legs,
     * where limbSwing represents the time(so that arms and legs swing back and
     * forth) and limbSwingAmount represents how "far" arms and legs can swing
     * at most.
     *
     * @see net.minecraft.entity.Entity
     * @since 0.1.0 */
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw,
            float rotationPitch, float scaleFactor, Entity entity)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scaleFactor,
                entity);
    }

    /** Starts running the given animation.
     * 
     * @param id */
    public void startAnimation(String id, IAnimationHolder animate)
    {
        if (!id.equals(playingAnimation)) stopAnimation(animate);
        if (playingAnimation == null)
        {
            playingAnimation = id;
            List<Animation> anims = animationMap.get(id);
            if (anims == null) stopAnimation(animate);
            else playing.addAll(anims);
        }
    }

    public void startAnimation(List<Animation> list, IAnimationHolder animate)
    {
        if (list == null || list.isEmpty())
        {
            stopAnimation(animate);
            return;
        }
        String name = list.get(0).name;
        if (!name.equals(playingAnimation)) stopAnimation(animate);
        if (playingAnimation == null)
        {
            playingAnimation = name;
            List<Animation> anims = list;
            playing.addAll(anims);
        }
    }

    /** Stop all current running animations.
     *
     * @since 0.1.0 */
    public void stopAnimation(IAnimationHolder animate)
    {
        playingAnimation = null;
        this.playing.clear();
        animate.clean();
    }

    public void stopAnimation(Animation toStop, IAnimationHolder animate)
    {
        playing.remove(toStop);
        animate.setStep(toStop, 0);
    }

    public void updateAnimation(IAnimationHolder animate, Animation animation, int tick, float partialTick,
            float limbSwing)
    {
        int aniTick = animate.getStep(animation);
        if (aniTick == 0) aniTick = tick;
        float time1 = aniTick;
        float time2 = 0;
        int animationLength = animation.getLength();
        time2 = limbSwing % animationLength;
        time1 = (time1 + partialTick) % animationLength;
        animate.setStep(animation, tick);
        for (Entry<String, ArrayList<AnimationComponent>> entry : animation.sets.entrySet())
        {
            TabulaRenderer animating = identifierMap.get(entry.getKey());
            for (AnimationComponent component : entry.getValue())
            {
                float time = component.limbBased ? time2 : time1;
                if (time >= component.startKey)
                {
                    float componentTimer = time - component.startKey;

                    if (componentTimer > component.length)
                    {
                        componentTimer = component.length;
                    }
                    animating.scaleX += (float) (component.scaleChange[0] / component.length * componentTimer);
                    animating.scaleY += (float) (component.scaleChange[1] / component.length * componentTimer);
                    animating.scaleZ += (float) (component.scaleChange[2] / component.length * componentTimer);

                    animating.rotationPointX += component.posChange[0] / component.length * componentTimer;
                    animating.rotationPointY += component.posChange[1] / component.length * componentTimer;
                    animating.rotationPointZ += component.posChange[2] / component.length * componentTimer;

                    animating.rotateAngleX += Math
                            .toRadians(component.rotChange[0] / component.length * componentTimer);
                    animating.rotateAngleY += Math
                            .toRadians(component.rotChange[1] / component.length * componentTimer);
                    animating.rotateAngleZ += Math
                            .toRadians(component.rotChange[2] / component.length * componentTimer);

                    animating.rotationPointX += component.posOffset[0];
                    animating.rotationPointY += component.posOffset[1];
                    animating.rotationPointZ += component.posOffset[2];

                    // Rotate by the Rotation Offset of the animation.
                    animating.rotateAngleX += Math.toRadians(component.rotOffset[0]);
                    animating.rotateAngleY += Math.toRadians(component.rotOffset[1]);
                    animating.rotateAngleZ += Math.toRadians(component.rotOffset[2]);

                }
            }
        }
    }
}
