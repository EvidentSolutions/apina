# Apina

Apina creates client-side TypeScript code for either for Angular or Fetch API based on server-side APIs. Apina reads 
Spring Web MVC's `@RestController` annotated classes and their related Jackson classes and creates code for the data
model and for executing HTTP-requests.

Read [the manual](https://apina.evident.fi/) for details.

## Quick start using Gradle

Include something like the following in your web application project:

```kotlin
plugins {
    id("fi.evident.apina") version "0.19.0"
}

tasks.apina {
    // Set the name of the created TypeScript file. Default is 'build/apina/apina.ts'.
    target.set(project(":frontend").layout.projectDirectory.file("app/apina-api.ts"))
    
    // Specify types that should not be generated, but are implemented manually
    // and should be imported to the generated code. Keys are module paths, values 
    // are list of types imported from the module.
    imports.set(mapOf(
        "./my-time-module" to listOf("Instant", "LocalDate"),
        "./other-module" to listOf("Foo", "Bar")
    ))
    
    // How Java enums are translated to TypeScript enums? (Default mode is 'default'.)
    //  - 'default'      => enum MyEnum { FOO = "FOO", BAR = "BAR", BAZ = "BAZ" }
    //  - 'int_enum'     => enum MyEnum { FOO, BAR, BAZ }
    //  - 'string_union' => type MyEnum = "FOO" | "BAR" | "BAZ"
    enumMode.set(EnumMode.DEFAULT)
    
    // How nullables are translated to TypeScript interfaces? (Default mode is 'NULL'.)
    //  - 'NULL'      => name: Type | null
    //  - 'UNDEFINED' => name?: Type
    optionalTypeMode.set(OptionalTypeMode.NULL)
    
    // Which controllers to include when generating API? Defaults to everything.
    endpoints.set(listOf("""my\.package\.foo\..+"""))
    
    // If generated URLs would start with given prefix, removes it. Useful when configuring Apina
    // to work behind reverse proxies. Defaults to empty string (URL is not modified).
    removedUrlPrefix.set("/foo")
    
    // Code generation target (Default is 'angular')
    // - 'angular' => Generate Angular module that uses Angular's HttpClient 
    // - 'es6' => Generate code that uses Fetch API and has no dependencies apart from ES6
    platform.set(Platform.ANGULAR) 
}

// Tell the frontend to run apina before setup 
// (the 'setup' task will probably be different for you)
tasks.findByPath(":frontend:setup").dependsOn(tasks.apina)
```

## Using generated code

First make your own module dependent on Apina:

```typescript
import { ApinaModule } from 'apina';

@NgModule({
    imports: [ApinaModule],
    providers: [MyService]
})
class MyModule { }
```

Then just inject the generated endpoint and use it:

```typescript
import { DocumentsEndpoint } from 'apina';

@Injectable()
class MyService {
    
    constructor(private readonly documentsEndpoint: DocumentsEndpoint) { }
    
    load() {
        this.documentsEndpoint.findDocument(42).forEach(doc => this.document = doc);
    }
}
```

## Modules

  - `apina-core` main apina code
  - `apina-cli` command line interface for running conversion
  - `apina-gradle` plugin for Gradle
