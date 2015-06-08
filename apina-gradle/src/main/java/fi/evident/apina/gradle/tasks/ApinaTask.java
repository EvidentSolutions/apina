package fi.evident.apina.gradle.tasks;

import fi.evident.apina.ApinaProcessor;
import fi.evident.apina.java.reader.Classpath;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.gradle.util.GFileUtils.writeFile;

public class ApinaTask extends DefaultTask {

    private File target;

    private FileCollection classpath;

    private static final Logger log = LoggerFactory.getLogger(ApinaTask.class);

    public static final String GENERATE_API_CLIENT_TASK_NAME = "apina";

    @TaskAction
    public void generateTypeScript() throws IOException {
        requireNonNull(classpath, "classpath not initialized");
        requireNonNull(target, "target not initialized");

        Classpath myClasspath = new Classpath();
        for (File file : classpath)
            myClasspath.addRoot(file.toPath());

        ApinaProcessor processor = new ApinaProcessor(myClasspath);
        String output = processor.process();

        writeFile(output, target, "UTF-8");
    }

    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @OutputFile
    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }
}
