package fi.evident.apina.spring.java.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class AnnotationMetadata {

    private final QualifiedName name;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public AnnotationMetadata(QualifiedName name) {
        this.name = requireNonNull(name);
    }

    public QualifiedName getName() {
        return name;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(name);

        if (attributes.size() == 1 && attributes.containsKey("value")) {
            sb.append('(');
            writeValue(sb, attributes.get("value"));
            sb.append(')');

        } else if (!attributes.isEmpty()) {
            sb.append('(');
            for (Iterator<Map.Entry<String, Object>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
                String name = entry.getKey();
                Object value = entry.getValue();

                sb.append(name).append('=');

                writeValue(sb, value);

                if (it.hasNext())
                    sb.append(", ");
            }
            sb.append(')');
        }

        return sb.toString();
    }

    private void writeValue(StringBuilder sb, Object value) {
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            if (list.size() == 1)
                writePrimitive(sb, list.get(0));
            else
                sb.append(list.stream().map(Object::toString).collect(joining(",", "{", "}")));
        } else {
            writePrimitive(sb, value);
        }
    }

    private void writePrimitive(StringBuilder sb, Object value) {
        if (value instanceof String) {
            String s = (String) value;
            sb.append('"').append(s.replace("\"", "\\\"")).append('"');
        } else {
            sb.append(value);
        }
    }
}
