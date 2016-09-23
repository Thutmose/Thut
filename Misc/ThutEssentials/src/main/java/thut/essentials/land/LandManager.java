package thut.essentials.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.util.ConfigManager;

public class LandManager
{

    public static class Invites
    {
        public Set<String> teams = Sets.newHashSet();
    }

    public static class LandTeam
    {
        public static LandTeam loadFromNBT(NBTTagCompound nbt)
        {
            if (!nbt.hasKey("name")) return null;
            LandTeam team = new LandTeam(nbt.getString("name"));
            team.land.loadFromNBT(nbt.getCompoundTag("land"));
            for (LandChunk land : team.land.land)
            {
                LandManager.getInstance().landMap.put(land, team.teamName);
            }
            NBTTagList adminList = nbt.getTagList("admins", 10);
            for (int i = 0; i < adminList.tagCount(); i++)
            {
                team.admins.add(adminList.getCompoundTagAt(i).getString("N"));
            }
            return team;
        }

        public TeamLand land   = new TeamLand();
        String          teamName;
        Set<String>     admins = Sets.newHashSet();

        public LandTeam()
        {
        }

        public LandTeam(String name)
        {
            teamName = name;
        }
    }

    public static class TeamLand
    {
        public HashSet<LandChunk> land = Sets.newHashSet();

        public boolean addLand(LandChunk land)
        {
            return this.land.add(land);
        }

        public int countLand()
        {
            return land.size();
        }

        public void loadFromNBT(NBTTagCompound tag)
        {
            NBTTagList list = tag.getTagList("Land", 10);
            for (int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound landTag = list.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                LandChunk c = new LandChunk(loc[0], loc[1], loc[2], loc[3]);
                land.add(c);
            }
        }

        public boolean removeLand(LandChunk land)
        {
            return this.land.remove(land);
        }
    }

    static LandManager      instance;

    public static final int VERSION = 1;

    public static void clearInstance()
    {
        if (instance != null)
        {
            LandSaveHandler.saveGlobalData();
            for (String s : instance.teamMap.keySet())
                LandSaveHandler.saveTeam(s);
        }
        instance = null;
    }

    public static LandManager getInstance()
    {
        if (instance == null) instance = new LandManager();
        return instance;
    }

    public static Team getTeam(EntityPlayer player)
    {
        Team playerTeam = player.getEntityWorld().getScoreboard().getPlayersTeam(player.getName());
        if (playerTeam == null)
        {
            getInstance().addToTeam(player, ConfigManager.INSTANCE.defaultTeamName);
        }
        return playerTeam;
    }

    protected HashMap<String, LandTeam>  teamMap;
    protected HashMap<LandChunk, String> landMap;
    protected HashMap<String, Invites>   inviteMap;
    protected HashSet<LandChunk>         publicBlocks;

    private LandManager()
    {
        publicBlocks = Sets.newHashSet();
        inviteMap = Maps.newHashMap();
        teamMap = Maps.newHashMap();
        landMap = Maps.newHashMap();
    }

    public void addTeamLand(String team, LandChunk land, boolean sync)
    {
        LandTeam t = teamMap.get(team);
        if (t == null)
        {
            Thread.dumpStack();
            return;
        }
        t.land.addLand(land);
        landMap.put(land, team);
        for (LandTeam t1 : teamMap.values())
        {
            if (t != t1) t1.land.removeLand(land);
        }
        if (sync)
        {
            LandSaveHandler.saveTeam(team);
        }
    }

    public void addToAdmins(String admin, String team)
    {
        LandTeam t = getTeam(team, true);
        System.out.println("Adding Admin " + admin + " to " + team);
        t.admins.add(admin);
        LandSaveHandler.saveTeam(team);
    }

    public void addToTeam(EntityPlayer player, String team)
    {
        if (player.getEntityWorld().getScoreboard().getTeam(team) == null)
        {
            player.getEntityWorld().getScoreboard().createTeam(team);
        }
        player.getEntityWorld().getScoreboard().addPlayerToTeam(player.getName(), team);
        player.addChatMessage(new TextComponentString("You joined Team " + team));
        LandTeam t = getTeam(team, true);
        if (t.admins.isEmpty())
        {
            addToAdmins(player.getName(), team);
        }
        Invites invite = inviteMap.get(player.getName());
        if (invite != null)
        {
            invite.teams.remove(team);
        }
    }

    public int countLand(String team)
    {
        LandTeam t = teamMap.get(team);
        if (t != null) { return t.land.countLand(); }
        return 0;
    }

    public void createTeam(EntityPlayer player, String team)
    {
        for (Object o : player.getEntityWorld().getScoreboard().getTeamNames())
        {
            String s = (String) o;
            if (s.equalsIgnoreCase(team))
            {
                player.addChatMessage(new TextComponentString("Team " + team + " Already Exists"));
                return;
            }
        }
        if (player.getEntityWorld().getScoreboard().getTeam(team) == null)
        {
            player.getEntityWorld().getScoreboard().createTeam(team);
            getTeam(team, true);
            addToTeam(player, team);
            addToAdmins(player.getName(), team);
        }
    }

    public List<String> getAdmins(String team)
    {
        List<String> ret = new ArrayList<String>();
        LandTeam t = teamMap.get(team);
        if (t != null) return Lists.newArrayList(t.admins);
        return ret;
    }

    public List<String> getInvites(String player)
    {
        List<String> ret = new ArrayList<String>();
        Invites invite = inviteMap.get(player);
        if (invite == null) return ret;
        return Lists.newArrayList(invite.teams);
    }

