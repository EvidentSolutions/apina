package fi.evident.apina.model.settings;

import fi.evident.apina.utils.PatternSet;

/**
 * Various settings guiding the translation.
 */
public final class TranslationSettings {

    public final PatternSet blackBoxClasses = new PatternSet();

    public boolean isBlackBoxClass(String name) {
        return blackBoxClasses.test(name);
    }
}
