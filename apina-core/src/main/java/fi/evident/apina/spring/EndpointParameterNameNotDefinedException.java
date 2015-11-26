package fi.evident.apina.spring;

public final class EndpointParameterNameNotDefinedException extends RuntimeException {

    public EndpointParameterNameNotDefinedException() {
        super("Could not resolve endpoint parameter name from class-file. Add '-parameters' argument for javac.");
    }
}
