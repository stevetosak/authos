export interface User{
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    phone?: string,
}

export interface App {
    id: number,
    name: string,
    redirectUris: string[],
    clientId: string,
    clientSecret: string,
    clientSecretExpiresAt: string,
    createdAt: string,
    shortDescription: string,
    scopes: string[],
    responseTypes: [string]
    grantTypes: [string],
    tokenEndpointAuthMethod: string,
    logoUri: string,
    appUrl: string
    group: number,
    dusterCallbackUri:string
}

export interface AppGroup {
    id: number,
    name: string,
    createdAt: string,
    isDefault: boolean,
    ssoPolicy: SSOPolicyValue,
    mfaPolicy: MFAPolicyValue
}
export type SSOPolicyValue =  "Full" | "Partial" | "Same Domain" | "Disabled";
export type MFAPolicyValue = "Email" | "Phone" | "Disabled";
export interface CreateAppGroupDTO {
    name: string,
    isDefault: boolean,
    ssoPolicy: SSOPolicyValue,
    mfaPolicy: MFAPolicyValue
}
export type AppGroupEditableField = "isDefault" | "name" | "ssoPolicy" | "mfaPolicy"

export interface DusterApp {
    id: number
    clientId: string,
    clientSecret: string,
    tokenFetchMode: string
    userId: number,
    createdAt: string
}
export const defaultDusterApp: DusterApp = {
    id: -1,
    clientId: "",
    clientSecret: "",
    tokenFetchMode: "auto",
    userId: -1,
    createdAt: Date()
}

export const defaultUser: User = {id: -1,email: "",firstName: "",lastName: "", phone: ""}
export const defaultApp: App = {
    id: -1,
    name: "",
    redirectUris: [],
    clientId: "",
    clientSecret: "",
    clientSecretExpiresAt: "",
    createdAt: "",
    shortDescription: "",
    scopes: [],
    responseTypes: [""],
    grantTypes: [""],
    tokenEndpointAuthMethod: "",
    logoUri: "",
    appUrl: "",
    group: -1,
    dusterCallbackUri: ""
};

export const defaultAppGroup: AppGroup = {
    id: -1,
    name: "",
    createdAt: "",
    isDefault: false,
    ssoPolicy: "Disabled",
    mfaPolicy: "Disabled"
};

export type LoginResponse = {
    user: User,
    apps: App[],
    groups: AppGroup[],
    redirectUri?: string,
    signature?: string
}

export interface JwkKey {
    kty: string;
    e: string;
    use: string;
    kid: string;
    alg: string;
    iat?: number;
    n: string;
}

export interface JwksResponse {
    keys: JwkKey[];
}
