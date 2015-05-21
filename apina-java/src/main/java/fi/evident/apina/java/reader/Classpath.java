package fi.evident.apina.java.reader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class Classpath {

    private final List<Path> roots = new ArrayList<>();

    public final List<Path> getRoots() {
        return unmodifiableList(roots);
    }

    public void addRoot(Path root) {
        roots.add(requireNonNull(root));
    }

    public static Classpath parse(String path) {
        Classpath result = new Classpath();

        String[] elements = path.split(Pattern.quote(File.pathSeparator), -1);
        for (String part : elements)
            result.addRoot(Paths.get(part));

        return result;
    }

    @Override
    public String toString() {
        return roots.stream()
                .map(Path::toString)
                .collect(joining(File.pathSeparator));
    }
}
