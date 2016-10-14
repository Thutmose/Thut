package thut.permissions;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GroupManager
{
    public static GroupManager instance;

    public Map<UUID, Group>    groupIDMap   = Maps.newHashMap();
    public Map<UUID, Player>   playerIDMap  = Maps.newHashMap();
    public Map<String, Group>  groupNameMap = Maps.newHashMap();
    public HashSet<Group>      groups       = Sets.newHashSet();
    public HashSet<Player>     players      = Sets.newHashSet();

    public Group               initial      = new Group("default");
    public Group               mods         = new Group("mods");

    public GroupManager()
    {
        System.out.println("Initializing Group Manager.");
    }

    public void init()
    {
        for (Group g : groups)
        {
            if (g.name.isEmpty()) g.name = "unnamed" + new Random().nextFloat();
            groupNameMap.put(g.name, g);
            for (UUID id : g.members)
            {
                groupIDMap.put(id, g);
            }
        }
        if (initial == null) initial = new Group("default");
        for (UUID id : initial.members)
        {
            groupIDMap.put(id, initial);
        }
        if (mods == null) mods = new Group("mods");
        for (UUID id : mods.members)
        {
            groupIDMap.put(id, mods);
        }
        for (Player player : players)
        {
            playerIDMap.put(player.id, player);
        }
        mods.all = true;
        groupNameMap.put("default", initial);
        groupNameMap.put("mods", mods);
    }

    public Player createPlayer(UUID id)
    {
        Player player = new Player();
        player.id = id;
        players.add(player);
        playerIDMap.put(id, player);
        return player;
    }

    public Group getPlayerGroup(UUID id)
    {
        Group ret = groupIDMap.get(id);
        if (ret == null)
        {
            if (initial == null) initial = new Group("default");
            ret = initial;
            groupIDMap.put(id, ret);
        }
        return ret;
    }

}
