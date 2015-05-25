package fi.evident.apina.tsang;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.Endpoint;
import fi.evident.apina.model.EndpointGroup;

public final class AngularTypeScriptWriter {

    public static void printModel(ApiDefinition api) {
        for (EndpointGroup endpointGroup : api.getEndpointGroups()) {
            System.out.println(endpointGroup.getName());
            for (Endpoint endpoint : endpointGroup.getEndpoints()) {
                System.out.println("    " + endpoint);
            }
        }
    }
}
