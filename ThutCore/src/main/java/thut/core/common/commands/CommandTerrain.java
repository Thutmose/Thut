package thut.core.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class CommandTerrain extends CommandBase
{

    public CommandTerrain()
    {
    }

    @Override
    public String getName()
    {
        return "tcterrain";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/tcterrain <arguments>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(player);
        System.out.println(segment.chunk);
        System.out.println(segment.getCentre());
        System.out.println(segment);

        if (args.length > 0)
        {
            BiomeType type = BiomeType.getBiome(args[0], false);
            segment.setBiome(Vector3.getNewVector().set(player), type);
        }
    }

}
