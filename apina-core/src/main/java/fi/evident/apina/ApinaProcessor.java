package fi.evident.apina;

import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.ApiTypeName;
import fi.evident.apina.output.ts.TypeScriptGenerator;
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
            log.warn("Did not find any endpoints");
        }

        log.debug("Loaded {} class definitions", api.getClassDefinitionCount());
        log.trace("Loaded class definitions: {}", api.getClassDefinitions());

        Set<ApiTypeName> unknownTypes = api.getUnknownTypeReferences();
        if (!unknownTypes.isEmpty()) {
            log.warn("Writing {} unknown class definitions as black boxes: {}", unknownTypes.size(), unknownTypes);
        }

        TypeScriptGenerator writer = new TypeScriptGenerator(api, settings);
        writer.writeApi();

        return writer.getOutput();
    }
}
