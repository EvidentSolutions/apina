export module Support {

    export interface IPromise<T> {
        then<TResult>(successCallback: (promiseValue: T) => IPromise<TResult>, errorCallback?: (reason: any) => any, notifyCallback?: (state: any) => any): IPromise<TResult>;
        then<TResult>(successCallback: (promiseValue: T) => TResult, errorCallback?: (reason: any) => TResult, notifyCallback?: (state: any) => any): IPromise<TResult>;
        catch<TResult>(onRejected: (reason: any) => IPromise<TResult>): IPromise<TResult>;
        catch<TResult>(onRejected: (reason: any) => TResult): IPromise<TResult>;
        finally<TResult>(finallyCallback: () => any): IPromise<TResult>;
    }

    export interface IRequestData {
        uriTemplate: String
        method: String
        pathVariables?: any
        requestParams?: any
        requestBody?: any
        requestBodyType?: string
        responseBodyType?: string
    }

    export interface Context {
        serialize(value: any, name: string): any
        request(data: IRequestData): IPromise<any>
        registerSerializer(name: string, serializer: Serializer)
        registerClassSerializer(name: string, fields: any)
    }

    export interface Serializer {
        fromJson(o: any): any
        toJson(o: any): any
    }

    var identitySerializer: Serializer = {
        fromJson(o) { return o; },
        toJson(o) { return o; }
    };

    var serializers = {
        string: identitySerializer,
        number: identitySerializer,
        boolean: identitySerializer
    };
}
