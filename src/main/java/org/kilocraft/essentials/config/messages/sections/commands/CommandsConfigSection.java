package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandsConfigSection {

    @Setting(value = "broadCastFormat")
    public String broadCastFormat = "&f[&cBroadCast&f]&r %MESSAGE%";

    @Setting(value = "context")
    private final CommandsContextConfigSection contextSection = new CommandsContextConfigSection();

    @Setting(value = "serverWideWarps")
    private final WarpCommandConfigSection warpCommand = new WarpCommandConfigSection();

    @Setting(value = "playerWarp")
    private final PlayerWarpCommandConfigSection playerWarpSection = new PlayerWarpCommandConfigSection();

    @Setting(value = "nickname", comment = "Local Variables: {NICK}, {NICK_NEW}, {TARGET_TAG}")
    private final NicknameCommandConfigSection nickname = new NicknameCommandConfigSection();

    public CommandsContextConfigSection context() {
        return this.contextSection;
    }

    public WarpCommandConfigSection warp() {
        return this.warpCommand;
    }

    public NicknameCommandConfigSection nickname() {
        return this.nickname;
    }

    public PlayerWarpCommandConfigSection playerWarp() {
        return this.playerWarpSection;
    }

}
