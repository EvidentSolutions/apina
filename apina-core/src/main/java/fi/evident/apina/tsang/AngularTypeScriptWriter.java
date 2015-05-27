package fi.evident.apina.tsang;

import fi.evident.apina.model.*;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.type.ApiType;
import org.jetbrains.annotations.NotNull;

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

    private final CodeWriter out;
    private final ApiDefinition api;
    private final List<String> startDeclarations = new ArrayList<>();

    public AngularTypeScriptWriter(ApiDefinition api, Appendable out) {
        this.api = requireNonNull(api);
        this.out = new CodeWriter(out);
    }

    public void addStartDeclaration(String declaration) {
        startDeclarations.add(requireNonNull(declaration));
    }

    public void writeApi() throws IOException {
        writeStartDeclarations();
        writeRuntime();
        writeClassDefinitions(api.getClassDefinitions());

        writeEndpointInterfaces(api.getEndpointGroups());

        out.write("export function createEndpointGroups(context: Support.Context): Endpoints.IEndpointGroups ").writeBlock(() -> {
            writeSerializerDefinitions(api.getClassDefinitions());
            writeEndpoints(api.getEndpointGroups());
        });

        out.writeLine();
        out.writeLine();
    }

    private void writeRuntime() throws IOException {
        out.write(readResourceAsString("typescript/runtime.ts", UTF_8));
        out.writeLine();
    }

    private void writeStartDeclarations() throws IOException {
        if (!startDeclarations.isEmpty()) {
            for (String declaration : startDeclarations)
                out.writeLine(declaration);

            out.writeLine();
        }
    }

    private void writeEndpointInterfaces(Collection<EndpointGroup> endpointGroups) throws IOException {
        out.write("export module Endpoints ").writeBlock(() -> {
            for (EndpointGroup endpointGroup : endpointGroups) {
                out.write("export interface I" + endpointGroup.getName() + " ").writeBlock(() -> {
                    for (Endpoint endpoint : endpointGroup.getEndpoints())
                        out.writeLine(endpointSignature(endpoint));


                });

                out.writeLine().writeLine();
            }

            out.write("export interface IEndpointGroups ").writeBlock(() -> {
                for (EndpointGroup endpointGroup : endpointGroups)
                    out.writeLine(endpointGroup.getName() + ": I" + endpointGroup.getName());
            });
            out.writeLine();
        });

        out.writeLine().writeLine();
    }

    private void writeEndpoints(Collection<EndpointGroup> endpointGroups) throws IOException {
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
    }

    private void writeEndpoint(Endpoint endpoint) throws IOException {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock(() ->
                out.write("return context.request(").writeValue(createConfig(endpoint)).writeLine(");"));
    }

    private static String endpointSignature(Endpoint endpoint) {
        String name = endpoint.getName();
        String parameters = parameterListCode(endpoint.getParameters());
        String resultType = endpoint.getResponseBody().map(ApiType::toString).orElse("void");

        return format("%s(%s): Support.IPromise<%s>", name, parameters, resultType);
    }

    private static String parameterListCode(List<EndpointParameter> parameters) {
        return parameters.stream()
                .map(p -> p.getName() + ": " + p.getType())
                .collect(joining(", "));
    }

    @NotNull
    private static Map<String, Object> createConfig(Endpoint endpoint) {
        Map<String,Object> config = new LinkedHashMap<>();

        config.put("uriTemplate", endpoint.getUriTemplate().toString());
        config.put("method", endpoint.getMethod().toString());

        List<EndpointPathVariableParameter> pathVariables = endpoint.getPathVariables();
        if (!pathVariables.isEmpty())
            config.put("pathVariables", createPathVariablesMap(pathVariables));

        List<EndpointRequestParamParameter> requestParameters = endpoint.getRequestParameters();
        if (!requestParameters.isEmpty())
            config.put("requestParams", createRequestParamMap(requestParameters));

        endpoint.getRequestBody().ifPresent(body -> config.put("requestBody", serialize(body.getName(), body.getType())));
        endpoint.getResponseBody().ifPresent(body -> config.put("responseType", createTypeDescriptor(body)));

        return config;
    }

    private static Map<String,Object> createRequestParamMap(Collection<EndpointRequestParamParameter> parameters) {
        Map<String,Object> result = new LinkedHashMap<>();

        for (EndpointRequestParamParameter param : parameters)
            result.put(param.getQueryParameter(), serialize(param.getName(), param.getType()));

        return result;
    }

    private static Map<String,Object> createPathVariablesMap(List<EndpointPathVariableParameter> pathVariables) {
        Map<String,Object> result = new LinkedHashMap<>();

        for (EndpointPathVariableParameter param : pathVariables)
            result.put(param.getPathVariable(), serialize(param.getName(), param.getType()));

        return result;
    }

    /**
     * Returns TypeScript code to serialize {@code variable} of given {@code type}
     * to transfer representation.
     */
    private static RawCode serialize(String variable, ApiType type) {
        return new RawCode("context.serialize(" + variable + ", '" + createTypeDescriptor(type) + "')");
    }

    private static String createTypeDescriptor(ApiType type) {
        // TODO: write proper type descriptors, e.g. handle arrays
        return type.toString();
    }

    private void writeClassDefinitions(Collection<ClassDefinition> classDefinitions) throws IOException {
        for (ClassDefinition classDefinition : classDefinitions) {
            out.write("export interface " + classDefinition.getType() + " ").writeBlock(() -> {
                for (PropertyDefinition property : classDefinition.getProperties())
                    out.writeLine(property.getName() + ": " + property.getType());
            });

            out.writeLine().writeLine();
        }
    }

    private void writeSerializerDefinitions(Collection<ClassDefinition> classDefinitions) throws IOException {
        for (ClassDefinition classDefinition : classDefinitions) {
            Map<String, String> defs = new LinkedHashMap<>();

            for (PropertyDefinition property : classDefinition.getProperties())
                defs.put(property.getName(), createTypeDescriptor(property.getType()));

            out.write("context.registerClassSerializer(").writeValue(classDefinition.getType().toString()).write(", ");
            out.writeValue(defs).writeLine(");");
            out.writeLine();
        }
    }
}
