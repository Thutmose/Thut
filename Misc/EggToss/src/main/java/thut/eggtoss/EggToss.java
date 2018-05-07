package thut.eggtoss;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = EggToss.MODID, name = "Egg Toss", version = EggToss.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = EggToss.MCVERSIONS)
public class EggToss
{
    public static final String MODID      = "eggtoss";
    public static final String VERSION    = "1.0.0";
    public final static String MCVERSIONS = "[1.12.2]";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event)
    {
        EggItem item = new EggItem();
        item.setCreativeTab(CreativeTabs.TOOLS);
        item.setRegistryName(new ResourceLocation("eggtoss", "egg"));
        event.getRegistry().register(item);
    }

    public static class EggItem extends ItemEgg
    {
        @Nullable
        public Entity createEntity(World world, Entity location, ItemStack itemstack)
        {
            EntityChicken entity = new EntityChicken(world);
            entity.copyLocationAndAnglesFrom(location);
            return entity;
        }

        @Override
        public boolean hasCustomEntity(ItemStack stack)
        {
            return true;
        }
    }
}
