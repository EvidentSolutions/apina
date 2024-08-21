declare const brand: unique symbol;

export type Branded<T, TBrand extends string> = T & { [brand]: TBrand };

export class ApinaConfig {

    /** Prefix added for all API calls */
    baseUrl: string = "";

    private readonly serializers: Record<string, Serializer> = {};

    constructor() {
        this.registerIdentitySerializer("any");
        this.registerIdentitySerializer("string");
        this.registerIdentitySerializer("number");
        this.registerIdentitySerializer("boolean");

        registerDefaultSerializers(this);
    }

    serialize(value: unknown, type: string): any {
        return this.lookupSerializer(type).serialize(value);
    }

    deserialize(value: unknown, type: string): any {
        return this.lookupSerializer(type).deserialize(value);
    }

    registerSerializer(name: string, serializer: Serializer) {
        this.serializers[name] = serializer;
    }

    registerEnumSerializer(name: string, enumObject: any) {
        this.registerSerializer(name, nullSafeSerializer({
            serialize: o => enumObject[o],
            deserialize: o => enumObject[o],
        }));
    }

    registerClassSerializer<T = unknown>(name: string, fields: Record<keyof T, string>) {
        this.registerSerializer(name, nullSafeSerializer({
            serialize: o => Object.fromEntries(Object.entries(fields).map(([name, type]) => [name, this.serialize(o[name], type as string)])),
            deserialize: o => Object.fromEntries(Object.entries(fields).map(([name, type]) => [name, this.deserialize(o[name], type as string)])),
        }));
    }

    registerIdentitySerializer(name: string) {
        this.registerSerializer(name, {
            serialize: o => o,
            deserialize: o => o
        });
    }

    registerDiscriminatedUnionSerializer<T = unknown>(name: string, discriminator: keyof T, types: Record<string, string>) {
        const self = this;
        this.registerSerializer(name, nullSafeSerializer({
            serialize(obj) {
                const localType = obj[discriminator];
                return {
                    ...self.lookupSerializer(types[localType]).serialize(obj),
                    [discriminator]: localType
                };
            },
            deserialize(obj) {
                const localType = obj[discriminator];
                return {
                    ...self.lookupSerializer(types[localType]).deserialize(obj),
                    [discriminator]: localType
                };
            }
        }));
    }

    private lookupSerializer(type: string): Serializer {
        if (!type) throw new Error("no type given");

        if (type.endsWith("[]")) {
            const elementType = type.substring(0, type.length - 2);
            return arraySerializer(this.lookupSerializer(elementType));
        }

        const dictionaryMatch = /^Record<string,\s*(.+)>$/.exec(type);
        if (dictionaryMatch)
            return dictionarySerializer(this.lookupSerializer(dictionaryMatch[1]));

        const serializer = this.serializers[type];
        if (serializer)
            return serializer;

        throw new Error(`could not find serializer for type '${type}'`);
    }
}

function arraySerializer(elementSerializer: Serializer): Serializer {
    return nullSafeSerializer({
        serialize: o => o.map((v: any) => elementSerializer.serialize(v)),
        deserialize: o => o.map((v: any) => elementSerializer.deserialize(v)),
    });
}

function dictionarySerializer(elementSerializer: Serializer): Serializer {
    return nullSafeSerializer({
        serialize: o => Object.fromEntries(Object.entries(o).map(([k, v]) => [k, elementSerializer.serialize(v)])),
        deserialize: o => Object.fromEntries(Object.entries(o).map(([k, v]) => [k, elementSerializer.deserialize(v)])),
    });
}

function formatQueryParameters(params: Record<string, unknown>): string {
    const components: string[] = [];

    const addQueryParameter = (encodedKey: string, value: any): void => {
        if (value != null)
            components.push(`${encodedKey}=${encodeURIComponent(value)}`);
    };

    for (const [key, value] of Object.entries(params || {})) {
        const encodedKey = encodeURIComponent(key);

        if (Array.isArray(value)) {
            for (const arrayItemValue of value)
                addQueryParameter(encodedKey, arrayItemValue);
        } else {
            addQueryParameter(encodedKey, value);
        }
    }

    return components.length > 0 ? '?' + components.join('&') : '';
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

export interface Serializer<A = any, B = any> {
    serialize(o: A): B;

    deserialize(o: B): A;
}

function nullSafeSerializer<A, B>(serializer: Serializer<A, B>): Serializer<A | null | undefined, B | null | undefined> {
    return {
        serialize: (o: A) => (o === null) ? null : (o === undefined) ? undefined : serializer.serialize(o),
        deserialize: (o: B) => (o === null) ? null : (o === undefined) ? undefined : serializer.deserialize(o),
    };
}
