package fi.evident.apina.tsang;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Helper for generating TypeScript code. Keeps track of indentation level
 * and supports proper writing of literal values.
 */
final class CodeWriter {

    private final Appendable out;
    private int indentationLevel = 0;
    private boolean beginningOfLine = true;

    CodeWriter(Appendable out) {
        this.out = requireNonNull(out);
    }

    public CodeWriter writeLine(String s) throws IOException {
        write(s);
        writeLine();
        return this;
    }

    public CodeWriter write(String s) throws IOException {
        if (!s.isEmpty()) {
            writeIndentIfAtBegin();
            out.append(s);
        }

        return this;
    }

    private void writeIndentIfAtBegin() throws IOException {
        if (beginningOfLine) {
            writeIndent();
            beginningOfLine = false;
        }
    }

    public CodeWriter writeLine() throws IOException {
        out.append('\n');
        beginningOfLine = true;
        return this;
    }

    public CodeWriter writeValue(Object obj) throws IOException {
        writeIndentIfAtBegin();

        if (obj instanceof Number) {
            out.append(obj.toString());
        } else if (obj instanceof Map<?,?>) {
            writeMap((Map<?, ?>) obj);
        } else if (obj instanceof String) {
            writeString((String) obj);
        } else {
            out.append(String.valueOf(obj));
        }

        return this;
    }

    public CodeWriter writeBlock(WriteCallback block) throws IOException {
        writeLine("{");
        indent();

        block.write();

        dedent();
        write("}");

        return this;
    }

    private void writeMap(Map<?, ?> obj) throws IOException {
        if (obj.isEmpty()) {
            write("{}");
            return;
        }

        writeBlock(() -> {
            for (Iterator<? extends Map.Entry<?, ?>> it = obj.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<?, ?> entry = it.next();
                writeValue(entry.getKey());
                write(": ");
                writeValue(entry.getValue());

                if (it.hasNext())
                    write(", ");

                writeLine();
            }
        });
    }

    private void writeString(String s) throws IOException {
        out.append('\'');
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\'':
                    out.append("\\'");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('\'');
    }

    public CodeWriter indent() {
        indentationLevel++;
        return this;
    }

    public CodeWriter dedent() {
        if (indentationLevel == 0) throw new IllegalStateException();

        indentationLevel--;
        return this;
    }

    private void writeIndent() throws IOException {
        for (int i = 0; i < indentationLevel; i++)
            out.append("    ");
    }

    public interface WriteCallback {
        void write() throws IOException;
    }
}
