package org.kilocraft.essentials.craft.commands.servermanagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class MotdCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> motdNode = CommandManager.literal("motd").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.motd.nomotd"), false);
			return 1;
		}).build();

		ArgumentCommandNode<ServerCommandSource, String> textNode = CommandManager
				.argument("text", StringArgumentType.string()).executes((context) -> {
					String text = context.getArgument("text", String.class);
					context.getSource().sendFeedback(LangText.getFormatter(true, "command.motd.success", text), false);
					File properties = new File(context.getSource().getMinecraftServer().getRunDirectory().getPath()
							+ "/server.properties");
					Mod.getLogger().debug(properties.getName());
					try {
						List<String> lines = Files.readAllLines(properties.toPath());

						for (int i = 0; i < lines.size(); i++) {
							if (lines.get(i).startsWith("motd=")) {
								lines.set(i, "motd=" + text);
							}
						}

						FileWriter fileWriter = new FileWriter(properties);
						for (int i = 0; i < lines.size(); i++) {
							fileWriter.write(lines.get(i));
						}
						
						fileWriter.close();
					} catch (IOException e) {
						Mod.getLogger().debug(e.getStackTrace().toString());
					}

					return 0;
				}).build();

		dispatcher.getRoot().addChild(motdNode);
		motdNode.addChild(textNode);
	}
}