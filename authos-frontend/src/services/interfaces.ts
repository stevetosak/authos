export interface User{
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    phone?: string,
    appGroups: AppGroup[]
}

export interface App {
    id: number,
    name: string,
    redirectUris: string[],
    clientId: string,
    clientSecret: string,
    createdAt: string,
    shortDescription: string,
    scopes: string[],
    responseTypes: [string]
    grantTypes: [string],
    tokenEndpointAuthMethod: string,
    logoUri: string,
    group: AppGroup
}

export interface AppGroup {
    id: number,
    name: string,
    createdAt: string,
    apps: App[],
}