    public String getLandOwner(LandChunk land)
    {
        return landMap.get(land);
    }

    public LandTeam getTeam(String name, boolean create)
    {
        LandTeam team = teamMap.get(name);
        if (team == null && create)
        {
            team = new LandTeam(name);
            teamMap.put(name, team);
        }
        return team;
    }

    public List<LandChunk> getTeamLand(String team)
    {
        ArrayList<LandChunk> ret = new ArrayList<LandChunk>();
        LandTeam t = teamMap.get(team);
        if (t != null) ret.addAll(t.land.land);
        return ret;
    }

    public boolean hasInvite(String player, String team)
    {
        Invites invite = inviteMap.get(player);
        if (invite != null) return invite.teams.contains(team);
        return false;
    }

    public void invite(String admin, String player, String team)
    {
        if (!isAdmin(admin, team)) return;
        if (hasInvite(player, team)) return;
        Invites invite = inviteMap.get(player);
        if (invite == null)
        {
            invite = new Invites();
            inviteMap.put(player, invite);
        }
        invite.teams.add(team);
    }

    public boolean isAdmin(String name, String team)
    {
        LandTeam t = teamMap.get(team);
        if (t != null) return t.admins.contains(name);
        return false;
    }

    public boolean isAdmin(String name, Team team)
    {
        return isAdmin(name, team.getRegisteredName());
    }

    public boolean isOwned(LandChunk land)
    {
        return landMap.containsKey(land);
    }

    public boolean isPublic(LandChunk c)
    {
        return publicBlocks.contains(c);
    }

    public boolean isTeamLand(LandChunk chunk, String team)
    {
        LandTeam t = teamMap.get(team);
        if (t != null) return t.land.land.contains(chunk);
        return false;
    }

    public void loadFromNBT(NBTTagCompound nbt)
    {
        NBTBase base;
        if (((base = nbt.getTag("PublicBlocks")) instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) base;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                LandChunk c = new LandChunk(loc[0], loc[1], loc[2], loc[3]);
                publicBlocks.add(c);
            }
        }
        if (((base = nbt.getTag("Invites")) instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) base;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                String name = tag.getString("name");
                Invites invites;
                inviteMap.put(name, invites = new Invites());
                NBTTagList teams = tag.getTagList("teams", 10);
                for (int i1 = 0; i1 < teams.tagCount(); i1++)
                {
                    invites.teams.add(teams.getCompoundTagAt(i1).getString("T"));
                }
            }
        }
    }

    public void loadFromNBTOld(NBTTagCompound nbt)
    {
        if ((nbt.getTag("LandMap") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("LandMap");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                String team = landTag.getString("Team");
                if (loc.length != 4 || team.isEmpty()) continue;
                LandChunk c = new LandChunk(loc[0], loc[1], loc[2], loc[3]);
                addTeamLand(team, c, false);
            }
        }
        if ((nbt.getTag("Admins") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("Admins");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound adminTag = tagList.getCompoundTagAt(i);
                String[] admins = adminTag.getString("Admins").split(":");
                String team;
                getTeam(team = adminTag.getString("Team"), true);
                for (String s : admins)
                {
                    if (s == null || s.isEmpty())
                    {
                        addToAdmins(s, team);
                    }
                }
            }
        }
        if ((nbt.getTag("PublicBlocks") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("PublicBlocks");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                LandChunk c = new LandChunk(loc[0], loc[1], loc[2], loc[3]);
                publicBlocks.add(c);
            }
        }
    }

    public void loadTeamFromNBT(NBTTagCompound nbt)
    {
        LandTeam team = LandTeam.loadFromNBT(nbt);
        if (team != null)
        {
            teamMap.put(team.teamName, team);
            LandSaveHandler.saveTeam(team.teamName);
        }
    }

    public void removeFromAdmins(String admin, String team)
    {
        LandTeam t = teamMap.get(team);
        if (t != null && t.admins.contains(admin))
        {
            t.admins.remove(admin);
            System.out.println("Removing Admin " + admin + " to " + team);
            LandSaveHandler.saveTeam(team);
        }
    }

    public void removeFromInvites(String player, String team)
    {
        Invites invites = inviteMap.get(player);
        if (invites != null && invites.teams.contains(team))
        {
            invites.teams.remove(team);
            LandSaveHandler.saveGlobalData();
        }
    }

    public void removeFromTeam(EntityPlayer admin, String team, String toRemove)
    {
        ScorePlayerTeam oldTeam = admin.getEntityWorld().getScoreboard().getPlayersTeam(toRemove);
        if (oldTeam != null)
        {
            removeFromAdmins(toRemove, team);
            admin.getEntityWorld().getScoreboard().removePlayerFromTeam(toRemove, oldTeam);
        }
        this.addToTeam(admin, ConfigManager.INSTANCE.defaultTeamName);
    }

    public void removeTeamLand(String team, LandChunk land)
    {
        LandTeam t = teamMap.get(team);
        landMap.remove(land);
        if (t != null && t.land.removeLand(land))
        {
            LandSaveHandler.saveTeam(team);
        }
    }

    public void setPublic(LandChunk c)
    {
        publicBlocks.add(c);
        LandSaveHandler.saveGlobalData();
    }

    public void unsetPublic(LandChunk c)
    {
        if (!publicBlocks.contains(c)) return;
        publicBlocks.remove(c);
        LandSaveHandler.saveGlobalData();
    }

}
