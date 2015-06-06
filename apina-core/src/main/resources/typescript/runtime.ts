export module Support {

    export interface IPromise<T> {
        then<TResult>(successCallback: (promiseValue: T) => IPromise<TResult>|TResult, errorCallback?: (reason: any) => any, notifyCallback?: (state: any) => any): IPromise<TResult>;
        catch<TResult>(onRejected: (reason: any) => IPromise<TResult>|TResult): IPromise<TResult>;
        finally<TResult>(finallyCallback: () => any): IPromise<TResult>;
    }

    export interface IRequestData {
        uriTemplate: string
        method: string
        pathVariables?: any
        requestParams?: any
        requestBody?: any
        responseType?: string
    }

    export interface IHttpProvider {
        request(url: string, method: string, params: any, data: any): IPromise<any>
    }

    export interface ISerializer {
        serialize(o: any): any
        deserialize(o: any): any
    }

    var identitySerializer: ISerializer = {
        serialize(o) {
            return o;
        },
        deserialize(o) {
            return o;
        }
    };

    interface ISerializerMap {
        [name: string]: ISerializer
    }

    export class SerializationConfig {
        private serializers: ISerializerMap = {
            any: identitySerializer,
            string: identitySerializer,
            number: identitySerializer,
            boolean: identitySerializer
        };

        serialize(value: any, type: string): any {
            return this.lookupSerializer(type).serialize(value);
        }

        deserialize(value: any, type: string): any {
            return this.lookupSerializer(type).deserialize(value);
        }

        registerSerializer(name: string, serializer: ISerializer) {
            this.serializers[name] = serializer;
        }

        registerClassSerializer(name: string, fields: any) {
            this.registerSerializer(name, this.classSerializer(fields));
        }

        registerIdentitySerializer(name: string) {
            this.registerSerializer(name, identitySerializer);
        }

        private classSerializer(fields: any): ISerializer {
            function mapProperties(obj: any, propertyMapper: (value: any, type: string) => any) {
                if (obj === null || obj === undefined) {
                    return obj;
                }

                var result = {};

                for (var name in fields) {
                    if (fields.hasOwnProperty(name)) {
                        var value = obj[name];
                        var type = fields[name];
                        result[name] = propertyMapper(value, type);
                    }
                }

                return result;
            }

            var serialize = this.serialize.bind(this);
            var deserialize = this.deserialize.bind(this);
            return {
                serialize(obj) {
                    return mapProperties(obj, serialize);
                },
                deserialize(obj) {
                    return mapProperties(obj, deserialize);
                }
            };
        }

        private lookupSerializer(type: string): ISerializer {
            if (!type) throw new Error("no type given");

            if (type.indexOf('[]', type.length - 2) !== -1) { // type.endsWith('[]')
                var elementType = type.substring(0, type.length - 2);
                var elementSerializer = this.lookupSerializer(elementType);
                return arraySerializer(elementSerializer);
            }
            var serializer = this.serializers[type];
            if (serializer) {
                return serializer;
            } else {
                throw new Error(`could not find serializer for type '${type}'`);
            }
        }
    }

    export class EndpointContext {

        constructor(private httpProvider: IHttpProvider, private serializationConfig: SerializationConfig) {
        }

        request(data: IRequestData): IPromise<any> {
            var url = this.buildUrl(data.uriTemplate, data.pathVariables);

            var responsePromise = this.httpProvider.request(url, data.method, data.requestParams, data.requestBody);
            if (data.responseType) {
                return responsePromise.then(r => this.deserialize(r, data.responseType));
            } else {
                return responsePromise;
            }
        }

        serialize(value: any, type: string): any {
            return this.serializationConfig.serialize(value, type);
        }

        deserialize(value: any, type: string): any {
            return this.serializationConfig.deserialize(value, type);
        }

        private buildUrl(uriTemplate: String, pathVariables: any): string {
            return uriTemplate.replace(/\{([^}]+)}/g, (match, name) => pathVariables[name]);
        }
    }

    function arraySerializer(elementSerializer: ISerializer): ISerializer {
        function safeMap(value, mapper) {
            if (!value)
                return value;
            else
                return value.map(mapper);
        }

        return {
            serialize(value) {
                return safeMap(value, elementSerializer.serialize.bind(elementSerializer));
            },
            deserialize(value) {
                return safeMap(value, elementSerializer.deserialize.bind(elementSerializer));
            }
        }
    }

    module Angular {
        class AngularHttpProvider implements Support.IHttpProvider {

            constructor(private $http: angular.IHttpService) {
            }

            request(url: string, method: string, params: any, data: any): IPromise<any> {
                return this.$http({
                    url: url,
                    method: method,
                    params: params,
                    data: data
                }).then(response => response.data);
            }
        }

        var serializationConfig = new SerializationConfig();
        Types.registerDefaultSerializers(serializationConfig);

        var apinaModule = angular.module('apina.api', []);

        apinaModule.constant('apinaSerializationConfig', serializationConfig);

        apinaModule.service('apinaEndpointContext', ['$http', 'apinaSerializationConfig', ($http: angular.IHttpService, apinaSerializationConfig: SerializationConfig) =>
            new EndpointContext(new AngularHttpProvider($http), apinaSerializationConfig)]);

        apinaModule.service('endpointGroups', ['apinaEndpointContext', (apinaEndpointContext: EndpointContext) =>
            Endpoints.createEndpointGroups(apinaEndpointContext)]);

        var endpointsModule = angular.module('apina.endpoints', ['apina.api']);

        Endpoints.endpointGroupNames.forEach(name => {
            endpointsModule.factory(name + 'Endpoints', ['endpointGroups', (endpointGroups: Endpoints.IEndpointGroups) => endpointGroups[name]]);
        });
    }
}
