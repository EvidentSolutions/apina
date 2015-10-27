package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiTypeName;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.apina.utils.CollectionUtils.hasDuplicates;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class EnumDefinition {

    private final ApiTypeName type;

    private final List<String> constants;

    public EnumDefinition(ApiTypeName type, List<String> constants) {
        this.type = requireNonNull(type);
        this.constants = unmodifiableList(new ArrayList<>(constants));

        assert !hasDuplicates(constants);
    }

    public ApiTypeName getType() {
        return type;
    }

    public List<String> getConstants() {
        return constants;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
