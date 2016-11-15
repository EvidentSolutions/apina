package fi.evident.apina.output.ts;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.Endpoint;
import fi.evident.apina.model.EndpointGroup;
import fi.evident.apina.model.settings.ImportDefinition;
import fi.evident.apina.model.settings.TranslationSettings;

import java.io.IOException;
import java.util.Collection;

import static fi.evident.apina.utils.CollectionUtils.join;
import static fi.evident.apina.utils.ResourceUtils.readResourceAsString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generates Angular 2 TypeScript code for client side.
 */
public final class TypeScriptAngular2Generator extends AbstractTypeScriptGenerator {

    public TypeScriptAngular2Generator(ApiDefinition api, TranslationSettings settings) {
        super(api, settings, "", "", "Observable");
    }

    public void writeApi() throws IOException {
        writeHeader();
        writeImports();
        writeTypes();
        writeRuntime();
        writeEndpoints(api.getEndpointGroups());
        writeModule();
    }

    private void writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */");
    }

    public String getOutput() {
        return out.getOutput();
    }

    private void writeRuntime() throws IOException {
        out.write(readResourceAsString("typescript/runtime-angular2.ts", UTF_8));
        out.writeLine();
    }

    private void writeImports() {
        Collection<ImportDefinition> imports = settings.getImports();

        out.writeLine("import { Injectable, NgModule } from '@angular/core';");
        out.writeLine("import { Http, HttpModule } from '@angular/http';");
        out.writeLine("import { Observable } from 'rxjs/Observable';");
        out.writeLine("import 'rxjs/add/operator/map';");

        if (!imports.isEmpty()) {
            for (ImportDefinition anImport : imports)
                out.writeLine("import { " + join(anImport.getTypes(), ", ") + " } from '" + anImport.getModuleName() + "';");

            out.writeLine();
        }
    }

    private void writeEndpoints(Collection<EndpointGroup> endpointGroups) {
        for (EndpointGroup endpointGroup : endpointGroups) {
            out.writeLine("@Injectable()");
            out.writeBlock("export class " + endpointGroup.getName() + "Endpoint", () -> {

                out.writeBlock("constructor(private context: ApinaEndpointContext)", () -> {
                });

                for (Endpoint endpoint : endpointGroup.getEndpoints()) {
                    writeEndpoint(endpoint);
                    out.writeLine().writeLine();
                }
            });
        }
    }

    private void writeEndpoint(Endpoint endpoint) {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock(() ->
                out.write("return this.context.request(").writeValue(createConfig(endpoint)).writeLine(");"));
    }

    private void writeModule() {
        out.writeLine();

        out.writeLine("@NgModule({");
        out.writeLine("    imports: [HttpModule],");
        out.writeLine("    providers: [");

        for (EndpointGroup endpointGroup : api.getEndpointGroups())
            out.writeLine("        " + endpointGroup.getName() + "Endpoint,");

        out.writeLine("        ApinaEndpointContext,");
        out.writeLine("        ApinaConfig");

        out.writeLine("    ]");
        out.writeLine("})");
        out.writeLine("export class ApinaModule {}");
    }
}
