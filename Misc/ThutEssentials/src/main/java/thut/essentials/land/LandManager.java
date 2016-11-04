package thut.essentials.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import thut.essentials.util.ConfigManager;

public class LandManager
{

    public static class Invites
    {
        public Set<String> teams = Sets.newHashSet();
    }

    public static class LandTeam
    {
        public TeamLand  land         = new TeamLand();
        public String    teamName;
        Set<String>      admins       = Sets.newHashSet();
        Set<UUID>        admin        = Sets.newHashSet();
        public Set<UUID> member       = Sets.newHashSet();
        public String    exitMessage  = "";
        public String    enterMessage = "";
        public String    denyMessage  = "";
        public boolean   reserved     = false;
        public boolean   players      = false;

        public LandTeam()
        {
        }

        public LandTeam(String name)
        {
            teamName = name;
        }

        public boolean isMember(EntityPlayer player)
        {
            if (player.getTeam() != null && player.getTeam().getRegisteredName().equals(teamName))
            {
                member.add(player.getUniqueID());
            }
            return member.contains(player.getUniqueID());
        }

        public boolean isAdmin(EntityPlayer player)
        {
            if (admin.contains(player.getUniqueID())) return true;
            if (admins.contains(player.getName()))
            {
                admins.remove(player.getName());
                admin.add(player.getUniqueID());
                return true;
            }
            return false;
        }

