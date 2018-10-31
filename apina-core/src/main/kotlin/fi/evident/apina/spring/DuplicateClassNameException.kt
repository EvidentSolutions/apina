package fi.evident.apina.spring

class DuplicateClassNameException(name1: String, name2: String) :
    RuntimeException("Translating classes with same simple names is not supported. Conflicting classes: $name1 vs. $name2")
