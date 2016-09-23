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
    public Map<String, Group>  groupNameMap = Maps.newHashMap();
    public HashSet<Group>      groups       = Sets.newHashSet();

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
    }

}
