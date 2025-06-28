import {App, AppGroup} from "@/services/types.ts";

export class Factory {
    static appGroupDefault = (): AppGroup => {
        return {
            id: -1,
            name: "",
            createdAt: Date(),
            isDefault: false,
            ssoPolicy: "Partial",
            mfaPolicy: "Disabled"
        }
    }

    static appDefault = (): App => {
        return {
            appUrl: "",
            clientSecretExpiresAt: "",
            id: -1,
            name: "",
            clientId: "",
            clientSecret: "",
            group: this.appGroupDefault().id,
            redirectUris: [],
            createdAt: Date(),
            scopes: [],
            grantTypes: [""],
            responseTypes: [""],
            tokenEndpointAuthMethod: "",
            shortDescription: "",
            logoUri: ""
        }
    }
}