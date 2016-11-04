package thut.essentials.economy;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.essentials.land.LandSaveHandler;
import thut.essentials.util.Coordinate;

public class EconomyManager
{
    public static class Account
    {
        int                   balance;
        Map<Coordinate, Shop> shops = Maps.newHashMap();
    }

    public static class Shop
    {
        Coordinate location;
        Coordinate storage;
        boolean    infinite;
        boolean    sell;
        int        cost;
        int        number;

        public boolean transact(EntityPlayer player, Account shopAccount)
        {
            ItemStack stack;
            
            System.out.println("interact");
            return false;
        }
    }

    public static final int         VERSION = 1;
    public static EconomyManager    instance;
    public int                      version = VERSION;
    public int                      initial = 1000;
    public Map<UUID, Account>       bank    = Maps.newHashMap();
    public Map<Coordinate, Account> shops   = Maps.newHashMap();

    public static void clearInstance()
    {
        if (instance != null)
        {
            LandSaveHandler.saveGlobalData();
            MinecraftForge.EVENT_BUS.unregister(instance);
        }
        instance = null;
    }

    public static EconomyManager getInstance()
    {
        if (instance == null) instance = new EconomyManager();
        return instance;
    }

    public EconomyManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void interactRightClickEntity(PlayerInteractEvent.EntityInteract evt)
    {
        if (evt.getWorld().isRemote) return;
        if (evt.getTarget() instanceof EntityItemFrame)
        {
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos().down(), evt.getEntityPlayer().dimension);
            Shop shop = getShop(c);
            if (shop == null)
            {
                Account account = getAccount(evt.getEntityPlayer());
                shop = new Shop();
                shop.location = c;
                shop.infinite = true;
                shop.cost = 10;
                shop.sell = true;
                account.shops.put(c, shop);
                shops.put(c, account);
                return;
            }
            if (evt.getHand() == EnumHand.MAIN_HAND)
            {
                EntityItemFrame frame = (EntityItemFrame) evt.getTarget();
                System.out.println(frame.getDisplayedItem());
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void interactLeftClickEntity(AttackEntityEvent evt)
    {
        if (evt.getEntityPlayer().getEntityWorld().isRemote) return;
        if (evt.getTarget() instanceof EntityItemFrame)
        {
            System.out.println(evt.getTarget());
        }
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(receiveCanceled = true)
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getWorld().isRemote) return;
        Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        Shop shop = getShop(c);
        if (shop != null)
        {
            boolean interact = shop.transact(evt.getEntityPlayer(), shops.get(c));
            if (interact)
            {

            }
        }
    }

    public Account getAccount(EntityPlayer player)
    {
        Account account = bank.get(player.getUniqueID());
        if (account == null)
        {
            bank.put(player.getUniqueID(), account = new Account());
            account.balance = initial;
            EconomySaveHandler.saveGlobalData();
        }
        return account;
    }

    public static Shop getShop(Coordinate location)
    {
        Account account = getInstance().shops.get(location);
        if (account == null) return null;
        return account.shops.get(location);
    }

    public static int getBalance(EntityPlayer player)
    {
        return getInstance().getAccount(player).balance;
    }

    public static void setBalance(EntityPlayer player, int amount)
    {
        Account account = getInstance().getAccount(player);
        account.balance = amount;
        EconomySaveHandler.saveGlobalData();
    }

    public static void addBalance(EntityPlayer player, int amount)
    {
        Account account = getInstance().getAccount(player);
        account.balance += amount;
        EconomySaveHandler.saveGlobalData();
    }

    public static void giveItem(EntityPlayer entityplayer, ItemStack itemstack)
    {
        boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);
        if (flag)
        {
            entityplayer.worldObj.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY,
                    entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.inventoryContainer.detectAndSendChanges();
        }
        else
        {
            EntityItem entityitem = entityplayer.dropItem(itemstack, false);
            if (entityitem != null)
            {
                entityitem.setNoPickupDelay();
                entityitem.setOwner(entityplayer.getName());
            }
        }
    }
}
