package fi.evident.apina.cli;

import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.spring.SpringModelReader;
import fi.evident.apina.tsang.AngularTypeScriptWriter;

import java.io.File;
import java.io.IOException;

public final class Apina {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.printf("usage: apina INPUT1%sINPUT2%s...\n", File.pathSeparator, File.pathSeparator);
            System.exit(1);
        }

        try {
            Classpath classpath = Classpath.parse(args[0]);

            ApiDefinition api = SpringModelReader.readApiDefinition(classpath);
            AngularTypeScriptWriter.printModel(api);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
