package thut.lib;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

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
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CompatWrapper
{
    public static final ItemStack nullStack = ItemStack.EMPTY;

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
        return EntityList.createEntityByIDFromName(EntityList.getKey(in), world);
    }

    public static void moveEntitySelf(Entity in, double x, double y, double z)
    {
        in.move(MoverType.SELF, x, y, z);
    }

    public static void sendChatMessage(ICommandSender to, ITextComponent message)
    {//
        to.sendMessage(message);
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
        stack.setCount(amount);
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        return stack.getCount();
    }

    public static boolean isValid(ItemStack stack)
    {
        if (stack == null)
        {
            System.err.println("Stacks should not be null!");
            Thread.dumpStack();
            return false;
        }
        return getStackSize(stack) > 0;
    }

    public static ItemStack validate(ItemStack in)
    {
        if (in == null || !isValid(in)) return nullStack;
        return in;
    }

    public static void setAnimationToGo(ItemStack stack, int num)
    {
        stack.setAnimationsToGo(num);
    }

    public static int increment(ItemStack in, int amt)
    {
        in.grow(amt);
        return in.getCount();
    }

    public static List<ItemStack> makeList(int size)
    {
        return NonNullList.<ItemStack> withSize(size, ItemStack.EMPTY);
    }

    public static void rightClickWith(ItemStack stack, EntityPlayer player, EnumHand hand)
    {
        ItemStack old = player.getHeldItem(hand);
        player.setHeldItem(hand, stack);
        stack.getItem().onItemRightClick(player.world, player, hand);
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

    @SuppressWarnings("deprecation")
    public static IBlockState getBlockStateFromMeta(Block block, int meta)
    {
        return block.getStateFromMeta(meta);
    }

    // Forge Section
    private static final Map<String, Type> byName = ReflectionHelper.getPrivateValue(Type.class, null, "byName");

    public static Type getBiomeType(String name)
    {
        return byName.get(name.toUpperCase());
    }

    public static boolean isOfType(Biome biome, BiomeDictionary.Type type)
    {
        return BiomeDictionary.hasType(biome, type);
    }

    public static Set<Type> getTypes(Biome biome)
    {
        return BiomeDictionary.getTypes(biome);
    }

    public static Set<Biome> getBiomes(Type type)
    {
        return BiomeDictionary.getBiomes(type);
    }
}