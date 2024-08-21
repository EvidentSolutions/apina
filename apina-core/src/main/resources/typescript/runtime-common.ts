export class ApinaConfig {

    /** Prefix added for all API calls */
    baseUrl: string = "";

    private serializers: SerializerMap = {
        any: identitySerializer,
        string: identitySerializer,
        number: identitySerializer,
        boolean: identitySerializer
    };

    constructor() {
        registerDefaultSerializers(this);
    }

    serialize(value: any, type: string): any {
        return this.lookupSerializer(type).serialize(value);
    }

    deserialize(value: any, type: string): any {
        return this.lookupSerializer(type).deserialize(value);
    }

    registerSerializer(name: string, serializer: Serializer) {
        this.serializers[name] = serializer;
    }

    registerEnumSerializer(name: string, enumObject: any) {
        this.registerSerializer(name, enumSerializer(enumObject));
    }

    registerClassSerializer(name: string, fields: any) {
        this.registerSerializer(name, this.classSerializer(fields));
    }

    registerIdentitySerializer(name: string) {
        this.registerSerializer(name, identitySerializer);
    }

    registerDiscriminatedUnionSerializer(name: string, discriminator: string, types: { [key: string]: string; }) {
        this.registerSerializer(name, this.discriminatedUnionSerializer(discriminator, types));
    }

    private classSerializer(fields: any): Serializer {
        function mapProperties(obj: any, propertyMapper: (value: any, type: string) => any) {
            if (obj === null || obj === undefined) {
                return obj;
            }

            const result: any = {};

            for (const name in fields) {
                if (fields.hasOwnProperty(name)) {
                    const value: any = obj[name];
                    const type: string = fields[name];
                    result[name] = propertyMapper(value, type);
                }
            }

            return result;
        }

        const serialize = this.serialize.bind(this);
        const deserialize = this.deserialize.bind(this);
        return {
            serialize(obj) {
                return mapProperties(obj, serialize);
            },
            deserialize(obj) {
                return mapProperties(obj, deserialize);
            }
        };
    }

    private discriminatedUnionSerializer(discriminatorProperty: string, types: { [key: string]: string; }): Serializer {
        const resolveSerializer = (localType: string) => {
            return this.lookupSerializer(types[localType]);
        };

        return {
            serialize(obj) {
                if (obj == null) return null;

                const localType = obj[discriminatorProperty];
                const result = resolveSerializer(localType).serialize(obj);
                result[discriminatorProperty] = localType;
                return result;
            },
            deserialize(obj) {
                if (obj == null) return null;

                const localType = obj[discriminatorProperty];
                const result = resolveSerializer(localType).deserialize(obj);
                result[discriminatorProperty] = localType;
                return result;
            }
        };
    }

    private lookupSerializer(type: string): Serializer {
        if (!type) throw new Error("no type given");

        if (type.indexOf('[]', type.length - 2) !== -1) { // type.endsWith('[]')
            const elementType = type.substring(0, type.length - 2);
            const elementSerializer = this.lookupSerializer(elementType);
            return arraySerializer(elementSerializer);
        }

        const dictionaryMatched = /^Record<string,\s*(.+)>$/.exec(type);
        if (dictionaryMatched) {
            return dictionarySerializer(this.lookupSerializer(dictionaryMatched[1]));
        }

        const serializer = this.serializers[type];
        if (serializer) {
            return serializer;
        } else {
            throw new Error(`could not find serializer for type '${type}'`);
        }
    }
}

function arraySerializer(elementSerializer: Serializer): Serializer {
    function safeMap(value: any[], mapper: (a: any) => any) {
        if (!value)
            return value;
        else
            return value.map(mapper);
    }

    const serialize = elementSerializer.serialize.bind(elementSerializer);
    const deserialize = elementSerializer.deserialize.bind(elementSerializer);

    return {
        serialize(value) {
            return safeMap(value, serialize);
        },
        deserialize(value) {
            return safeMap(value, deserialize);
        }
    }
}

function dictionarySerializer(valueSerializer: Serializer): Serializer {
    function safeMap(dictionary: Record<string, any>, mapper: (a: any) => any) {
        if (!dictionary)
            return dictionary;
        else {
            const result: any = {};
            for (const key in dictionary) {
                if (dictionary.hasOwnProperty(key)) {
                    result[key] = mapper(dictionary[key])
                }
            }
            return result
        }
    }

    const serialize = valueSerializer.serialize.bind(valueSerializer);
    const deserialize = valueSerializer.deserialize.bind(valueSerializer);

    return {
        serialize(value) {
            return safeMap(value, serialize);
        },
        deserialize(value) {
            return safeMap(value, deserialize);
        }
    }
}

function formatQueryParameters(params: { [key: string]: any }): string {
    const queryParameters: string[] = [];

    const addQueryParameter = (encodedKey: string, value: any) => {
        if (value != null)
            queryParameters.push(`${encodedKey}=${encodeURIComponent(value)}`);
    };

    for (const key of Object.keys(params || {})) {
        const value = params[key];
        const encodedKey = encodeURIComponent(key);

        if (Array.isArray(value)) {
            for (const arrayItemValue of value)
                addQueryParameter(encodedKey, arrayItemValue);
        } else {
            addQueryParameter(encodedKey, value);
        }
    }

    return queryParameters.length > 0 ? '?' + queryParameters.join('&') : '';
}

export interface UrlData {
    uriTemplate: string;
    pathVariables?: any;
    requestParams?: any;
}

export interface RequestData extends UrlData {
    method: string;
    requestBody?: any;
    responseType?: string;
}

export interface Serializer {
    serialize(o: any): any;
    deserialize(o: any): any;
}

const identitySerializer: Serializer = {
    serialize(o) {
        return o;
    },
    deserialize(o) {
        return o;
    }
};

function enumSerializer(enumObject: any): Serializer {
    return {
        serialize(o) {
            if (o === null || o === undefined)
                return o;
            else
                return enumObject[o];
        },
        deserialize(o) {
            if (o === null || o === undefined)
                return o;
            else
                return enumObject[o];
        }
    }
}

interface SerializerMap {
    [name: string]: Serializer;
}
