package fi.evident.apina.gradle.tasks;

import fi.evident.apina.ApinaProcessor;
import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.spring.EndpointParameterNameNotDefinedException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.gradle.util.GFileUtils.writeFile;

@SuppressWarnings("unused")
public class ApinaTask extends DefaultTask {

    private File target;

    private FileCollection classpath;

    private List<String> blackBoxClasses = new ArrayList<>();

    private Map<String,List<String>> imports = new HashMap<>();

    private String platform = "angular2";

    public static final String GENERATE_API_CLIENT_TASK_NAME = "apina";

    @TaskAction
    public void generateTypeScript() throws IOException {
        try {
            requireNonNull(classpath, "classpath not initialized");
            requireNonNull(target, "target not initialized");

            Classpath myClasspath = new Classpath();
            for (File file : classpath)
                myClasspath.addRoot(file.toPath());

            ApinaProcessor processor = new ApinaProcessor(myClasspath);

            if (platform != null)
                processor.getSettings().setPlatform(platform);

            for (@Language("RegExp") String pattern : blackBoxClasses)
                processor.getSettings().getBlackBoxClasses().addPattern(pattern);

            for (Map.Entry<String, List<String>> anImport : imports.entrySet())
                processor.getSettings().addImport(anImport.getKey(), anImport.getValue());

            String output = processor.process();

            writeFile(output, target, "UTF-8");
        } catch (EndpointParameterNameNotDefinedException e) {
            getLogger().error("{}\nConsider adding 'compileJava { options.compilerArgs = ['-parameters'] }' to your build file.", e.getMessage());
            throw e;
        }
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

    @Input
    public Map<String, List<String>> getImports() {
        return imports;
    }

    public void setImports(Map<String, List<String>> imports) {
        this.imports = imports;
    }

    @Input
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
