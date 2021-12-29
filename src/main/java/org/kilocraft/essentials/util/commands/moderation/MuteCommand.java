package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.events.PunishEvents;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.MutedPlayerEntry;

import java.util.Date;
import net.minecraft.commands.CommandSourceStack;

public class MuteCommand extends EssentialCommand {
    public MuteCommand() {
        super("mute", CommandPermission.MUTE);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> user = this.getUserArgument("victim")
                .executes(ctx -> this.execute(ctx, null, false));
        RequiredArgumentBuilder<CommandSourceStack, String> reason = this.argument("reason", StringArgumentType.greedyString())
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent").then(
                this.getUserArgument("victim")
                        .executes(ctx -> this.execute(ctx, null, true))
                        .then(this.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), true)))
        );

        user.then(reason);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, @Nullable String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        Date date = new Date();

        super.resolveAndGetProfileAsync(ctx, "victim").thenAcceptAsync((victim) -> {
            MutedPlayerEntry entry = new MutedPlayerEntry(victim, date, src.getName(), null, reason);
            this.getUserManager().getMutedPlayerList().add(entry);

            PunishEvents.MUTE.invoker().onMute(src, EntityIdentifiable.fromGameProfile(victim), entry.getReason(), -1L, silent);

            this.getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.MUTE, null, silent);
        });

        return AWAIT;
    }
}
