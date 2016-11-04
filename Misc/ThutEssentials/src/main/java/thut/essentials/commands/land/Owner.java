package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.Coordinate;

public class Owner extends BaseCommand
{

    public Owner()
    {
        super("landOwner", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
        int y = MathHelper.floor_double(sender.getPosition().getY() / 16f);
        int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
        int dim = sender.getEntityWorld().provider.getDimension();
        LandTeam owner = LandManager.getInstance().getLandOwner(new Coordinate(x, y, z, dim));
        if (owner == null) sender.addChatMessage(new TextComponentString("This Land is not owned"));
        else sender.addChatMessage(new TextComponentString("This Land is owned by Team " + owner.teamName));
    }

}
