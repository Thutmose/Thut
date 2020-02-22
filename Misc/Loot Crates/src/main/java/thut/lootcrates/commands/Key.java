package thut.lootcrates.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.lootcrates.XMLStuff;
import thut.lootcrates.XMLStuff.Crate;

public class Key
{

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String name = "lc_key";
        String perm;
        PermissionAPI.registerNode(perm = "command." + name, DefaultPermissionLevel.OP, "Can the player use /" + name);
        LiteralArgumentBuilder<CommandSource> command = Commands.literal(name).requires(cs -> CommandManager.hasPerm(cs,
                perm));

        final SuggestionProvider<CommandSource> suggestor = (ctx, sb) -> ISuggestionProvider.suggest(Key.getKeys(), sb);

        command = command.then(Commands.argument("target_player", EntityArgument.player()).then(Commands.argument(
                "crate", StringArgumentType.string()).suggests(suggestor).executes(ctx -> Key.execute(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target_player"), StringArgumentType.getString(ctx, "crate")))))
                .then(Commands.argument("number", IntegerArgumentType.integer()));
        commandDispatcher.register(command);

        command = Commands.literal(name).requires(cs -> CommandManager.hasPerm(cs, perm));
        command = command.then(Commands.argument("target_player", EntityArgument.player()).then(Commands.argument(
                "crate", StringArgumentType.string()).suggests(suggestor).then(Commands.argument("number",
                        IntegerArgumentType.integer()).executes(ctx -> Key.execute(ctx.getSource(), EntityArgument
                                .getPlayer(ctx, "target_player"), StringArgumentType.getString(ctx, "crate"),
                                IntegerArgumentType.getInteger(ctx, "number"))))));
        commandDispatcher.register(command);
    }

    private static List<String> getKeys()
    {
        if (XMLStuff.instance.map.isEmpty()) XMLStuff.instance.init();
        final List<String> crates = Lists.newArrayList(XMLStuff.instance.map.keySet());
        Collections.sort(crates);
        return crates;
    }

    private static int execute(final CommandSource source, final ServerPlayerEntity player, final String crate)
    {
        return Key.execute(source, player, crate, 1);
    }

    private static int execute(final CommandSource source, final ServerPlayerEntity player, final String crateName,
            final int number)
    {
        final Crate crate = XMLStuff.instance.map.get(crateName);
        if (crate != null)
        {
            final ItemStack key = crate.key.copy();
            key.setCount(number);
            XMLStuff.giveItem(player, key);
            source.sendFeedback(new StringTextComponent("Key(s) given."), true);
            return 0;
        }
        source.sendErrorMessage(new StringTextComponent("No crate found for name " + crateName));
        return 1;
    }
}
