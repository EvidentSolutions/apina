package fi.evident.apina.cli;

import fi.evident.apina.ApinaProcessor;
import fi.evident.apina.java.reader.Classpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Apina {

    private static final Logger log = LoggerFactory.getLogger(Apina.class);

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.printf("usage: apina INPUT1%sINPUT2%s... [OUTPUT]\n", File.pathSeparator, File.pathSeparator);
            System.exit(1);
        }

        try {
            Classpath classpath = Classpath.parse(args[0]);

            ApinaProcessor processor = new ApinaProcessor(classpath);
            String output = processor.process();

            if (args.length == 2) {
                Path outputFile = Paths.get(args[1]);

                log.debug("Writing API to '{}'", outputFile);
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
