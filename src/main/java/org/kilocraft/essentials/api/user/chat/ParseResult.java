package org.kilocraft.essentials.api.user.chat;

import net.kyori.adventure.text.Component;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.ArrayList;
import java.util.List;

public class ParseResult {

    private final String input;
    private Component result;
    private final List<OnlineUser> pinged;
    private ParseType type;

    public ParseResult(String input) {
        this.input = input;
        this.result = Component.text(input);
        this.pinged = new ArrayList<>();
        this.type = ParseType.RAW;
    }

    public ParseResult addPinged(OnlineUser user) {
        this.pinged.add(user);
        return this;
    }

    public String getInput() {
        return this.input;
    }

    public Component getResult() {
        return this.result;
    }

    public ParseResult setResult(Component result) {
        this.result = result;
        return this;
    }

    public List<OnlineUser> getPinged() {
        return this.pinged;
    }

    public ParseType getType() {
        return this.type;
    }

    public ParseResult setType(ParseType type) {
        this.type = type;
        return this;
    }

    public enum ParseType {
        LINK(),
        ITEM(),
        PING(),
        RAW()
    }

}
