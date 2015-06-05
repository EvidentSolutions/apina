package fi.evident.apina.tsang;

import fi.evident.apina.model.*;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiClassType;
import fi.evident.apina.model.type.ApiPrimitiveType;
import fi.evident.apina.model.type.ApiType;

import java.io.IOException;
import java.util.*;

import static fi.evident.apina.utils.ResourceUtils.readResourceAsString;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Generates TypeScript code for client side.
 */
public final class AngularTypeScriptWriter {

    private final CodeWriter out = new CodeWriter();
    private final ApiDefinition api;
    private final List<String> startDeclarations = new ArrayList<>();

    public AngularTypeScriptWriter(ApiDefinition api) {
        this.api = requireNonNull(api);
    }

    public void addStartDeclaration(String declaration) {
        startDeclarations.add(requireNonNull(declaration));
    }

    public void writeApi() throws IOException {
        writeStartDeclarations();
        writeRuntime();
        writeTypes();

        writeEndpointInterfaces(api.getEndpointGroups());

        out.write("export function createEndpointGroups(context: Support.Context): Endpoints.IEndpointGroups ").writeBlock(() -> {
            writeSerializerDefinitions();
            writeEndpoints(api.getEndpointGroups());
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

    private void writeStartDeclarations() {
        if (!startDeclarations.isEmpty()) {
            for (String declaration : startDeclarations)
                out.writeLine(declaration);

            out.writeLine();
        }
    }

    private void writeEndpointInterfaces(Collection<EndpointGroup> endpointGroups) {
        out.writeExportedModule("Endpoints", () -> {
            for (EndpointGroup endpointGroup : endpointGroups) {
                out.writeExportedInterface("I" + endpointGroup.getName(), () -> {
                    for (Endpoint endpoint : endpointGroup.getEndpoints())
                        out.writeLine(endpointSignature(endpoint));
                });
            }

            out.writeExportedInterface("IEndpointGroups", () -> {
                for (EndpointGroup endpointGroup : endpointGroups)
                    out.writeLine(endpointGroup.getName() + ": I" + endpointGroup.getName());
            });
        });
    }

    private void writeEndpoints(Collection<EndpointGroup> endpointGroups) {
        out.writeLine();

        out.write("return ").writeBlock(() -> {

            for (Iterator<EndpointGroup> groupIterator = endpointGroups.iterator(); groupIterator.hasNext(); ) {
                EndpointGroup endpointGroup = groupIterator.next();

                out.write(endpointGroup.getName() + ": ").writeBlock(() -> {
                    for (Iterator<Endpoint> it = endpointGroup.getEndpoints().iterator(); it.hasNext(); ) {
                        writeEndpoint(it.next());
                        if (it.hasNext())
                            out.writeLine(", ");
                        out.writeLine();
                    }
                });

                if (groupIterator.hasNext())
                    out.writeLine(", ");

                out.writeLine();
            }
        });

        out.writeLine();
    }

    private void writeEndpoint(Endpoint endpoint) {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock(() ->
                out.write("return context.request(").writeValue(createConfig(endpoint)).writeLine(");"));
    }

    private static String endpointSignature(Endpoint endpoint) {
        String name = endpoint.getName();
        String parameters = parameterListCode(endpoint.getParameters());
        String resultType = endpoint.getResponseBody().map(AngularTypeScriptWriter::qualifiedTypeName).orElse("void");

        return format("%s(%s): Support.IPromise<%s>", name, parameters, resultType);
    }

    private static String parameterListCode(List<EndpointParameter> parameters) {
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
        return new RawCode("context.serialize(" + variable + ", '" + typeDescriptor(type) + "')");
    }

    private static String qualifiedTypeName(ApiType type) {
        if (type instanceof ApiPrimitiveType) {
            return type.toString();
        } else if (type instanceof ApiArrayType) {
            ApiArrayType arrayType = (ApiArrayType) type;
            return qualifiedTypeName(arrayType.getElementType()) + "[]";
        } else {
            return "Types." + type;
        }
    }

    private static String typeDescriptor(ApiType type) {
        // Use ApiType's native representation - i.e. ApiType.toString() - as type descriptor.
        // This method encapsulates the call to make it meaningful in this context.
        return type.toString();
    }

    private void writeTypes() {
        out.writeExportedModule("Types", () -> {
            for (ApiClassType unknownType : api.getUnknownTypeReferences()) {
                out.writeLine(format("export type %s = {};", unknownType.getName()));
            }

            out.writeLine();

            for (ClassDefinition classDefinition : api.getClassDefinitions()) {
                out.writeExportedInterface(classDefinition.getType().getName(), () -> {
                    for (PropertyDefinition property : classDefinition.getProperties())
                        out.writeLine(property.getName() + ": " + property.getType());
                });
            }
        });
    }

    private void writeSerializerDefinitions() {
        for (ApiClassType unknownType : api.getUnknownTypeReferences()) {
            out.write("context.registerIdentitySerializer(").writeValue(unknownType.getName()).writeLine(");");
        }
        out.writeLine();

        for (ClassDefinition classDefinition : api.getClassDefinitions()) {
            Map<String, String> defs = new LinkedHashMap<>();

            for (PropertyDefinition property : classDefinition.getProperties())
                defs.put(property.getName(), typeDescriptor(property.getType()));

            out.write("context.registerClassSerializer(").writeValue(classDefinition.getType().toString()).write(", ");
            out.writeValue(defs).writeLine(");");
            out.writeLine();
        }
    }
}
