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

            AngularTypeScriptWriter writer = new AngularTypeScriptWriter(api, System.out);

            // TODO: don't hard code these. these are here temporarily so that translating Yoke produces correct code
            writer.addStartDeclaration("type ResultTable = {}");
            writer.addStartDeclaration("type LocalDate = String");
            writer.addStartDeclaration("type LocalDateTime = String");
            writer.addStartDeclaration("type Duration = String");

            writer.writeApi();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
