package thut.lootcrates;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.lootcrates.XMLStuff.Crate;

@Mod(modid = LootCrates.MODID, name = "Loot Crates", version = LootCrates.VERSION, acceptableRemoteVersions = "*")
public class LootCrates
{
    public static final String MODID      = "loot_crates";
    public static final String VERSION    = "1.0.0";
    public static boolean      chestOpens = true;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        chestOpens = config.getBoolean("chestOpens", Configuration.CATEGORY_GENERAL, true,
                "if the chest opens when used.");
        config.save();
        MinecraftForge.EVENT_BUS.register(this);
        XMLStuff.instance = new XMLStuff(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        XMLStuff.instance.init();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandReload());
        event.registerServerCommand(new CommandKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void rightClickBlockEvent(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getWorld().isRemote) return;
        TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
        if (tile instanceof IInventory && ((IInventory) tile).hasCustomName())
        {
            String name = ((IInventory) tile).getName();
            if (XMLStuff.instance.map.isEmpty()) XMLStuff.instance.init();
            Crate crate = XMLStuff.instance.map.get(name);
            if (crate != null)
            {
                boolean hasKey = evt.getItemStack() != null && evt.getItemStack().hasTagCompound()
                        && evt.getItemStack().getTagCompound().hasKey("key");
                if (hasKey) hasKey = name.equals(evt.getItemStack().getTagCompound().getString("key"));
                if (!hasKey)
                {
                    evt.getEntityPlayer().addChatMessage(new TextComponentString("You did not use the key"));
                    evt.setCanceled(true);
                    return;
                }
                ITextComponent message = new TextComponentString(TextFormatting.GOLD + "[Loot Crate] "
                        + TextFormatting.RESET + evt.getEntityPlayer().getDisplayNameString() + TextFormatting.GOLD
                        + " has recieved ");
                ITextComponent footer = new TextComponentString(TextFormatting.GOLD + " from " + name);
                ITextComponent rewards = crate.getReward().giveRewards(evt.getEntityPlayer());
                message.appendSibling(rewards).appendSibling(footer);
                evt.getEntityPlayer().getServer().getPlayerList().sendChatMsg(message);
                evt.getItemStack().splitStack(1);
                evt.getEntityPlayer().inventoryContainer.detectAndSendChanges();
                if (!chestOpens)
                {
                    evt.setCanceled(true);
                    return;
                }
            }
        }
    }
}
