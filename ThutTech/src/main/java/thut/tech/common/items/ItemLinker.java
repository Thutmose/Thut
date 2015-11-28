package thut.tech.common.items;

import java.io.File;
import java.util.UUID;

import akka.actor.FSM.State;
import scala.actors.threadpool.Arrays;
import thut.api.ThutBlocks;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.maths.ExplosionCustom.ExplosionStuff;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.EntityProjectile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public class ItemLinker extends Item
{
    public static Item instance;

    public ItemLinker()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("devicelinker");
        this.setCreativeTab(TechCore.tabThut);
        instance = this;
    }

    /** Called whenever this item is equipped and the right mouse button is
     * pressed. Args: itemStack, world, entityPlayer */
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
//        if (worldObj.isRemote || itemstack.getItemDamage() != 10) return itemstack;
//
//        Vector3 here = Vector3.getNewVectorFromPool().set(player);
//        BiomeGenBase b = here.getBiome(worldObj);
//
//        Vector3 direction = Vector3.getNewVectorFromPool().set(player.getLookVec());
//        Vector3 location2 = Vector3.getNextSurfacePoint(worldObj, here, direction, 255);
//        ExplosionCustom boom = new ExplosionCustom(worldObj, player, here, 200);
//        System.out.println(Arrays.toString(BiomeDictionary.getTypesForBiome(b)) + " " + location2);
//        
//        boom.doExplosion();
        
        

        
        // EntityProjectile p = new EntityProjectile(worldObj, here.x, here.y,
        // here.z, Blocks.stone);
        // here.set(player.getLookVec()).scalarMultBy(0);
        // here.setVelocities(p);
        // worldObj.spawnEntityInWorld(p);
        //
        return itemstack;
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        boolean ret = false;

        if (itemstack.getTagCompound() == null)
        {
            return false;
        }
        else
        {
            IBlockState state = worldObj.getBlockState(pos);
            
            if (state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER && !player.isSneaking())
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldObj.getTileEntity(pos);
                te.setSide(side, true);
                return true;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(itemstack.getTagCompound().getString("lift"));
            }
            catch (Exception e)
            {
                return false;
            }

            EntityLift lift = EntityLift.getLiftFromUUID(liftID);

            if (player.isSneaking() && lift != null && state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER)
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldObj.getTileEntity(pos);
                te.setLift(lift);
                int floor = te.getButtonFromClick(side, hitX, hitY, hitZ);
                te.setFloor(floor);
                
                String message = StatCollector.translateToLocalFormatted("msg.floorSet.name", floor);
                
                if (worldObj.isRemote) player.addChatMessage(new ChatComponentText(message));
                return true;
            }
        }
        return false;
    }

    public void setLift(EntityLift lift, ItemStack stack)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString("lift", lift.id.toString());
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }
}
