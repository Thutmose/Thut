package thut.essentials.areacontrol;

import java.util.Set;

import com.google.common.collect.Sets;

public class Area
{
    int              xMin;
    int              yMin;
    int              zMin;
    int              xMax;
    int              yMax;
    int              zMax;
    int              direction;
    Set<Requirement> requirements = Sets.newHashSet();
    public Area()
    {
        // TODO Auto-generated constructor stub
    }

}
