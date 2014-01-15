package thut.world.common.blocks.crystals;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.Blocks;
import thut.world.common.WorldCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSulfur extends BlockCrystal
{
	public BlockSulfur(int par1) 
	{
		super(par1);
		Blocks.sulfur = this;
		this.setUnlocalizedName("sulfur");
		drop = new ItemStack(this);
	}

    /**
     * Returns the ID of the items to drop on destruction.
     */
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return 0;//drop.itemID;
    }
    
    @Override
    public int quantityDropped(int meta, int fortune, Random random)
    {
    	return 0;
    }
    
    @SideOnly(Side.CLIENT)

    /**
     * Gets the icon name of the ItemBlock corresponding to this block. Used by hoppers.
     */
    public String getItemIconName()
    {
        return WorldCore.TEXTURE_PATH+"sulfur";
    }
    
    /**TODO account for hazmat
     * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
     */
    public void onEntityCollidedWithBlock(World worldObj, int x, int y, int z, Entity entity) 
    {

    }
    

}
