package fi.evident.apina.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class CommandLineArguments {

    public final List<String> files = new ArrayList<>();
    public final List<String> blackBoxPatterns = new ArrayList<>();
    public final List<ImportArgument> imports = new ArrayList<>();

    private void parse(String arg) {
        // This could be more general, but this is all we need for now.

        String blackBox = parseOptionalWithValue("black-box", arg).orElse(null);
        if (blackBox != null) {
            blackBoxPatterns.add(blackBox);
            return;
        }

        String anImport = parseOptionalWithValue("import", arg).orElse(null);
        if (anImport != null) {
            int colonIndex = anImport.indexOf(':');
            if (colonIndex == -1)
                throw new IllegalArgumentException("invalid import: " + anImport);

            String[] types = anImport.substring(0, colonIndex).split(",", -1);
            String module = anImport.substring(colonIndex+1);

            imports.add(new ImportArgument(Arrays.asList(types), module));
            return;
        }

        files.add(arg);
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

    public static final class ImportArgument {
        public final List<String> types;
        public final String module;

        public ImportArgument(List<String> types, String module) {
            this.types = types;
            this.module = module;
        }
    }
}
