package thut.lootcrates;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.lootcrates.XMLStuff.Crate;
import thut.lootcrates.commands.CommandManager;
import thut.lootcrates.config.Config.ConfigData;
import thut.lootcrates.config.Configure;

@Mod(LootCrates.MODID)
public class LootCrates
{
    public static class Config extends ConfigData
    {
        @Configure(category = "general")
        public boolean chestOpens = false;

        public Config()
        {
            super(LootCrates.MODID);
        }

        @Override
        public void onUpdated()
        {
        }

    }

    public static final String MODID  = "loot_crates";
    public static File         dir;
    public static final Config config = new Config();
    public static final Logger LOGGER = LogManager.getLogger(LootCrates.MODID);

    public LootCrates()
    {
        MinecraftForge.EVENT_BUS.register(this);
        thut.lootcrates.config.Config.setupConfigs(LootCrates.config, LootCrates.MODID, LootCrates.MODID);

        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(LootCrates.MODID + ".log").toFile();
        if (logfile.exists()) logfile.delete();
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LootCrates.LOGGER;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                LootCrates.MODID).build();
        logger.addAppender(appender);
        appender.start();
        LootCrates.dir = FMLPaths.CONFIGDIR.get().resolve(LootCrates.MODID).toFile();
        XMLStuff.instance = new XMLStuff();
    }

    @SubscribeEvent
    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        XMLStuff.instance.init();
    }

    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event)
    {
        CommandManager.register_commands(event.getCommandDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void rightClickBlockEvent(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
        if (tile instanceof INameable && ((INameable) tile).hasCustomName())
        {
            final ITextComponent nameComp = ((INameable) tile).getCustomName();
            final String name = nameComp.getFormattedText();
            if (XMLStuff.instance.map.isEmpty()) XMLStuff.instance.init();
            final Crate crate = XMLStuff.instance.map.get(name);
            if (crate != null)
            {
                boolean hasKey = evt.getItemStack() != null && evt.getItemStack().hasTag() && evt.getItemStack()
                        .getTag().contains("key");
                if (hasKey) hasKey = name.equals(evt.getItemStack().getTag().getString("key"));
                if (!hasKey)
                {
                    evt.getPlayer().sendMessage(new StringTextComponent("You did not use the correct key"));
                    evt.setCanceled(true);
                    return;
                }
                final ITextComponent message = new StringTextComponent(TextFormatting.GOLD + "[Loot Crate] "
                        + TextFormatting.RESET + evt.getPlayer().getDisplayName().getFormattedText()
                        + TextFormatting.GOLD + " has recieved ");
                final ITextComponent footer = new StringTextComponent(TextFormatting.GOLD + " from " + name);
                final ITextComponent rewards = crate.getReward((ServerPlayerEntity) evt.getPlayer());
                if (!rewards.getUnformattedComponentText().isEmpty())
                {
                    message.appendSibling(rewards).appendSibling(footer);
                    evt.getPlayer().getServer().getPlayerList().sendMessage(message);
                }
                evt.getItemStack().split(1);
                evt.getPlayer().container.detectAndSendChanges();
                if (!LootCrates.config.chestOpens)
                {
                    evt.setCanceled(true);
                    return;
                }
            }
        }
    }
}
