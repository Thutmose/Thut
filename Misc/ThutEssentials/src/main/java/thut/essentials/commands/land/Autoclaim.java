package thut.essentials.commands.land;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.Coordinate;

public class Autoclaim extends BaseCommand
{
    private Map<EntityPlayer, Boolean> claimers = Maps.newHashMap();

    public Autoclaim()
    {
        super("autoclaim", 4);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean all = false;
        if (args.length > 0)
        {
            all = args[0].equalsIgnoreCase("all");
        }
        if (claimers.containsKey(sender))
        {
            claimers.remove(sender);
            sender.addChatMessage(new TextComponentString("Set Autoclaiming off"));
        }
        else
        {
            claimers.put((EntityPlayer) sender, all);
            sender.addChatMessage(new TextComponentString("Set Autoclaiming on"));
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntity().getEntityWorld().isRemote || evt.getEntity().isDead || claimers.isEmpty()) return;

        if (evt.getEntityLiving() instanceof EntityPlayer && claimers.containsKey(evt.getEntityLiving()))
        {
            boolean all = claimers.get(evt.getEntityLiving());
            LandTeam team = LandManager.getTeam((EntityPlayer) evt.getEntityLiving());
            if (team == null)
            {
                claimers.remove(evt.getEntityLiving());
                return;
            }
            int num = all ? 16 : 1;
            int n = 0;
            for (int i = 0; i < num; i++)
            {
                int x = MathHelper.floor_double(evt.getEntityLiving().getPosition().getX() / 16f);
                int y = MathHelper.floor_double(evt.getEntityLiving().getPosition().getY() / 16f) + i;
                if (all) y = i;
                int z = MathHelper.floor_double(evt.getEntityLiving().getPosition().getZ() / 16f);
                int dim = evt.getEntityLiving().getEntityWorld().provider.getDimension();
                if (y < 0 || y > 15) continue;
                if (LandManager.getInstance().getLandOwner(new Coordinate(x, y, z, dim)) != null)
                {
                    continue;
                }
                n++;
                LandManager.getInstance().addTeamLand(team.teamName, new Coordinate(x, y, z, dim), true);
            }
            if (n > 0)
            {
                evt.getEntityLiving()
                        .addChatMessage(new TextComponentString("Claimed This land for Team" + team.teamName));
            }
        }
    }

}
