package thut.essentials.economy;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import thut.essentials.land.LandSaveHandler;

public class EconomyManager
{
    public static class Account
    {
        int balance;
    }

    public static final int      VERSION = 1;
    public static EconomyManager instance;
    public int                   version = VERSION;
    public int                   initial = 1000;
    public Map<UUID, Account>    bank    = Maps.newHashMap();

    public static void clearInstance()
    {
        if (instance != null)
        {
            LandSaveHandler.saveGlobalData();
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

}
