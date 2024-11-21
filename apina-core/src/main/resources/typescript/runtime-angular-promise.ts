export abstract class ApinaEndpointContext {

    constructor(protected readonly config: ApinaConfig) {
    }

    abstract request(data: RequestData): Promise<any>

    url(data: UrlData): string {
        const url = this.buildUrl(data.uriTemplate, data.pathVariables);
        return url + formatQueryParameters(data.requestParams);
    }

    serialize(value: any, type: string): any {
        return this.config.serialize(value, type);
    }

    deserialize(value: any, type: string): any {
        return this.config.deserialize(value, type);
    }

    protected buildUrl(uriTemplate: String, pathVariables: any): string {
        return this.config.baseUrl + uriTemplate.replace(/{([^}]+)}/g, (_match, name) => pathVariables[name]);
    }
}

@Injectable()
export class DefaultApinaEndpointContext extends ApinaEndpointContext {

    constructor(private readonly httpClient: HttpClient, config: ApinaConfig) {
        super(config);
    }

    request(data: RequestData): Promise<any> {
        const url = this.buildUrl(data.uriTemplate, data.pathVariables);

        const requestParams = data.requestParams;
        let params: HttpParams | undefined = undefined;
        if (requestParams != null) {
            const filteredParams: { [key: string]: any }  = {};
            for (const key of Object.keys(requestParams)) {
                const value = requestParams[key];
                if (value != null)
                    filteredParams[key] = value;
            }

            params = new HttpParams({fromObject: filteredParams});
        }

        return firstValueFrom(this.httpClient.request(data.method, url, { params: params, body: data.requestBody }))
            .then(r => data.responseType ? this.config.deserialize(r, data.responseType) : r);
    }
}

interface ProvideParams {
    config?: ApinaConfig;
    endpointContextClass?: Type<ApinaEndpointContext>;
}

export function provideApina(params: ProvideParams = {}): Provider[] {
    return [
        { provide: ApinaConfig, useValue: params.config ?? new ApinaConfig() },
        { provide: ApinaEndpointContext, useClass: params.endpointContextClass ?? DefaultApinaEndpointContext },
    ];
}
