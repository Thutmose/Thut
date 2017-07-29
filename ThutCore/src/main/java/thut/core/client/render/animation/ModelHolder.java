package thut.core.client.render.animation;

import net.minecraft.util.ResourceLocation;

public class ModelHolder
{
    public ResourceLocation model;
    public ResourceLocation texture;
    public ResourceLocation animation;
    public String           name;

    public ModelHolder(ResourceLocation model, ResourceLocation texture, ResourceLocation animation, String name)
    {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.name = name;
    }

}
