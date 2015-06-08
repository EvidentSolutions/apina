package fi.evident.apina.cli;

import fi.evident.apina.ApinaProcessor;
import fi.evident.apina.java.reader.Classpath;
import org.intellij.lang.annotations.Language;
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
        CommandLineArguments arguments = CommandLineArguments.parse(args);

        if (arguments.files.size() != 1 && arguments.files.size() != 2) {
            System.err.printf("usage: apina INPUT1%sINPUT2%s... [OUTPUT]\n", File.pathSeparator, File.pathSeparator);
            System.exit(1);
        }

        try {
            Classpath classpath = Classpath.parse(arguments.files.get(0));

            ApinaProcessor processor = new ApinaProcessor(classpath);

            for (@Language("RegExp") String blackBoxPattern : arguments.blackBoxPatterns)
                processor.settings.blackBoxClasses.addPattern(blackBoxPattern);

            String output = processor.process();

            if (arguments.files.size() == 2) {
                Path outputFile = Paths.get(arguments.files.get(1));

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
