package fi.evident.apina.gradle.tasks;

import fi.evident.apina.ApinaProcessor;
import fi.evident.apina.java.reader.Classpath;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.intellij.lang.annotations.Language;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.gradle.util.GFileUtils.writeFile;

public class ApinaTask extends DefaultTask {

    private File target;

    private FileCollection classpath;

    private List<String> blackBoxClasses = new ArrayList<>();

    public static final String GENERATE_API_CLIENT_TASK_NAME = "apina";

    @TaskAction
    public void generateTypeScript() throws IOException {
        requireNonNull(classpath, "classpath not initialized");
        requireNonNull(target, "target not initialized");

        Classpath myClasspath = new Classpath();
        for (File file : classpath)
            myClasspath.addRoot(file.toPath());

        ApinaProcessor processor = new ApinaProcessor(myClasspath);

        for (@Language("RegExp") String pattern : blackBoxClasses)
            processor.settings.blackBoxClasses.addPattern(pattern);

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

    @Input
    public List<String> getBlackBoxClasses() {
        return blackBoxClasses;
    }

    public void setBlackBoxClasses(List<String> blackBoxTypePatterns) {
        this.blackBoxClasses = blackBoxTypePatterns;
    }
}
