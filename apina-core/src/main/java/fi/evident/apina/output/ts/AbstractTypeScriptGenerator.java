package fi.evident.apina.output.ts;

import fi.evident.apina.model.*;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiPrimitiveType;
import fi.evident.apina.model.type.ApiType;
import fi.evident.apina.model.type.ApiTypeName;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

abstract class AbstractTypeScriptGenerator {

    protected final CodeWriter out = new CodeWriter();
    protected ApiDefinition api;
    protected final TranslationSettings settings;
    private final String typePrefix;
    private final String supportPrefix;
    private final String resultFunctor;

    public AbstractTypeScriptGenerator(ApiDefinition api, TranslationSettings settings, String typePrefix, String supportPrefix, String resultFunctor) {
        this.api = requireNonNull(api);
        this.settings = requireNonNull(settings);
        this.typePrefix = requireNonNull(typePrefix);
        this.supportPrefix = requireNonNull(supportPrefix);
        this.resultFunctor = requireNonNull(resultFunctor);
    }

    protected void writeTypes() {

        out.writeExportedInterface("Dictionary<V>", () ->
                out.writeLine("[key: string]: V;"));

        for (ApiTypeName unknownType : api.getAllBlackBoxClasses()) {
            out.writeLine(format("export type %s = {};", unknownType));
        }

        out.writeLine();

        for (EnumDefinition enumDefinition : api.getEnumDefinitions()) {
            out.writeLine(format("export enum %s { %s }", enumDefinition.getType(), String.join(", ", enumDefinition.getConstants())));
        }

        out.writeLine();

        for (ClassDefinition classDefinition : api.getClassDefinitions()) {
            out.writeExportedClass(classDefinition.getType().toString(), () -> {
                for (PropertyDefinition property : classDefinition.getProperties())
                    out.writeLine(property.getName() + ": " + property.getType() + ";");
            });
        }

        writeSerializerDefinitions();
    }

    private String qualifiedTypeName(ApiType type) {
        if (type instanceof ApiPrimitiveType) {
            return type.typeRepresentation();
        } else if (type instanceof ApiArrayType) {
            ApiArrayType arrayType = (ApiArrayType) type;
            return qualifiedTypeName(arrayType.getElementType()) + "[]";
        } else if (settings.isImported(new ApiTypeName(type.typeRepresentation()))) {
            return type.typeRepresentation();
        } else {
            return typePrefix + type.typeRepresentation();
        }
    }

    private void writeSerializerDefinitions() {
        out.write("export function registerDefaultSerializers(config: " + supportPrefix + "ApinaConfig) ").writeBlock(() -> {
            for (ApiTypeName unknownType : api.getAllBlackBoxClasses()) {
                out.write("config.registerIdentitySerializer(").writeValue(unknownType.toString()).writeLine(");");
            }
            out.writeLine();

            for (EnumDefinition enumDefinition : api.getEnumDefinitions()) {
                String enumName = enumDefinition.getType().toString();
                out.write("config.registerEnumSerializer(").writeValue(enumName).write(", ");
                out.write(enumName).writeLine(");");
            }
            out.writeLine();

            for (ClassDefinition classDefinition : api.getClassDefinitions()) {
                Map<String, String> defs = new LinkedHashMap<>();

                for (PropertyDefinition property : classDefinition.getProperties())
                    defs.put(property.getName(), typeDescriptor(property.getType()));

                out.write("config.registerClassSerializer(").writeValue(classDefinition.getType().toString()).write(", ");
                out.writeValue(defs).writeLine(");");
                out.writeLine();
            }
        });

        out.writeLine().writeLine();
    }

    protected static Map<String, Object> createConfig(Endpoint endpoint) {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("uriTemplate", endpoint.getUriTemplate().toString());
        config.put("method", endpoint.getMethod().toString());

        List<EndpointPathVariableParameter> pathVariables = endpoint.getPathVariables();
        if (!pathVariables.isEmpty())
            config.put("pathVariables", createPathVariablesMap(pathVariables));

        List<EndpointRequestParamParameter> requestParameters = endpoint.getRequestParameters();
        if (!requestParameters.isEmpty())
            config.put("requestParams", createRequestParamMap(requestParameters));

        endpoint.getRequestBody().ifPresent(body -> config.put("requestBody", serialize(body.getName(), body.getType().unwrapNullable())));
        endpoint.getResponseBody().ifPresent(body -> config.put("responseType", typeDescriptor(body)));

        return config;
    }

    protected String endpointSignature(Endpoint endpoint) {
        String name = endpoint.getName();
        String parameters = parameterListCode(endpoint.getParameters());
        String resultType = endpoint.getResponseBody().map(this::qualifiedTypeName).orElse("void");

        return format("%s(%s): %s<%s>", name, parameters, resultFunctor, resultType);
    }

    private String parameterListCode(List<EndpointParameter> parameters) {
        return parameters.stream()
                .map(p -> p.getName() + ": " + qualifiedTypeName(p.getType()))
                .collect(joining(", "));
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

    private static String typeDescriptor(ApiType type) {
        // Use ApiType's native representation as type descriptor.
        // This method encapsulates the call to make it meaningful in this context.
        return type.unwrapNullable().typeRepresentation();
    }
}
