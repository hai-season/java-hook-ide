package org.hai.jhook;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static final Map<CommandType, ICommand> commandMap = new HashMap<>();

    public static ICommand getCommand (CommandType type) {
        return commandMap.get(type);
    }

    public static void registerCommand (CommandType type, ICommand command) {
        commandMap.put(type, command);
    }

    static {
        registerCommand(CommandType.REDEFINE_CLASS, new RedefineClassCommand());
        registerCommand(CommandType.GET_ALL_CLASSES, new GetAllClassesCommand());
        registerCommand(CommandType.GET_CLASS, new GetClassCommand());
        registerCommand(CommandType.LIST_CLASS, new ListClassCommand());
        registerCommand(CommandType.LIST_METHOD, new ListMethodCommand());
    }
}
