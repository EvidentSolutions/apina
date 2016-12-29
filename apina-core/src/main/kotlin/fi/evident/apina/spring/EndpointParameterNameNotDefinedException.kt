package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaMethod

class EndpointParameterNameNotDefinedException(method: JavaMethod) : RuntimeException("Could not resolve endpoint parameter name of method '" + method.name + "' from class " + method.owningClass.name + ". Add '-parameters' argument for javac.")
