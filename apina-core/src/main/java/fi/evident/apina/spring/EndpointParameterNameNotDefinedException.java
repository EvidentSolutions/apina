package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaMethod;

public final class EndpointParameterNameNotDefinedException extends RuntimeException {

    public EndpointParameterNameNotDefinedException(JavaMethod method) {
        super("Could not resolve endpoint parameter name of method '" + method.getName() + "' from class " + method.getOwningClass().getName() + ". Add '-parameters' argument for javac.");
    }
}
