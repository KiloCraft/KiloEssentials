package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.time.LocalDateTime;
import java.util.Date;

public class DateArgument {
    private String input;
    private String type;
    private int amount;
    private Date date;

    public DateArgument(String input) {
        this.input = input;
    }

    public DateArgument parse() throws CommandSyntaxException {
        if (!input.matches("^\\d+\\w$"))
            throw KiloCommands.getException(ExceptionMessageNode.EMPTY).create();

        this.type = input.replace("\\d+", "");
        this.amount = Integer.parseInt(input);
        Date current = thisDate();

        return this;
    }

    public Date getDate() {
        return this.date;
    }

    private Date thisDate() throws CommandSyntaxException {
        switch (this.type) {
            case "s":

            case "m":

            case "h":

            case "d":

            default:
                throw KiloCommands.getException(ExceptionMessageNode.WRONG_DATE_ARGUMENT).create();
        }

    }


    private LocalDateTime now() {
        return LocalDateTime.now();
    }

}