        public void init(MinecraftServer server)
        {
            for (String s : admins)
            {
                GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(null, s));
                if (profile.getId() != null) admin.add(profile.getId());
            }
            admins.clear();
            Scoreboard board = server.getEntityWorld().getScoreboard();
            Team team = board.getTeam(teamName);
            if (team != null)
            {
                for (String s : team.getMembershipCollection())
                {
                    GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(null, s));
                    if (profile.getId() != null) member.add(profile.getId());
                }
            }
            Set<UUID> members = Sets.newHashSet(member);
            for (UUID id : members)
            {
                if (!LandManager.instance.playerTeams.containsKey(id)
                        || (LandManager.instance.playerTeams.get(id).teamName
                                .equals(ConfigManager.INSTANCE.defaultTeamName)))
                    LandManager.instance.playerTeams.put(id, this);
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof LandTeam) { return ((LandTeam) o).teamName.equals(teamName); }
            return false;
        }

        @Override
        public int hashCode()
        {
            return teamName.hashCode();
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

    public static LandTeam getTeam(EntityPlayer player)
    {
        LandTeam playerTeam = getInstance().playerTeams.get(player.getUniqueID());
        if (playerTeam == null)
        {
            getInstance().addToTeam(player.getUniqueID(), ConfigManager.INSTANCE.defaultTeamName);
            playerTeam = getInstance().getTeam(ConfigManager.INSTANCE.defaultTeamName, false);
        }
        return playerTeam;
    }

    public static LandTeam getDefaultTeam()
    {
        return getInstance().getTeam(ConfigManager.INSTANCE.defaultTeamName, true);
    }

    public static boolean owns(EntityPlayer player, LandChunk chunk)
    {
        return getTeam(player).equals(getInstance().getLandOwner(chunk));
    }

    protected HashMap<String, LandTeam>    teamMap      = Maps.newHashMap();
    protected HashMap<LandChunk, LandTeam> landMap      = Maps.newHashMap();
    protected HashMap<UUID, LandTeam>      playerTeams  = Maps.newHashMap();
    protected HashMap<UUID, Invites>       invites      = Maps.newHashMap();
    protected HashSet<LandChunk>           publicBlocks = Sets.newHashSet();
    public int                             version      = VERSION;

    private LandManager()
    {
    }

    public void renameTeam(String oldName, String newName) throws CommandException
    {
        if (teamMap.containsKey(newName)) throw new CommandException("Error, new team name already in use");
        LandTeam team = teamMap.remove(oldName);
        if (team == null) throw new CommandException("Error, specified team not found");
        teamMap.put(newName, team);
        for (Invites i : invites.values())
        {
            if (i.teams.remove(oldName))
            {
                i.teams.add(newName);
            }
        }
        team.teamName = newName;
        LandSaveHandler.saveTeam(newName);
        LandSaveHandler.deleteTeam(oldName);
    }

    public void removeTeam(String teamName)
    {
        LandTeam team = teamMap.remove(teamName);
        HashSet<LandChunk> land = Sets.newHashSet(landMap.keySet());
        for (LandChunk c : land)
        {
            if (landMap.get(c).equals(team))
            {
                landMap.remove(c);
            }
        }
        HashSet<UUID> ids = Sets.newHashSet(playerTeams.keySet());
        for (UUID id : ids)
        {
            if (playerTeams.get(id).equals(team))
            {
                playerTeams.remove(id);
            }
        }
        for (Invites i : invites.values())
        {
            i.teams.remove(teamName);
        }
        LandSaveHandler.deleteTeam(teamName);
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
        landMap.put(land, t);
        for (LandTeam t1 : teamMap.values())
        {
            if (t != t1) t1.land.removeLand(land);
        }
        if (sync)
        {
            LandSaveHandler.saveTeam(team);
        }
    }

    public void addAdmin(UUID admin, String team)
    {
        LandTeam t = getTeam(team, true);
        t.admin.add(admin);
        LandSaveHandler.saveTeam(team);
    }

    public void addToTeam(UUID member, String team)
    {
        LandTeam t = getTeam(team, true);
        if (t.admin.isEmpty() && !t.teamName.equals(ConfigManager.INSTANCE.defaultTeamName))
        {
            t.admin.add(member);
        }
        if (playerTeams.containsKey(member))
        {
            LandTeam old = playerTeams.remove(member);
            old.member.remove(member);
        }
        t.member.add(member);
        playerTeams.put(member, t);
        Invites invite = invites.get(member);
        if (invite != null)
        {
            invite.teams.remove(team);
        }
        LandSaveHandler.saveTeam(team);
    }

    public int countLand(String team)
    {
        LandTeam t = teamMap.get(team);
        if (t != null) { return t.land.countLand(); }
        return 0;
    }

    public void createTeam(UUID member, String team) throws CommandException
    {
        if (teamMap.containsKey(team)) throw new CommandException(team + " already exists!");
        getTeam(team, true);
        addToTeam(member, team);
        addAdmin(member, team);
    }

    public List<String> getAdmins(String team)
    {
        List<String> ret = new ArrayList<String>();
        LandTeam t = teamMap.get(team);
        if (t != null) return Lists.newArrayList(t.admins);
        return ret;
    }

    public List<String> getInvites(UUID member)
    {
        List<String> ret = new ArrayList<String>();
        Invites invite = invites.get(member);
        if (invite == null) return ret;
        return Lists.newArrayList(invite.teams);
    }

    public LandTeam getLandOwner(LandChunk land)
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

    public boolean hasInvite(UUID member, String team)
    {
        Invites invite = invites.get(member);
        if (invite != null) return invite.teams.contains(team);
        return false;
    }

    public boolean invite(UUID inviter, UUID invitee)
    {
        if (!isAdmin(inviter)) return false;
        String team = playerTeams.get(inviter).teamName;
        if (hasInvite(invitee, team)) return false;
        Invites invite = invites.get(invitee);
        if (invite == null)
        {
            invite = new Invites();
            invites.put(invitee, invite);
        }
        invite.teams.add(team);
        return true;
    }

    public boolean isAdmin(UUID member)
    {
        LandTeam team = playerTeams.get(member);
        if (team == null) return false;
        return team.admin.contains(member);
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

    public void removeAdmin(UUID member)
    {
        LandTeam t = playerTeams.get(member);
        if (t != null)
        {
            t.admin.remove(member);
        }
    }

    public void removeFromInvites(UUID member, String team)
    {
        Invites invite = invites.get(member);
        if (invite != null && invite.teams.contains(team))
        {
            invite.teams.remove(team);
            LandSaveHandler.saveGlobalData();
        }
    }

    public void removeFromTeam(UUID member)
    {
        LandTeam team = playerTeams.get(member);
        if (team != null)
        {
            team.admin.remove(member);
            team.member.remove(member);
            playerTeams.remove(member);
        }
    }

    public void removeTeamLand(String team, LandChunk land)
    {
        LandTeam t = teamMap.get(team);
        if (t != null && t.land.removeLand(land))
        {
            landMap.remove(land);
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
