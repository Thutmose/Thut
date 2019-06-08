package thut.tech.common.blocks.lift;

public class EventLiftUpdate extends Event
{
    private final TileEntityLiftAccess tile;

    public EventLiftUpdate(TileEntityLiftAccess tile)
    {
        this.tile = tile;
    }

    public TileEntityLiftAccess getTile()
    {
        return tile;
    }
}
