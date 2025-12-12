declare const brand: unique symbol;

export type Branded<T, TBrand extends string> = T & { [brand]: TBrand };

type TypeName = string;
export type ApinaTypeDescriptor = [TypeName, ...ApinaTypeDescriptor[]];
type GenericSerializer = (...serializers: Serializer[]) => Serializer;

abstract class ApinaConfigBase {

    /** Prefix added for all API calls */
    baseUrl: string = "";

    private readonly serializers: Record<TypeName, GenericSerializer> = {};

    protected constructor() {
        this.registerIdentitySerializer("any");
        this.registerIdentitySerializer("string");
        this.registerIdentitySerializer("number");
        this.registerIdentitySerializer("boolean");
        this.registerGenericSerializer("[]", elementSerializer => nullSafeSerializer({
            serialize: o => o.map((v: any) => elementSerializer.serialize(v)),
            deserialize: o => o.map((v: any) => elementSerializer.deserialize(v)),
        }));
        this.registerGenericSerializer("{}", elementSerializer => nullSafeSerializer({
            serialize: o => Object.fromEntries(Object.entries(o).map(([k, v]) => [k, elementSerializer.serialize(v)])),
            deserialize: o => Object.fromEntries(Object.entries(o).map(([k, v]) => [k, elementSerializer.deserialize(v)])),
        }));
    }

    serialize(value: unknown, type: ApinaTypeDescriptor): any {
        return this.lookupSerializer(type).serialize(value);
    }

    deserialize(value: unknown, type: ApinaTypeDescriptor): any {
        return this.lookupSerializer(type).deserialize(value);
    }

    registerSerializer(name: TypeName, serializer: Serializer) {
        this.registerGenericSerializer(name, () => serializer);
    }

    registerGenericSerializer(name: TypeName, serializer: GenericSerializer) {
        this.serializers[name] = serializer;
    }

    registerEnumSerializer(name: TypeName, enumObject: any) {
        this.registerSerializer(name, nullSafeSerializer({
            serialize: o => enumObject[o],
            deserialize: o => enumObject[o],
        }));
    }

    registerClassSerializer<T = unknown>(name: TypeName, fields: Record<keyof T, ApinaTypeDescriptor>) {
        this.registerSerializer(name, nullSafeSerializer({
            serialize: o => Object.fromEntries(Object.entries(fields).map(([name, type]) => [name, this.serialize(o[name], type as ApinaTypeDescriptor)])),
            deserialize: o => Object.fromEntries(Object.entries(fields).map(([name, type]) => [name, this.deserialize(o[name], type as ApinaTypeDescriptor)])),
        }));
    }

    registerIdentitySerializer(name: TypeName) {
        this.registerSerializer(name, identitySerializer);
    }

    registerDiscriminatedUnionSerializer<T = unknown>(name: TypeName, discriminator: keyof T, types: Record<TypeName, ApinaTypeDescriptor>) {
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

    private lookupSerializer(type: ApinaTypeDescriptor): Serializer {
        const check = (condition: boolean): void => {
            if (!condition) throw new Error("invalid type descriptor " + JSON.stringify(type));
        };

        check(type != null && type.length > 0);

        const baseType = type[0];
        const serializerProvider = this.serializers[baseType];
        if (serializerProvider()) {
            const args = type.slice(1) as ApinaTypeDescriptor[];
            const serializers = args.map(arg => this.lookupSerializer(arg));
            return serializerProvider(...serializers);
        }

        console.error(`could not find serializer for type '${baseType}', falling back to identity serializer`);
        return identitySerializer;
    }
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
    responseType?: ApinaTypeDescriptor;
}

export interface Serializer<A = any, B = any> {
    serialize(o: A): B;

    deserialize(o: B): A;
}

const identitySerializer: Serializer = {
    serialize: o => o,
    deserialize: o => o
};

function nullSafeSerializer<A, B>(serializer: Serializer<A, B>): Serializer<A | null | undefined, B | null | undefined> {
    return {
        serialize: (o: A) => (o === null) ? null : (o === undefined) ? undefined : serializer.serialize(o),
        deserialize: (o: B) => (o === null) ? null : (o === undefined) ? undefined : serializer.deserialize(o),
    };
}
