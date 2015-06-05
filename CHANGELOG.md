## 0.1.x (yyyy-mm-dd)

### New features

- Expose `Support.EndpointContext` as angular-service `endpointContext`. This allows us to override serializers.
  ([#3](https://github.com/EvidentSolutions/apina/issues/3))

### Breaking changes

- Generated code is simplified by translating endpoint groups directly to classes. The types are now named 
 `Endpoints.Foo` instead of `Endpoints.Foo`. Endpoint group properties of `Endpoints.IEndpointGroups` now begin
  with lowercase letter, e.g. `endpointGroups.Foo` is now `endpointGroups.foo`. 
