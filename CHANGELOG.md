## Unreleased

- Find subclasses of sealed Kotlin classes without using `@JsonSubTypes`.

## 0.13.3 (2020-04-14)

- Consult the field corresponding to a getter when considering ignore-annotations

## 0.13.2 (2020-04-14)

- Ignore properties marked with `java.beans.Transient` or `org.springframework.data.annotation.Transient`
- Ignore transient fields, unless they have getters

## 0.13.1 (2020-04-09)

- Fix race condition regarding the creation of shadowJar ([#56](https://github.com/EvidentSolutions/apina/pull/56))
- Fix empty response as null serialization ([#58](https://github.com/EvidentSolutions/apina/pull/58))

## 0.13.0 (2020-01-18)

### Breaking changes

- Write generated data-types as interfaces instead of classes.

## 0.12.4 (2019-09-25)

- Support Jackson's @JsonUnwrapped ([#16](https://github.com/EvidentSolutions/apina/issues/16))

## 0.12.3 (2019-07-21)

- Exclude injected parameters from generated model. ([#54](https://github.com/EvidentSolutions/apina/pull/54))

## 0.12.2 (2019-06-05)

- Initial support for targeting Swift. Note that this is still incubating and the generated code is might to have
  breaking changes in minor releases.

## 0.12.1 (2019-03-27)

- Support specifying platform and controller patterns when using command line runner
- Relax parsing HTTP methods: if multiple methods are specified, use first instead of failing.
- Allow translating JSON objects to interfaces instead of classes

## 0.12.0 (2019-03-05)

- Add [manual](https://apina.evident.fi/)
- Support Jackson subtypes as discriminated unions ([#50](https://github.com/EvidentSolutions/apina/pull/50))
- Improve translation of @JsonValue types ([#41](https://github.com/EvidentSolutions/apina/pull/41))
- Change `ANGULAR2` platform name to `ANGULAR`.
- Support other nullability annotations in addition to JetBrains' annotations.

## 0.11.0 (2019-02-08)

- Add new `es6` platform target, allowing using Apina without Angular. 

## 0.10.14 (2019-01-29)

- Support resolving base path and generic parameters from subclass when controller methods
  are defined in superclass. 

## 0.10.13 (2019-01-29)

- Search controller methods for superclasses of controllers 

## 0.10.12 (2019-01-25)

- Support Java 11

## 0.10.11 (2019-01-11)

- Fix classpath scanning on Windows.

## 0.10.10 (2019-01-06)

- Make order of written elements deterministic to help with build caching.

## 0.10.9 (2019-01-05)

- Speed up class loading by trying to avoid parsing unnecessary classfiles

## 0.10.8 (2018-11-01)

- Generate Typescript classes for types in parameterized classes ([#44](https://github.com/EvidentSolutions/apina/issues/44), [#47](https://github.com/EvidentSolutions/apina/issues/47))

## 0.10.7 (2018-09-03)

- Import `Observable` from `rxjs` instead of `rxjs/Observable`

## 0.10.6 (2018-03-05)

- Improve up-to-date detection
- Support caching

## 0.10.5 (2018-03-05)

- Support parsing module-info files

## 0.10.4 (2018-03-05)

- Update asm to 6.0, allowing parsing of Java 9 bytecode.

## 0.10.3 (2018-02-13)

- Allow generated code to compile with `noImplicitAny`.

## 0.10.2 (2018-02-13)

- Fix invalid pom.

## 0.10.1 (2018-02-13)

- Omit null values from request params, again.

## 0.10.0 (2018-02-08)

### Breaking changes

- Usage `HttpClient` instead of `Http` for making request, thus requiring Angular 4.3. 

### Improvements

- Add `removedUrlPrefix` parameter that can be used to strip a prefix from the generated URLs

## 0.9.0 (2018-02-08)

### Fixes

- Omit null values from request params ([#34](https://github.com/EvidentSolutions/apina/issues/34))
- Simplify writing custom ApinaEndpointContexts

### Breaking changes

- Change handling of enumerations. New `DEFAULT`-mode is to serialize enums as string enums.
  Old `ENUM`-mode is now `INT_ENUM` and `STRING` is `STRING_UNION`. ([#37](https://github.com/EvidentSolutions/apina/issues/37))
- Remove support for Angular 1 ([#36](https://github.com/EvidentSolutions/apina/issues/36))

## 0.8.4 (2019-01-25)

- Support Java 11 (thanks to Markus Kouko).

## 0.8.3 (2018-12-13)

### Fixes

- Support for parsing Java 9 module-info files (thanks to Markus Kouko).

## 0.8.2 (2017-09-08)

### Fixes

- Fix qualified type name generation for nullable types.

## 0.8.1 (2017-09-08)

### Fixes

- Fix typing of Promise to be compatible with latest AngularJS typings.

## 0.8.0 (2017-04-26)

### Improvements

- Allow specifying which endpoints to include in generated API.

### Breaking changes

- `assemble` no longer depends on Apina by default, you need to register wanted dependency yourself

## 0.7.6 (2017-04-22)

### Fixes

- Fix response mapping broken in 3ca5cd9.

## 0.7.5 (2017-04-22)

### Improvements

- Support `--strictNullChecks` in generated Angular 2 code.

## 0.7.4 (2017-04-22)

### Fixes

- Fix imports in generated Angular 2 code  

## 0.7.3 (2017-04-22)

### Fixes

- Fix passing request parameters in Angular 2 backend. 

## 0.7.2 (2017-04-18)

### Fixes

- Fix parsing generic signatures with shadowing ([#33](https://github.com/EvidentSolutions/apina/issues/33))

### Other changes

- Upgraded to Kotlin 1.1.1

## 0.7.1 (2017-01-20)

### Improvements

- Unwrap logical return type from a possible wrapper before analyzing it. That is, interpret 
  methods returning `ResponseEntity<T>`, `HttpEntity<T>` or `Callable<T>` as methods returning just `T`.

### Fixes

- Improvements to handling some obscure generic types.

## 0.7.0 (2017-01-16)

### Improvements

- Support for using string union types (`type MyEnum = "FOO" | "BAR" | "BAZ"`) instead of `enum`-types
  for representing enums in TypeScript.

### Other changes

- Built against Gradle 3.3

## 0.6.5 (2017-01-10)

### Fixes

- Fix issue with loading resources

## 0.6.4 (2017-01-10)

### Improvements

- Support reading nested libraries from JAR/WAR-archives.
- Support serialization of maps with wildcard parameters.
- Print warning instead of failing if there are invalid classpath entries.

## 0.6.3 (2017-01-02)

### Fixes

- New alias resolution broke attributes without aliases.

## 0.6.2 (2017-01-02)

### Improvements

- More complete alias resolution for Spring.

## 0.6.1 (2017-01-01)

### Improvements

- Support @AliasFor without explicit attribute name. Use the name of the original attribute 
  if @AliasFor does not specify a name.

## 0.6.0 (2017-01-01)

### Improvements

- Add support for Spring's meta-annotations and `@AliasFor` when resolving annotations. 
  ([#30](https://github.com/EvidentSolutions/apina/issues/30))

### Other changes

- Converted most of the codebase to Kotlin. 

## 0.5.3 (2016-12-21)

### Improvements

- Create nullable types in TypeScript output if `@Nullable` or `Optional<T>` is use in Jackson classes.

## 0.5.2 (2016-12-19)

### Breaking changes

- Remove automatic `.share()` calls from returned `Observable`s in Angular 2 backend. Instead, return
  the `Observable` exactly the same way that Angular's `Http` does, letting caller decide appropriate
  strategy for sharing (if needed).

## 0.5.1 (2016-11-15)

### Fixes

- Provide default value for platform when using Gradle.

## 0.5.0 (2016-11-15)

### Improvements

- Add support for Angular 2.

### Breaking changes

- Code is now generated for Angular 2 by default, you need to specify `framework = 'angular1'` 
  to build for AngularJS.

## 0.4.3 (2016-04-21)

### Improvements

- Support reading parameter names from debug information.

## 0.4.2 (2016-01-07)

### Improvements

- TSLint warnings are disabled for the generated TypeScript file so that it can be included in
  a project regardless of TSLint configuration settings.

## 0.4.1 (2015-11-27)

### New features

- Support type parameters in inheritance ([#27](https://github.com/EvidentSolutions/apina/issues/27))
- Support overriding ignores in subclasses ([#26](https://github.com/EvidentSolutions/apina/issues/26))

### Improvements

- Fail fast if endpoint parameter name can't be resolved ([#17](https://github.com/EvidentSolutions/apina/issues/17))
- Support @RequestParam(name=...) ([#28](https://github.com/EvidentSolutions/apina/issues/28))

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
