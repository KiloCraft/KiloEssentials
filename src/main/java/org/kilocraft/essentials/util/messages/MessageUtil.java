package org.kilocraft.essentials.util.messages;

import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
import org.kilocraft.essentials.util.messages.nodes.CommandMessageNode;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;
import org.kilocraft.essentials.util.messages.nodes.GeneralMessageNode;

import java.io.IOException;
import java.util.Properties;

import static org.kilocraft.essentials.api.ModConstants.getResourceAsStream;

public class MessageUtil {
    private Properties GENERAL_MESSAGES = new Properties();
    private Properties COMMAND_MESSAGES = new Properties();
    private Properties EXCEPTION_MESSAGES = new Properties();
    private Properties ARGUMENT_EXCEPTION_MESSAGES = new Properties();

    public MessageUtil() throws IOException {
        GENERAL_MESSAGES.load(getResourceAsStream(getPath("general")));
        COMMAND_MESSAGES.load(getResourceAsStream(getPath("commands")));
        EXCEPTION_MESSAGES.load(getResourceAsStream(getPath("exceptions")));
        ARGUMENT_EXCEPTION_MESSAGES.load(getResourceAsStream(getPath("arg_exceptions")));
        
    }

    private String getPath(String fileName) {
        return "assets/messages/" + fileName + ".properties";
    }

    public String fromGeneralNode(GeneralMessageNode node) {
        return (!node.getKey().equals("")) ? GENERAL_MESSAGES.getProperty(node.getKey()) : "";
    }

    public String fromCommandNode(CommandMessageNode node) {
        return (!node.getKey().equals("")) ? COMMAND_MESSAGES.getProperty(node.getKey()) : "";
    }

    public String fromExceptionNode(ExceptionMessageNode node) {
        return (!node.getKey().equals("")) ? EXCEPTION_MESSAGES.getProperty(node.getKey()) : "";
    }

    public String fromArgumentExceptionNode(ArgExceptionMessageNode node) {
        return (!node.getKey().equals("")) ? ARGUMENT_EXCEPTION_MESSAGES.getProperty(node.getKey()) : "";
    }

    public String getGeneral(GeneralMessageNode node, Object... objects) {
        return (objects != null) ? String.format(fromGeneralNode(node), objects) : fromGeneralNode(node);
     }
}
