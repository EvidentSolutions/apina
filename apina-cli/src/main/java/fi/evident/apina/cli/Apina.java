package fi.evident.apina.cli;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.spring.SpringModelReader;
import fi.evident.apina.spring.java.reader.Classpath;
import fi.evident.apina.tsang.AngularTypeScriptWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Apina {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: apina OUTPUT INPUT...");
            System.exit(1);
        }

        try {
            Classpath classpath = Classpath.parse(args[0]);
            Path output = Paths.get(args[1]);

            ApiDefinition api = SpringModelReader.readApiDefinition(classpath);
            AngularTypeScriptWriter.writeModel(output, api);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
