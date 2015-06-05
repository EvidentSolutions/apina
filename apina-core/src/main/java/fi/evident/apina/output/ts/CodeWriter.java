package fi.evident.apina.output.ts;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper for generating TypeScript code. Keeps track of indentation level
 * and supports proper writing of literal values.
 */
final class CodeWriter {

    private final StringBuilder out = new StringBuilder();
    private int indentationLevel = 0;
    private boolean beginningOfLine = true;

    public CodeWriter writeLine(String s) {
        write(s);
        writeLine();
        return this;
    }

    public String getOutput() {
        return out.toString();
    }

    public CodeWriter write(String s) {
        if (!s.isEmpty()) {
            writeIndentIfAtBegin();
            out.append(s);
        }

        return this;
    }

    private void writeIndentIfAtBegin() {
        if (beginningOfLine) {
            writeIndent();
            beginningOfLine = false;
        }
    }

    public CodeWriter writeLine() {
        out.append('\n');
        beginningOfLine = true;
        return this;
    }

    public CodeWriter writeValue(Object obj) {
        writeIndentIfAtBegin();

        if (obj instanceof Number) {
            out.append(obj.toString());
        } else if (obj instanceof Map<?, ?>) {
            writeMap((Map<?, ?>) obj);
        } else if (obj instanceof String) {
            writeString((String) obj);
        } else if (obj instanceof Collection<?>) {
            writeCollection((Collection<?>) obj);
        } else {
            out.append(String.valueOf(obj));
        }

        return this;
    }

    public CodeWriter writeExportedModule(String module, Runnable moduleWriter) {
        return writeBlock("export module " + module, moduleWriter);
    }

    public CodeWriter writeExportedInterface(String name, Runnable moduleWriter) {
        return writeBlock("export interface " + name, moduleWriter);
    }

    public CodeWriter writeBlock(String prefix, Runnable moduleWriter) {
        return write(prefix + " ").writeBlock(moduleWriter).writeLine().writeLine();
    }

    public CodeWriter writeBlock(Runnable block) {
        writeLine("{");
        indent();

        block.run();

        dedent();
        write("}");

        return this;
    }

    private void writeMap(Map<?, ?> obj) {
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

    private void writeCollection(Collection<?> obj) {
        write("[");

        for (Iterator<?> it = obj.iterator(); it.hasNext(); ) {
            writeValue(it.next());

            if (it.hasNext())
                write(", ");
        }

        write("]");
    }

    private void writeString(String s) {
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

    private void writeIndent() {
        for (int i = 0; i < indentationLevel; i++)
            out.append("    ");
    }
}
