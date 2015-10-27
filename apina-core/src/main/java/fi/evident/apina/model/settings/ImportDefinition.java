package fi.evident.apina.model.settings;

import fi.evident.apina.model.type.ApiTypeName;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public final class ImportDefinition {

    private final String moduleName;
    private final Set<ApiTypeName> types = new TreeSet<>();

    public ImportDefinition(String moduleName) {
        this.moduleName = requireNonNull(moduleName);
    }

    public String getModuleName() {
        return moduleName;
    }

    public Set<ApiTypeName> getTypes() {
        return unmodifiableSet(types);
    }

    public void addType(ApiTypeName type) {
        types.add(requireNonNull(type));
    }
}
