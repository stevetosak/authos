import {AxiosResponse} from "axios";

export interface User {
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    phone?: string,
    lastLoginAt?: Date,
    mfaEnabled: boolean,
    emailVerified: boolean
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
    dusterCallbackUri: string
}

export interface AppGroup {
    id: number,
    name: string,
    createdAt: string,
    isDefault: boolean,
    ssoPolicy: SSOPolicyValue,
    mfaPolicy: MFAPolicyValue
}

export type SSOPolicyValue = "Full" | "Partial" | "Same Domain" | "Disabled";
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

export const defaultUser: User = {id: -1, email: "", firstName: "", lastName: "", phone: "",mfaEnabled:false,emailVerified:false}
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

export type UserInfoResponse = {
    user: User,
    apps: App[],
    groups: AppGroup[],
    redirectUri?: string,
    signature?: string
}

export type LoginResponse = {
    status: "SUCCESS" | "MFA_REQUIRED" | "FAILURE"
    time: Date
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

export type DashboardStatePropsType = {
    apps: App[],
    selectedGroup: AppGroup,
    selectedGroupEditing: AppGroup
    isEditingGroup: boolean,
}

export type DashboardHandlersPropType = {
    handleGroupUpdate: (param: AppGroupEditableField, value: string | boolean) => void,
    handleAppClick: (appId: number) => void
    handleGroupSave: () => Promise<void>
    toggleEditGroup: () => void
    handleDeleteGroup: () => Promise<void>
    handleGroupCancel: () => void
}


export type ProfileTabProps = { active?: boolean, user: User }
export type TotpModalProps = {
    dialogOpen: boolean,
    onOpenChange: (open: boolean) => void,
    user: User,
    onSubmit: (otp: string) => Promise<AxiosResponse>,
    onSuccess?: () => void
}
