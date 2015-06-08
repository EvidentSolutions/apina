package fi.evident.apina.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class CommandLineArguments {

    public final List<String> files = new ArrayList<>();
    public final List<String> blackBoxPatterns = new ArrayList<>();

    private void parse(String arg) {
        // The could be more general, but this is all we need for now.

        Optional<String> blackBox = parseOptionalWithValue("black-box", arg);

        if (blackBox.isPresent()) {
            blackBoxPatterns.add(blackBox.get());
        } else {
            files.add(arg);
        }
    }

    private static Optional<String> parseOptionalWithValue(String name, String arg) {
        String prefix = "--" + name + "=";
        if (arg.startsWith(prefix))
            return Optional.of(arg.substring(prefix.length()));
        else
            return Optional.empty();
    }

    public static CommandLineArguments parse(String[] args) {
        CommandLineArguments result = new CommandLineArguments();

        for (String arg : args)
            result.parse(arg);

        return result;
    }
}
