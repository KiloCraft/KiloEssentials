package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandsConfigSection {
    @Setting(value = "helpMessage", comment = "temp, will be removed, requires a TextComponent")
    public String helpMessage = "[{\"text\":\"Example Help Message!\", \"color\":\"green\"}, {\"text\":\" Edit this in /essentials/messages.hocon/\", \"color\":\"aqua\"}]";

    @Setting(value = "voteCommand", comment = "If you have enabled the VoteCommand feature you can set the message for it here")
    public String voteMessage = "[{\"text\":\"Example Vote Message!\", \"color\":\"green\"}, {\"text\":\" Edit this in /essentials/messages.hocon/\", \"color\":\"aqua\"}]";

    @Setting(value = "discordCommand", comment = "If you have enabled the DiscordCommand feature you can set the message for it here")
    public String discordMessage = "[{\"text\":\"Example Discord Message!\", \"color\":\"green\"}, {\"text\":\" Edit this in /essentials/messages.hocon/\", \"color\":\"aqua\"}]";

    @Setting(value = "broadCastFormat")
    public String broadCastFormat = "&f[&cBroadCast&f]&r %MESSAGE%";

    @Setting(value = "context")
    private CommandsContextConfigSection contextSection = new CommandsContextConfigSection();

    @Setting(value = "serverWideWarps")
    private WarpCommandConfigSection warpCommand = new WarpCommandConfigSection();

    @Setting(value = "rtp")
    private RtpCommandConfigSection rtpSection = new RtpCommandConfigSection();

    @Setting(value = "playerWarp")
    private PlayerWarpCommandConfigSection playerWarpSection = new PlayerWarpCommandConfigSection();

    @Setting(value = "nickname", comment = "Local Variables: {NICK}, {NICK_NEW}, {TARGET_TAG}")
    private NicknameCommandConfigSection nickname = new NicknameCommandConfigSection();

    public CommandsContextConfigSection context() {
        return contextSection;
    }

    public WarpCommandConfigSection warp() {
        return warpCommand;
    }

    public RtpCommandConfigSection rtp() {
        return rtpSection;
    }

    public NicknameCommandConfigSection nickname() {
        return nickname;
    }

    public PlayerWarpCommandConfigSection playerWarp() {
        return playerWarpSection;
    }

}
