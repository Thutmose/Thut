package thut.essentials.economy;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.ThutEssentials;
import thut.essentials.land.LandSaveHandler;
import thut.essentials.util.Coordinate;

public class EconomyManager
{
    public static class Account
    {
        int                   balance;
        Set<Shop>             shops   = Sets.newHashSet();
        Map<Coordinate, Shop> shopMap = Maps.newHashMap();
    }

    public static class Shop
    {
        Coordinate location;
        Coordinate storage;
        UUID       frameId;
        boolean    infinite  = false;
        boolean    ignoreTag = false;
        boolean    sell;
        int        cost;
        int        number;

        public boolean transact(EntityPlayer player, Account shopAccount)
        {
            ItemStack stack = null;
            Entity ent = player.getServer().worldServerForDimension(player.dimension).getEntityFromUuid(frameId);
            if (ent instanceof EntityItemFrame) stack = ((EntityItemFrame) ent).getDisplayedItem();
            TileEntity tile = player.worldObj.getTileEntity(new BlockPos(location.x, location.y, location.z));
            if (stack == null || !(tile instanceof TileEntitySign))
            {
                removeShop(location);
                return false;
            }
            TileEntitySign sign = (TileEntitySign) tile;
            sell = sign.signText[0].getUnformattedText().contains("Sell");
            try
            {
                number = Integer.parseInt(sign.signText[1].getUnformattedText());
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
                return false;
            }
            try
            {
                cost = Integer.parseInt(sign.signText[3].getUnformattedText());
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
                return false;
            }
            if (sell)
            {
                int balance = getBalance(player);
                if (balance < cost)
                {
                    player.addChatMessage(new TextComponentString(TextFormatting.RED + "Insufficient Funds"));
                    return false;
                }
                stack = stack.copy();
                stack.stackSize = number;
                if (!infinite)
                {
                    int count = 0;
                    IInventory inv = null;
                    ItemStack test2 = stack.copy();
                    if (storage != null)
                    {
                        TileEntity inventory = player.worldObj
                                .getTileEntity(new BlockPos(storage.x, storage.y, storage.z));
                        if (inventory instanceof IInventory)
                        {
                            inv = (IInventory) inventory;
                            if (ignoreTag) test2.setTagCompound(new NBTTagCompound());
                            for (int i = 0; i < inv.getSizeInventory(); i++)
                            {
                                ItemStack item = inv.getStackInSlot(i);
                                if (item != null)
                                {
                                    ItemStack test = item.copy();
                                    if (ignoreTag) test.setTagCompound(new NBTTagCompound());
                                    test.stackSize = number;
                                    if (ItemStack.areItemStacksEqual(test, test2)) count += item.stackSize;
                                }
                            }
                        }
                    }
                    if (count < number || inv == null)
                    {
                        player.addChatMessage(new TextComponentString(TextFormatting.RED + "Insufficient Items"));
                        return false;
                    }
                    int i = 0;
                    Item itemIn = test2.getItem();
                    int metadataIn = test2.getItemDamage();
                    int removeCount = number;
                    NBTTagCompound itemNBT = ignoreTag ? null : test2.getTagCompound();
                    for (int j = 0; j < inv.getSizeInventory(); ++j)
                    {
                        ItemStack itemstack = inv.getStackInSlot(j);

                        if (itemstack != null && (itemIn == null || itemstack.getItem() == itemIn)
                                && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn)
                                && (itemNBT == null || NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true)))
                        {
                            int k = removeCount <= 0 ? itemstack.stackSize
                                    : Math.min(removeCount - i, itemstack.stackSize);
                            i += k;

                            if (removeCount != 0)
                            {
                                itemstack.stackSize -= k;

                                if (itemstack.stackSize == 0)
                                {
                                    inv.setInventorySlotContents(j, (ItemStack) null);
                                }
                                if (removeCount > 0 && i >= removeCount)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
                giveItem(player, stack);
                addBalance(player, -cost);
                if (!infinite) shopAccount.balance += cost;
                player.addChatMessage(new TextComponentString(
                        TextFormatting.GREEN + "Remaining Balance: " + TextFormatting.GOLD + getBalance(player)));
            }
            else
            {
                int balance = shopAccount.balance;
                if (balance < cost)
                {
                    player.addChatMessage(new TextComponentString(TextFormatting.RED + "Insufficient Funds"));
                    return false;
                }
                stack = stack.copy();
                stack.stackSize = number;
                int count = 0;
                for (ItemStack item : player.inventory.mainInventory)
                {
                    if (item != null)
                    {
                        ItemStack test = item.copy();
                        test.stackSize = number;
                        if (ItemStack.areItemStacksEqual(test, stack)) count += item.stackSize;
                    }
                }
                if (count < number)
                {
                    player.addChatMessage(new TextComponentString(TextFormatting.RED + "Insufficient Items"));
                    return false;
                }
                if (!infinite)
                {
                    if (storage == null)
                    {
                        player.addChatMessage(new TextComponentString(TextFormatting.RED + "No Storage"));
                        return false;
                    }
                    TileEntity te = player.worldObj.getTileEntity(new BlockPos(storage.x, storage.y, storage.z));
                    if (te instanceof IInventory)
                    {
                        IInventory inv = (IInventory) te;
                        count = 0;
                        ItemStack a = stack;
                        count = a.stackSize;
                        for (int i = 0; i < inv.getSizeInventory(); i++)
                        {
                            if (inv.getStackInSlot(i) == null || a.isItemEqual(inv.getStackInSlot(i)))
                            {
                                if (inv.getStackInSlot(i) != null && inv.getStackInSlot(i).stackSize + a.stackSize < 65)
                                {
                                    a.stackSize = inv.getStackInSlot(i).stackSize + a.stackSize;
                                    count = 0;
                                    inv.setInventorySlotContents(i, a.copy());
                                }
                                else if (inv.getStackInSlot(i) == null)
                                {
                                    count = 0;
                                    inv.setInventorySlotContents(i, a.copy());
                                }
                            }
                            if (count == 0) break;
                        }
                    }
                }

                player.inventory.clearMatchingItems(stack.getItem(), stack.getItemDamage(), number,
                        stack.getTagCompound());
                addBalance(player, cost);
                if (!infinite) shopAccount.balance -= cost;
                player.addChatMessage(new TextComponentString(
                        TextFormatting.GREEN + "Remaining Balance: " + TextFormatting.GOLD + getBalance(player)));
            }
            return false;
        }
    }

    public static final int         VERSION = 1;
    public static EconomyManager    instance;
    public int                      version = VERSION;
    public int                      initial = 1000;
    public Map<UUID, Account>       bank    = Maps.newHashMap();
    public Map<Coordinate, Account> shopMap = Maps.newHashMap();

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
            Coordinate c = new Coordinate(evt.getPos().down(), evt.getEntityPlayer().dimension);
            Shop shop = getShop(c);
            TileEntity tile = evt.getWorld().getTileEntity(new BlockPos(c.x, c.y, c.z));
            if (evt.getItemStack() != null && tile instanceof TileEntitySign && shop == null
                    && (evt.getItemStack().getDisplayName().contains("Shop")
                            || evt.getItemStack().getDisplayName().contains("InfShop")))
            {
                boolean infinite = evt.getItemStack().getDisplayName().contains("InfShop");
                String permission = infinite ? "make_infinite_shop" : "make_shop";
                if (!ThutEssentials.perms.hasPermission(evt.getEntityPlayer(), permission))
                {
                    evt.getEntityPlayer().addChatMessage(
                            new TextComponentString(TextFormatting.RED + "You are not allowed to make that shop."));
                    return;
                }
                shop = addShop(evt.getEntityPlayer(), (EntityItemFrame) evt.getTarget(), c, infinite);
                shop.ignoreTag = evt.getItemStack().getDisplayName().contains("noTag");
                return;
            }
            if (shop != null)
            {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void interactLeftClickEntity(AttackEntityEvent evt)
    {
        if (evt.getEntityPlayer().getEntityWorld().isRemote) return;
        if (evt.getTarget() instanceof EntityItemFrame)
        {
            Coordinate c = new Coordinate(evt.getTarget().getPosition().down(), evt.getEntityPlayer().dimension);
            Shop shop = getShop(c);
            if (shop != null) evt.setCanceled(true);
        }
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(receiveCanceled = true)
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getWorld().isRemote) return;
        Coordinate c = new Coordinate(evt.getPos(), evt.getEntityPlayer().dimension);
        Shop shop = getShop(c);
        if (shop != null)
        {
            boolean interact = shop.transact(evt.getEntityPlayer(), shopMap.get(c));
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

    public static Shop addShop(EntityPlayer owner, EntityItemFrame frame, Coordinate location, boolean infinite)
    {
        Account account = getInstance().getAccount(owner);
        Shop shop = new Shop();
        shop.infinite = infinite;
        shop.frameId = frame.getUniqueID();
        account.shops.add(shop);
        account.shopMap.put(location, shop);
        getInstance().shopMap.put(location, account);
        if (!shop.infinite)
        {
            shop.storage = new Coordinate(location.x, location.y - 1, location.z, location.dim);
        }
        EconomySaveHandler.saveGlobalData();
        return shop;
    }

    public static void removeShop(Coordinate location)
    {
        Account account = getInstance().shopMap.remove(location);
        if (account != null)
        {
            account.shops.remove(account.shopMap.remove(location));
            EconomySaveHandler.saveGlobalData();
        }
    }

    public static Shop getShop(Coordinate location)
    {
        Account account = getInstance().shopMap.get(location);
        if (account == null) return null;
        return account.shopMap.get(location);
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
