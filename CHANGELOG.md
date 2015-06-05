## 0.1.x (yyyy-mm-dd)

### Breaking changes

- Generated code is simplified by translating endpoint groups directly to classes. The types are now named 
 `Endpoints.Foo` instead of `Endpoints.Foo`. Endpoint group properties of `Endpoints.IEndpointGroups` now begin
  with lowercase letter, e.g. `endpointGroups.Foo` is now `endpointGroups.foo`. 
