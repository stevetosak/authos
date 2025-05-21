import {App, AppGroup} from "@/services/interfaces.ts";

export class Factory {
    static appGroupDefault = (): AppGroup => {
        return {
            id: -1,
            name: "",
            apps: [],
            createdAt: Date()
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
            group: this.appGroupDefault(),
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