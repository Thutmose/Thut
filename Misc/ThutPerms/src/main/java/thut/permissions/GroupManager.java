package thut.permissions;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GroupManager
{
    public static GroupManager instance     = new GroupManager();

    public Map<UUID, Group>    groupIDMap   = Maps.newHashMap();
    public Map<UUID, Player>   playerIDMap  = Maps.newHashMap();
    public Map<String, Group>  groupNameMap = Maps.newHashMap();
    public HashSet<Group>      groups       = Sets.newHashSet();
    public HashSet<Player>     players      = Sets.newHashSet();

    public Group               initial;
    public Group               mods;

    public GroupManager()
    {
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
        for (Player player : players)
        {
            playerIDMap.put(player.id, player);
        }
    }

    public Player createPlayer(UUID id)
    {
        Player player = new Player();
        player.id = id;
        players.add(player);
        playerIDMap.put(id, player);
        return player;
    }

}
