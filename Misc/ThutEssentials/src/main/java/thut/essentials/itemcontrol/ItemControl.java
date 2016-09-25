package thut.essentials.itemcontrol;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.util.ConfigManager;

public class ItemControl
{
    public static Set<String> blacklist;

    public static void init()
    {
        blacklist = Sets.newHashSet();
        for (String s : ConfigManager.INSTANCE.itemBlacklist)
            blacklist.add(s);
    }

    public ItemControl()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void denyPlayerHeldEvent(LivingUpdateEvent event)
    {
        if (!ConfigManager.INSTANCE.itemControlEnabled || event.getEntity().worldObj.isRemote) return;
        for (EnumHand hand : EnumHand.values())
        {
            Item item;
            if (event.getEntityLiving().getHeldItem(hand) != null
                    && (item = event.getEntityLiving().getHeldItem(hand).getItem()) != null)
            {
                String name = item.getRegistryName().toString();
                if (blacklist.contains(name))
                {
                    EntityItem drop = event.getEntityLiving().entityDropItem(event.getEntityLiving().getHeldItem(hand),
                            0);
                    drop.setPickupDelay(40);
                    event.getEntityLiving().setHeldItem(EnumHand.MAIN_HAND, null);
                    event.getEntityLiving().attackEntityFrom(DamageSource.generic,
                            (float) ConfigManager.INSTANCE.blacklistDamage);
                    event.getEntityLiving()
                            .addChatMessage(new TextComponentString("That item is not allowed to be held"));
                }
            }
        }
    }
}
