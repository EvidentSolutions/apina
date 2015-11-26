## x.y.z

### New features

- Support type parameters in inheritance ([#27](https://github.com/EvidentSolutions/apina/issues/27))

## 0.4.0 (2015-10-29)

### New features

- Translate Java enums to TypeScript enums and register serializers for them. ([#18](https://github.com/EvidentSolutions/apina/issues/18))

## 0.3.2 (2015-10-23)

### Bug fixes

- If class is found multiple times from classpath, ignore the redefinitions and just log them.

## 0.3.1 (2015-10-23)

### New features

- Support inherited fields when translating data types. Inheritance is not exposed in the
  generated API, but rather everything is flattened to single type.
  ([#23](https://github.com/EvidentSolutions/apina/issues/23))

### Bug fixes

- Fail fast if duplicate class names are detected. ([#19](https://github.com/EvidentSolutions/apina/issues/19))

## 0.3.0 (2015-08-19)

### New features

- Support translation of optional types (`Optional<T>`, `OptionalInt`, `OptionalLong` and `OptionalDouble`).
  ([#15](https://github.com/EvidentSolutions/apina/issues/15))
- Support configuring base URL for API calls. ([#13](https://github.com/EvidentSolutions/apina/issues/13))
- Use black-box translation for classes with `@JsonValue` ([#11](https://github.com/EvidentSolutions/apina/issues/11))
- Support importing classes to be used instead of generated classes.

## Breaking changes

- Change to Angular context: expose `apinaSerializationConfig` as `apinaConfig` instead.

## 0.2.2 (2015-06-15)

### Bug fixes

- Support parsing method descriptors with differing argument counts for 
  generic and non-generic signatures. These are not common, yet are present
  in some legacy class files.

## 0.2.1 (2015-06-10)

### Bug fixes

- Parse Spring path patterns with braces in regular expressions properly. (e.g. `/{id:[0-9a-zA-Z]{16}}`).
  ([#14](https://github.com/EvidentSolutions/apina/issues/14))

## 0.2.0 (2015-06-08)

### New features

- Write types as classes instead of interfaces. This means that they can be instantiated with a constructor
  to get an instance with all properties.
- Expose `Support.EndpointContext` as angular-service `apinaEndpointContext` and `Support.SerializationConfig` as
  `apinaSerializationConfig`. This allows us to override serializers.
  ([#3](https://github.com/EvidentSolutions/apina/issues/3))
- Expose `apina.endpoints` module that directly binds all endpoint groups to angular module so that we don't need 
  to inject `endpointGroups.` The keys are available as constants of form `Endpoints.Foo.KEY`.
  ([#7](https://github.com/EvidentSolutions/apina/issues/7))

### Breaking changes

- Generated code is simplified by translating endpoint groups directly to classes. The types are now named 
 `Endpoints.Foo` instead of `Endpoints.Foo`. Endpoint group properties of `Endpoints.IEndpointGroups` now begin
  with lowercase letter, e.g. `endpointGroups.Foo` is now `endpointGroups.foo`. 
