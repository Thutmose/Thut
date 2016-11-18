package thut.lib;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CompatWrapper
{
    public static final ItemStack nullStack = ItemStack.field_190927_a;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return new ItemStack(tag);
    }

    public static ItemStack copy(ItemStack in)
    {
        return in.copy();
    }

    public static Entity createEntity(World world, String in)
    {
        return EntityList.createEntityByIDFromName(new ResourceLocation(in), world);
    }

    public static Entity createEntity(World world, ResourceLocation in)
    {
        return EntityList.createEntityByIDFromName(in, world);
    }

    public static Entity createEntity(World world, Entity in)
    {
        return EntityList.createEntityByIDFromName(EntityList.func_191301_a(in), world);
    }

    public static void moveEntitySelf(Entity in, double x, double y, double z)
    {
        in.moveEntity(MoverType.SELF, x, y, z);
    }

    public static void sendChatMessage(ICommandSender to, ITextComponent message)
    {//
        to.addChatMessage(message);
    }

    public static void registerTileEntity(Class<? extends TileEntity> tileClass, String id)
    {
        GameRegistry.registerTileEntity(tileClass, id);
    }

    public static void registerModEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod,
            int trackingRange, int updateFrequency, boolean sendsVelocityUpdates)
    {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        ResourceLocation regisrtyName;
        if (entityName.contains(":")) regisrtyName = new ResourceLocation(entityName);
        else regisrtyName = new ResourceLocation(mc.getModId(), entityName);
        EntityRegistry.registerModEntity(regisrtyName, entityClass, entityName, id, mod, trackingRange, updateFrequency,
                sendsVelocityUpdates);
    }

    public static ItemStack setStackSize(ItemStack stack, int amount)
    {
        if (amount <= 0) { return nullStack; }
        stack.func_190920_e(amount);
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        return stack.func_190916_E();
    }

    public static boolean isValid(ItemStack stack)
    {
        return getStackSize(stack) > 0;
    }

    public static ItemStack validate(ItemStack in)
    {
        if (!isValid(in)) return nullStack;
        return in;
    }

    public static void setAnimationToGo(ItemStack stack, int num)
    {
        stack.func_190915_d(num);
    }

    public static int increment(ItemStack in, int amt)
    {
        in.func_190917_f(amt);
        return in.func_190916_E();
    }

    public static List<ItemStack> makeList(int size)
    {
        List<ItemStack> ret = Lists.newArrayList();
        for (int i = 0; i < size; i++)
            ret.add(nullStack);
        return ret;
    }

    public static void rightClickWith(ItemStack stack, EntityPlayer player, EnumHand hand)
    {
        ItemStack old = player.getHeldItem(hand);
        player.setHeldItem(hand, stack);
        stack.getItem().onItemRightClick(player.worldObj, player, hand);
        player.setHeldItem(hand, old);
    }

    public static NBTTagCompound getTag(ItemStack stack, String name, boolean create)
    {
        NBTTagCompound ret = stack.getSubCompound(name);
        if (ret == null)
        {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            ret = new NBTTagCompound();
            stack.getTagCompound().setTag(name, ret);
        }
        return ret;
    }

    public static void processInitialInteract(Entity in, EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        in.processInitialInteract(player, hand);
    }

    public static boolean interactWithBlock(Block block, World worldIn, BlockPos pos, IBlockState state,
            EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY,
            float hitZ)
    {
        return block.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    public static EntityEggInfo getEggInfo(String name, int colour1, int colour2)
    {
        return new EntityEggInfo(new ResourceLocation(name), colour1, colour2);
    }
}