package fi.evident.apina.output.ts;

import fi.evident.apina.model.*;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.settings.ImportDefinition;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiPrimitiveType;
import fi.evident.apina.model.type.ApiType;

import java.io.IOException;
import java.util.*;

import static fi.evident.apina.utils.CollectionUtils.map;
import static fi.evident.apina.utils.ResourceUtils.readResourceAsString;
import static fi.evident.apina.utils.StringUtils.uncapitalize;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Generates TypeScript code for client side.
 */
public final class TypeScriptGenerator {

    private final CodeWriter out = new CodeWriter();
    private final ApiDefinition api;
    private final TranslationSettings settings;

    public TypeScriptGenerator(ApiDefinition api, TranslationSettings settings) {
        this.api = requireNonNull(api);
        this.settings = requireNonNull(settings);
    }

    public void writeApi() throws IOException {
        writeImports();
        writeTypes();
        writeEndpoints(api.getEndpointGroups());
        writeRuntime();
    }

    private void writeCreateEndpointGroups() {
        out.write("export function createEndpointGroups(context: Support.EndpointContext): Endpoints.IEndpointGroups ").writeBlock(() -> {
            out.write("return ").writeBlock(() -> {
                for (Iterator<EndpointGroup> it = api.getEndpointGroups().iterator(); it.hasNext(); ) {
                    EndpointGroup endpointGroup = it.next();

                    out.write(String.format("%s: new Endpoints.%s(context)", uncapitalize(endpointGroup.getName()), endpointGroup.getName()));

                    if (it.hasNext())
                        out.write(", ");

                    out.writeLine();
                }
            });

            out.writeLine();
        });

        out.writeLine();
        out.writeLine();
    }

    public String getOutput() {
        return out.getOutput();
    }

    private void writeRuntime() throws IOException {
        out.write(readResourceAsString("typescript/runtime.ts", UTF_8));
        out.writeLine();
    }

    private void writeImports() {
        Collection<ImportDefinition> imports = settings.getImports();

        if (!imports.isEmpty()) {
            for (ImportDefinition anImport : imports)
                out.writeLine("import { " + String.join(", ", anImport.getTypes()) + " } from '" + anImport.getModuleName() + "';");

            out.writeLine();
        }
    }

    private void writeEndpoints(Collection<EndpointGroup> endpointGroups) {
        out.writeExportedModule("Endpoints", () -> {

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

    private String endpointSignature(Endpoint endpoint) {
        String name = endpoint.getName();
        String parameters = parameterListCode(endpoint.getParameters());
        String resultType = endpoint.getResponseBody().map(this::qualifiedTypeName).orElse("void");

        return format("%s(%s): Support.IPromise<%s>", name, parameters, resultType);
    }

    private String parameterListCode(List<EndpointParameter> parameters) {
        return parameters.stream()
                .map(p -> p.getName() + ": " + qualifiedTypeName(p.getType()))
                .collect(joining(", "));
    }

    private static Map<String, Object> createConfig(Endpoint endpoint) {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("uriTemplate", endpoint.getUriTemplate().toString());
        config.put("method", endpoint.getMethod().toString());

        List<EndpointPathVariableParameter> pathVariables = endpoint.getPathVariables();
        if (!pathVariables.isEmpty())
            config.put("pathVariables", createPathVariablesMap(pathVariables));

        List<EndpointRequestParamParameter> requestParameters = endpoint.getRequestParameters();
        if (!requestParameters.isEmpty())
            config.put("requestParams", createRequestParamMap(requestParameters));

        endpoint.getRequestBody().ifPresent(body -> config.put("requestBody", serialize(body.getName(), body.getType())));
        endpoint.getResponseBody().ifPresent(body -> config.put("responseType", typeDescriptor(body)));

        return config;
    }

    private static Map<String, Object> createRequestParamMap(Collection<EndpointRequestParamParameter> parameters) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (EndpointRequestParamParameter param : parameters)
            result.put(param.getQueryParameter(), serialize(param.getName(), param.getType()));

        return result;
    }

    private static Map<String, Object> createPathVariablesMap(List<EndpointPathVariableParameter> pathVariables) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (EndpointPathVariableParameter param : pathVariables)
            result.put(param.getPathVariable(), serialize(param.getName(), param.getType()));

        return result;
    }

    /**
     * Returns TypeScript code to serialize {@code variable} of given {@code type}
     * to transfer representation.
     */
    private static RawCode serialize(String variable, ApiType type) {
        return new RawCode("this.context.serialize(" + variable + ", '" + typeDescriptor(type) + "')");
    }

    private String qualifiedTypeName(ApiType type) {
        if (type instanceof ApiPrimitiveType) {
            return type.typeRepresentation();
        } else if (type instanceof ApiArrayType) {
            ApiArrayType arrayType = (ApiArrayType) type;
            return qualifiedTypeName(arrayType.getElementType()) + "[]";
        } else if (settings.isImported(type.typeRepresentation())) {
            return type.typeRepresentation();
        } else {
            return "Types." + type.typeRepresentation();
        }
    }

    private static String typeDescriptor(ApiType type) {
        // Use ApiType's native representation as type descriptor.
        // This method encapsulates the call to make it meaningful in this context.
        return type.typeRepresentation();
    }

    private void writeTypes() {
        out.writeExportedModule("Types", () -> {
            out.writeExportedInterface("IDictionary<V>", () ->
                    out.writeLine("[key: string]: V"));

            for (ApiType unknownType : api.getAllBlackBoxClasses()) {
                out.writeLine(format("export type %s = {};", unknownType.typeRepresentation()));
            }

            out.writeLine();

            for (ClassDefinition classDefinition : api.getClassDefinitions()) {
                out.writeExportedClass(classDefinition.getType().getName(), () -> {
                    for (PropertyDefinition property : classDefinition.getProperties())
                        out.writeLine(property.getName() + ": " + property.getType() + ";");
                });
            }

            writeSerializerDefinitions();
        });
    }

    private void writeSerializerDefinitions() {
        out.write("export function registerDefaultSerializers(config: Support.ApinaConfig) ").writeBlock(() -> {
            for (ApiType unknownType : api.getAllBlackBoxClasses()) {
                out.write("config.registerIdentitySerializer(").writeValue(unknownType.typeRepresentation()).writeLine(");");
            }
            out.writeLine();

            for (ClassDefinition classDefinition : api.getClassDefinitions()) {
                Map<String, String> defs = new LinkedHashMap<>();

                for (PropertyDefinition property : classDefinition.getProperties())
                    defs.put(property.getName(), typeDescriptor(property.getType()));

                out.write("config.registerClassSerializer(").writeValue(classDefinition.getType().getName()).write(", ");
                out.writeValue(defs).writeLine(");");
                out.writeLine();
            }
        });

        out.writeLine().writeLine();
    }
}
