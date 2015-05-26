package fi.evident.apina.tsang;

import fi.evident.apina.model.*;

import java.util.Collection;

public final class AngularTypeScriptWriter {

    public static void printModel(ApiDefinition api) {
        printEndpoints(api.getEndpointGroups());
        System.out.println();
        printClassDefinitions(api.getClassDefinitions());
    }

    private static void printEndpoints(Collection<EndpointGroup> endpointGroups) {
        for (EndpointGroup endpointGroup : endpointGroups) {
            System.out.println(endpointGroup.getName());
            for (Endpoint endpoint : endpointGroup.getEndpoints()) {
                System.out.println("    " + endpoint);
            }
        }
    }

    private static void printClassDefinitions(Collection<ClassDefinition> classDefinitions) {
        for (ClassDefinition classDefinition : classDefinitions) {
            System.out.println("export interface " + classDefinition.getType() + " {");
            for (PropertyDefinition property : classDefinition.getProperties())
                System.out.println("    " + property.getName() + ": " + property.getType());
            System.out.println("}");
            System.out.println();
        }
    }
}
