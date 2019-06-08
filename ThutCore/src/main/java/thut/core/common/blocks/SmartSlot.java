package thut.core.common.blocks;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmartSlot extends Slot
{
    String tex;

    public SmartSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public SmartSlot setTex(String tex)
    {
        this.tex = tex;
        return this;
    }

    /** Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots. */
    @Override
    public boolean isItemValid(@Nullable ItemStack stack)
    {
        return inventory.isItemValidForSlot(getSlotIndex(), stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.texture.TextureAtlasSprite getBackgroundSprite()
    {
        if (tex != null)
        {
            this.setBackgroundName(tex);
            this.setBackgroundLocation(new ResourceLocation(tex));
            TextureAtlasSprite sprite = getBackgroundMap().getTextureExtry(getSlotTexture());
            if (sprite == null)
            {
                getBackgroundMap().registerSprite(getBackgroundLocation());
                sprite = getBackgroundMap().getTextureExtry(getSlotTexture());
            }
        }
        else return super.getBackgroundSprite();
        TextureAtlasSprite sprite = super.getBackgroundSprite();
        sprite.initSprite(16, 16, 0, 0, false);
        return sprite;
    }

}
