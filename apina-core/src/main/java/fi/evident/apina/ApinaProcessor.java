package fi.evident.apina;

import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.ApiTypeName;
import fi.evident.apina.output.ts.TypeScriptAngular1Generator;
import fi.evident.apina.output.ts.TypeScriptAngular2Generator;
import fi.evident.apina.spring.SpringModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public final class ApinaProcessor {

    public final TranslationSettings settings = new TranslationSettings();
    private final Classpath classpath;
    private static final Logger log = LoggerFactory.getLogger(ApinaProcessor.class);

    public ApinaProcessor(Classpath classpath) {
        this.classpath = classpath;
    }

    public String process() throws IOException {
        ApiDefinition api = SpringModelReader.readApiDefinition(classpath, settings);

        log.debug("Loaded {} endpoint groups with {} endpoints.", api.getEndpointGroupCount(), api.getEndpointCount());
        log.trace("Loaded endpoint groups: {}", api.getEndpointGroups());

        if (api.getEndpointCount() == 0) {
            log.warn("Apina could not find any endpoints to process");
        }

        log.debug("Loaded {} class definitions", api.getClassDefinitionCount());
        log.trace("Loaded class definitions: {}", api.getClassDefinitions());

        log.debug("Loaded {} enum definitions", api.getEnumDefinitionCount());
        log.trace("Loaded enum definitions: {}", api.getEnumDefinitions());

        Set<ApiTypeName> unknownTypes = api.getUnknownTypeReferences();
        if (!unknownTypes.isEmpty()) {
            log.warn("Writing {} unknown class definitions as black boxes: {}", unknownTypes.size(), unknownTypes);
        }

        if (settings.platform.equals("angular1")) {
            TypeScriptAngular1Generator writer = new TypeScriptAngular1Generator(api, settings);
            writer.writeApi();
            return writer.getOutput();

        } else {
            TypeScriptAngular2Generator writer = new TypeScriptAngular2Generator(api, settings);
            writer.writeApi();
            return writer.getOutput();
        }
    }
}
