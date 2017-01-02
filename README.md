# Apina

Apina creates client-side TypeScript from server-side APIs. Apina reads Spring Web MVC's
`@RestController` annotated classes and their related Jackson classes and creates related
data type and endpoint interfaces. It also creates implementation for the interfaces using
AngularJS' `$http` service.

## Using the Gradle-plugin

Include something like the following in your web application project:

```groovy
plugins {
    id "fi.evident.apina" version "0.6.3"
}

compileJava { 
    options.compilerArgs = ['-parameters'] 
}

apina {
    // set the name of the created TypeScript file
    target = new File(project(":frontend").projectDir, 'app/apina-api.ts')
    platform = 'angular1'   // default value is 'angular2'
}

// tell the frontend to run apina in setup
// (the 'setup' task will probably be different for you)
tasks.findByPath(":frontend:setup").dependsOn apina
```

## Using generated code

If you have a Spring controller named `DocumentsController` with a method
`Document findDocument(int id)`, you can say the following:

```typescript
import { ApinaModule, DocumentsEndpoint } from 'apina-api';

@NgModule({
    imports: [ApinaModule],
    providers: [MyService]
})
class MyModule {
}

@Injectable()
class MyService {
    
    constructor(documentsEndpoint: DocumentsEndpoint) {
        documentsEndpoint.findDocument(42).forEach(doc => this.document = doc);
    }
}
```

## Modules

  - `apina-core` main apina code
  - `apina-cli` command line interface for running conversion
  - `apina-gradle` plugin for Gradle
