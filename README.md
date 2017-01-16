# Apina

Apina creates client-side TypeScript from server-side APIs. Apina reads Spring Web MVC's
`@RestController` annotated classes and their related Jackson classes and creates code
for the data model and for executing HTTP-requests.

## Using the Gradle-plugin

Include something like the following in your web application project:

```groovy
plugins {
    id "fi.evident.apina" version "0.6.5"
}

apina {
    // Set the name of the created TypeScript file. Default is 'build/apina/apina.ts'.
    target = new File(project(":frontend").projectDir, 'app/apina-api.ts')
    
    // Specify types that should not be generated, but are implemented manually
    // and should be imported to the generated code. Keys are module paths, values 
    // are list of types imported from the module.
    imports = [
      './my-time-module': ['Instant', 'LocalDate'],
      './other-module': ['Foo', 'Bar']
    ]
    
    // Specify target platform. Allowed values are 'angular1' or 'angular2' (default). 
    platform = 'angular2'
     
    // How Java enums are translated to TypeScript enums? (Default mode is 'enum'.)
    //  - 'enum'   => enum MyEnum { FOO, BAR, BAZ }
    //  - 'string' => type MyEnum = "FOO" | "BAR" | "BAZ"
    enumMode = 'string' 
}

// Tell the frontend to run apina before setup 
// (the 'setup' task will probably be different for you)
tasks.findByPath(":frontend:setup").dependsOn apina
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
