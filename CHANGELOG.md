## x.y.z (yyyy-mm-dd)

### New features

- Support translation of optional types (`Optional<T>`, `OptionalInt`, `OptionalLong` and `OptionalDouble`).
  ([#15](https://github.com/EvidentSolutions/apina/issues/15))

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
