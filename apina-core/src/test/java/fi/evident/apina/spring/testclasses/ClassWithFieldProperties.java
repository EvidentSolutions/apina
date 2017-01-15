package fi.evident.apina.spring.testclasses;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unused", "WeakerAccess"})
public final class ClassWithFieldProperties {

    public int intField;
    public Integer integerField;
    public String stringField;
    public boolean booleanField;
    public Boolean booleanNonPrimitiveField;
    public int[] intArrayField;
    public Collection<String> stringCollectionField;
    public Collection rawCollectionField;
    public Map<String,Integer> stringIntegerMapField;
    public Map <?,?> wildcardMapField;
    public Map rawMapField;
    public Object objectField;
}
