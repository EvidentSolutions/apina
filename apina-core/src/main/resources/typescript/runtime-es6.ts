export abstract class ApinaEndpointContext {

    constructor(protected config: ApinaConfig) {
    }

    abstract request(data: RequestData): Promise<any>

    url(data: UrlData): string {
        const url = this.buildUrl(data.uriTemplate, data.pathVariables);
        return url + formatQueryParameters(data.requestParams);
    }

    serialize(value: any, type: ApinaTypeDescriptor): any {
        return this.config.serialize(value, type);
    }

    deserialize(value: any, type: ApinaTypeDescriptor): any {
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
        const isSendingData = data.requestBody != null
        const jsonContentType = 'application/json'
        return {
            method: data.method,
            headers: isSendingData ? {
                'Accept': jsonContentType,
                'Content-Type': jsonContentType
            } : {
                'Accept': jsonContentType
            },
            credentials: 'same-origin',
            body: isSendingData ? JSON.stringify(data.requestBody) : null
        };
    }

    request(data: RequestData): Promise<any> {
        const request = this.buildRequestInit(data);

        return fetch(this.url(data), request)
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
}
