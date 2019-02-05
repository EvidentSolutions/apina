export abstract class ApinaEndpointContext {

    constructor(protected config: ApinaConfig) {
    }

    abstract request(data: RequestData): Observable<any>

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

@Injectable()
export class DefaultApinaEndpointContext extends ApinaEndpointContext {

    constructor(private httpClient: HttpClient, config: ApinaConfig) {
        super(config);
    }

    request(data: RequestData): Observable<any> {
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


        return this.httpClient.request(data.method, url, { params: params, body: data.requestBody })
            .pipe(map(r => data.responseType ? this.config.deserialize(r, data.responseType) : r));
    }
}
