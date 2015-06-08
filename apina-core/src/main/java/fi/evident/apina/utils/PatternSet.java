package fi.evident.apina.utils;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A union of regex-patterns.
 */
public final class PatternSet implements Predicate<CharSequence> {

    private final List<Pattern> patterns = new ArrayList<>();

    public void addPattern(Pattern pattern) {
        patterns.add(requireNonNull(pattern));
    }

    public void addPattern(@Language("RegExp") String pattern) {
        addPattern(Pattern.compile(pattern));
    }

    @Override
    public boolean test(CharSequence s) {
        for (Pattern pattern : patterns)
            if (pattern.matcher(s).matches())
                return true;
        return false;
    }
}
