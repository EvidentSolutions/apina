package fi.evident.apina.cli;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.spring.SpringModelReader;
import fi.evident.apina.tsang.AngularTypeScriptWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Apina {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("usage: apina INPUT OUTPUT");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);

        ApiDefinition api = SpringModelReader.readApiDefinition(input);
        AngularTypeScriptWriter.writeModel(output, api);
    }
}
