package fi.evident.apina.cli;

import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.spring.SpringModelReader;
import fi.evident.apina.tsang.AngularTypeScriptWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Apina {

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.printf("usage: apina INPUT1%sINPUT2%s... [OUTPUT]\n", File.pathSeparator, File.pathSeparator);
            System.exit(1);
        }

        try {
            Classpath classpath = Classpath.parse(args[0]);

            ApiDefinition api = SpringModelReader.readApiDefinition(classpath);

            AngularTypeScriptWriter writer = new AngularTypeScriptWriter(api);
            writer.writeApi();
            String output = writer.getOutput();

            if (args.length == 2) {
                Path outputFile = Paths.get(args[1]);
                Files.write(outputFile, output.getBytes(StandardCharsets.UTF_8));
            } else {
                System.out.println(output);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
