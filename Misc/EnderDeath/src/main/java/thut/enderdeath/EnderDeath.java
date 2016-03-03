package thut.enderdeath;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "enderdeath", name = "EnderDeath", version = "1.0.0", acceptableRemoteVersions = "*")
public class EnderDeath
{
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void layerDeath(LivingDeathEvent evt)
    {
        if (!(evt.entity instanceof EntityPlayer)) return;

        if (evt.entity.worldObj.isRemote) return;

        EntityPlayer player = (EntityPlayer) evt.entity;
        InventoryPlayer inv = player.inventory;

        for (int i = 0; i < inv.mainInventory.length; i++)
        {
            ItemStack stack = inv.mainInventory[i];
            if (stack != null)
            {
                stack = player.getInventoryEnderChest().func_174894_a(stack);
                inv.mainInventory[i] = stack;
            }
        }
        for (int i = 0; i < inv.armorInventory.length; i++)
        {
            ItemStack stack = inv.armorInventory[i];
            if (stack != null)
            {
                stack = player.getInventoryEnderChest().func_174894_a(stack);
                inv.armorInventory[i] = stack;
            }
        }
    }
}
