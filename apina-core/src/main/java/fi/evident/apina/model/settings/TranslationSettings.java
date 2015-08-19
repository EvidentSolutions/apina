package fi.evident.apina.model.settings;

import fi.evident.apina.utils.PatternSet;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableCollection;

/**
 * Various settings guiding the translation.
 */
public final class TranslationSettings {

    public final PatternSet blackBoxClasses = new PatternSet();
    private final TreeMap<String, ImportDefinition> importsByModule = new TreeMap<>();
    private final Set<String> importedTypes = new TreeSet<>();

    public boolean isBlackBoxClass(String name) {
        return blackBoxClasses.test(name);
    }

    public void addImport(String typeName, String moduleName) {
        if (!importedTypes.add(typeName))
            throw new IllegalArgumentException("type " + typeName + " is already imported");

        importsByModule.computeIfAbsent(moduleName, ImportDefinition::new).addType(typeName);
    }

    public Collection<ImportDefinition> getImports() {
        return unmodifiableCollection(importsByModule.values());
    }

    public boolean isImported(String typeName) {
        return importedTypes.contains(typeName);
    }
}
