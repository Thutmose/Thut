package thut.lootcrates.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.lootcrates.XMLStuff;

public class Reload
{

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String name = "lc_reload";
        String perm;
        PermissionAPI.registerNode(perm = "command." + name, DefaultPermissionLevel.OP, "Can the player use /" + name);
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal(name).requires(cs -> CommandManager
                .hasPerm(cs, perm));
        command.executes(ctx -> Reload.execute());
        // Actually register the command.
        commandDispatcher.register(command);
    }

    private static int execute()
    {
        XMLStuff.instance.init();
        return 0;
    }
}
