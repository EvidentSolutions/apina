package fi.evident.apina.model.settings;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public final class ImportDefinition {

    private final String moduleName;
    private final Set<String> types = new TreeSet<>();

    public ImportDefinition(String moduleName) {
        this.moduleName = requireNonNull(moduleName);
    }

    public String getModuleName() {
        return moduleName;
    }

    public Set<String> getTypes() {
        return unmodifiableSet(types);
    }

    public void addType(String type) {
        types.add(requireNonNull(type));
    }
}
