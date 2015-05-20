package fi.evident.apina.cli;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.spring.SpringModelReader;
import fi.evident.apina.tsang.AngularTypeScriptWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class Apina {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: apina OUTPUT INPUT...");
            System.exit(1);
        }

        try {
            Path output = Paths.get(args[0]);
            List<Path> inputs = Stream.of(args).skip(1).map(Paths::get).collect(toList());

            ApiDefinition api = SpringModelReader.readApiDefinition(inputs);
            AngularTypeScriptWriter.writeModel(output, api);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
