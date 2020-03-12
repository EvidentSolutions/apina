export abstract class ApinaEndpointContext {

    constructor(protected config: ApinaConfig) {
    }

    abstract request(data: RequestData): Promise<any>

    serialize(value: any, type: string): any {
        return this.config.serialize(value, type);
    }

    deserialize(value: any, type: string): any {
        return this.config.deserialize(value, type);
    }

    protected buildUrl(uriTemplate: String, pathVariables: any): string {
        return this.config.baseUrl + uriTemplate.replace(/{([^}]+)}/g, (match, name) => pathVariables[name]);
    }
}


export class ES6ApinaEndpointContext extends ApinaEndpointContext {

    constructor(config: ApinaConfig) {
        super(config);
    }

    protected buildRequestInit(data: RequestData): RequestInit {
        return {
            method: data.method,
            body: data.requestBody
        };
    }

    request(data: RequestData): Promise<any> {
        const url = this.buildUrl(data.uriTemplate, data.pathVariables);

        const params = ES6ApinaEndpointContext.formatQueryParameters(data.requestParams);

        const request = this.buildRequestInit(data);

        return fetch(url + params, request)
            .then(r => {
                const responseType = data.responseType;
                if (!r.ok) {
                    return Promise.reject(r);
                } else {
                    return r.text().then(text => {
                        if (responseType) {
                            return this.config.deserialize(text ? JSON.parse(text) : null, responseType)
                        } else {
                            return text
                        }
                    })
                }
            })
    }

    private static formatQueryParameters(params: { [key: string]: any }): string {
        const queryParameters: string[] = [];

        const addQueryParameter = (encodedKey: string, value: any) => {
            if (value != null) {
                queryParameters.push(`${encodedKey}=${encodeURIComponent(value)}`);
            }
        };

        for (const [key, value] of Object.entries(params || {})) {
            const encodedKey = encodeURIComponent(key);

            if (Array.isArray(value)) {
                for (const arrayItemValue of value) {
                    addQueryParameter(encodedKey, arrayItemValue);
                }
            } else {
                addQueryParameter(encodedKey, value);
            }
        }

        return queryParameters.length > 0 ? '?' + queryParameters.join('&') : '';
    }
}
