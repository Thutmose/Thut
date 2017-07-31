package thut.lib;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
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
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CompatWrapper
{

    // Vanilla Section
    public static final ItemStack nullStack = null;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return ItemStack.loadItemStackFromNBT(tag);
    }

    public static ItemStack copy(ItemStack in)
    {
        return ItemStack.copyItemStack(in);
    }

    public static Entity createEntity(World world, String in)
    {
        return EntityList.createEntityByIDFromName(in, world);
    }

    public static Entity createEntity(World world, ResourceLocation in)
    {
        return EntityList.createEntityByIDFromName(in.toString(), world);
    }

    public static Entity createEntity(World world, Entity in)
    {
        return EntityList.createEntityByIDFromName(EntityList.getEntityString(in), world);
    }

    public static void moveEntitySelf(Entity in, double x, double y, double z)
    {
        in.moveEntity(x, y, z);
    }

    public static void sendChatMessage(ICommandSender to, ITextComponent message)
    {
        to.addChatMessage(message);
    }

    public static void registerTileEntity(Class<? extends TileEntity> tileClass, String id)
    {
        GameRegistry.registerTileEntity(tileClass, id);
    }

    public static void registerModEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod,
            int trackingRange, int updateFrequency, boolean sendsVelocityUpdates)
    {
        EntityRegistry.registerModEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency,
                sendsVelocityUpdates);
    }

    public static ItemStack setStackSize(ItemStack stack, int amount)
    {
        if (amount <= 0)
        {
            stack.stackSize = 0;
            return nullStack;
        }
        stack.stackSize = amount;
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        if (stack == nullStack || stack.stackSize < 0 || stack.getItem() == null) { return 0; }
        return stack.stackSize;
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
        stack.animationsToGo = num;
    }

    public static int increment(ItemStack in, int amt)
    {
        in.stackSize += amt;
        return in.stackSize;
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
        stack.getItem().onItemRightClick(stack, player.getEntityWorld(), player, hand);
    }

    public static NBTTagCompound getTag(ItemStack stack, String name, boolean create)
    {
        return stack.getSubCompound(name, create);
    }

    public static void processInitialInteract(Entity in, EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        in.processInitialInteract(player, stack, hand);
    }

    public static boolean interactWithBlock(Block block, World worldIn, BlockPos pos, IBlockState state,
            EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY,
            float hitZ)
    {
        return block.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    public static EntityEggInfo getEggInfo(String name, int colour1, int colour2)
    {
        return new EntityEggInfo(name, colour1, colour2);
    }

    @SuppressWarnings("deprecation")
    public static IBlockState getBlockStateFromMeta(Block block, int meta)
    {
        return block.getStateFromMeta(meta);
    }

    // Forge Section

    public static Type getBiomeType(String name)
    {
        try
        {
            return BiomeDictionary.Type.valueOf(name.toUpperCase(java.util.Locale.ENGLISH));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static boolean isOfType(Biome biome, BiomeDictionary.Type type)
    {
        return BiomeDictionary.isBiomeOfType(biome, type);
    }

    public static Set<Type> getTypes(Biome biome)
    {
        return Sets.newHashSet(BiomeDictionary.getTypesForBiome(biome));
    }

    public static Set<Biome> getBiomes(Type type)
    {
        return Sets.newHashSet(BiomeDictionary.getBiomesForType(type));
    }

}
