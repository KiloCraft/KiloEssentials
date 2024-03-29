package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;

import static net.kyori.adventure.text.Component.*;

public class TextFormattingCommand extends EssentialCommand {
    private static final Component message = ComponentText.toText(
            text().append(text("Text Formats", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(newline())
                    .append(
                            text("Full details:").append(space())
                                    .append(
                                            text("docs.adventure.kyori.net/minimessage.html#format",
                                                    Style.style(NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                                                            .hoverEvent(HoverEvent.showText(text("Click to open", NamedTextColor.LIGHT_PURPLE)))
                                                            .clickEvent(ClickEvent.openUrl("https://docs.adventure.kyori.net/minimessage.html#format")))
                                    )
                    ).build()
    );

    public TextFormattingCommand() {
        super("textformating", new String[]{"colours", "colors"});
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<CommandSourceStack> ctx) {
        this.getCommandSource(ctx).sendMessage(message);
        return SUCCESS;
    }
}
