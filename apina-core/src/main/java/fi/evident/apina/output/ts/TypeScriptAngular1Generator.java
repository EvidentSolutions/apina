package fi.evident.apina.output.ts;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.Endpoint;
import fi.evident.apina.model.EndpointGroup;
import fi.evident.apina.model.settings.ImportDefinition;
import fi.evident.apina.model.settings.TranslationSettings;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static fi.evident.apina.utils.CollectionUtils.join;
import static fi.evident.apina.utils.CollectionUtils.map;
import static fi.evident.apina.utils.ResourceUtils.readResourceAsString;
import static fi.evident.apina.utils.StringUtils.uncapitalize;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generates Angular 1 TypeScript code for client side.
 */
public final class TypeScriptAngular1Generator extends AbstractTypeScriptGenerator {

    public TypeScriptAngular1Generator(ApiDefinition api, TranslationSettings settings) {
        super(api, settings, "Types.", "Support.", "Support.IPromise");
    }

    public void writeApi() throws IOException {
        writeHeader();
        writeImports();
        out.writeExportedNamespace("Types", this::writeTypes);
        writeEndpoints(api.getEndpointGroups());
        writeRuntime();
    }

    private void writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */");
    }

    private void writeCreateEndpointGroups() {
        out.write("export function createEndpointGroups(context: Support.EndpointContext): Endpoints.IEndpointGroups ").writeBlock(() -> {
            out.write("return ").writeBlock(() -> {
                for (Iterator<EndpointGroup> it = api.getEndpointGroups().iterator(); it.hasNext(); ) {
                    EndpointGroup endpointGroup = it.next();

                    out.write(String.format("%s: new Endpoints.%s(context)", uncapitalize(endpointGroup.getName()), endpointGroup.getName()));

                    if (it.hasNext())
                        out.write(",");

                    out.writeLine();
                }
            });

            out.writeLine(";");
        });

        out.writeLine();
        out.writeLine();
    }

    public String getOutput() {
        return out.getOutput();
    }

    private void writeRuntime() throws IOException {
        out.write(readResourceAsString("typescript/runtime-angular1.ts", UTF_8));
        out.writeLine();
    }

    private void writeImports() {
        Collection<ImportDefinition> imports = settings.getImports();

        if (!imports.isEmpty()) {
            for (ImportDefinition anImport : imports)
                out.writeLine("import { " + join(anImport.getTypes(), ", ") + " } from '" + anImport.getModuleName() + "';");

            out.writeLine();
        }
    }

    private void writeEndpoints(Collection<EndpointGroup> endpointGroups) {
        out.writeExportedNamespace("Endpoints", () -> {

            List<String> names = map(endpointGroups, e -> uncapitalize(e.getName()));
            out.write("export const endpointGroupNames = ").writeValue(names).writeLine(";").writeLine();

            for (EndpointGroup endpointGroup : endpointGroups) {
                out.writeBlock("export class " + endpointGroup.getName(), () -> {

                    out.write("static KEY = ").writeValue(uncapitalize(endpointGroup.getName()) + "Endpoints").writeLine(";").writeLine();

                    out.writeBlock("constructor(private context: Support.EndpointContext)", () -> {
                    });

                    for (Endpoint endpoint : endpointGroup.getEndpoints()) {
                        writeEndpoint(endpoint);
                        out.writeLine().writeLine();
                    }
                });
            }

            out.writeExportedInterface("IEndpointGroups", () -> {
                for (EndpointGroup endpointGroup : endpointGroups)
                    out.writeLine(uncapitalize(endpointGroup.getName()) + ": " + endpointGroup.getName());
            });

            writeCreateEndpointGroups();
        });
    }

    private void writeEndpoint(Endpoint endpoint) {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock(() ->
                out.write("return this.context.request(").writeValue(createConfig(endpoint)).writeLine(");"));
    }


}
