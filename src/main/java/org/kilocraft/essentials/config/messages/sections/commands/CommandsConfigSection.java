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

    @Setting(value = "playerHomes", comment = "Local Variables: {HOME_NAME}, {TARGET_TAG}, {HOMES_SIZE}")
    private PlayerHomesConfigSection playerHomesSection = new PlayerHomesConfigSection();

    @Setting(value = "rtp")
    private RtpCommandConfigSection rtpSection = new RtpCommandConfigSection();

    @Setting(value = "nickname", comment = "Local Variables: {NICK}, {NICK_NEW}, {TARGET_TAG}")
    private NicknameCommandConfigSection nickname = new NicknameCommandConfigSection();

    public CommandsContextConfigSection context() {
        return contextSection;
    }

    public WarpCommandConfigSection warp() {
        return warpCommand;
    }

    public PlayerHomesConfigSection playerHomes() {
        return playerHomesSection;
    }

    public RtpCommandConfigSection rtp() {
        return rtpSection;
    }

    public NicknameCommandConfigSection nickname() {
        return nickname;
    }

}
