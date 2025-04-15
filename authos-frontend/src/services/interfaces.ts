export interface User{
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    phone?: string,
    appGroups: AppGroup[]
}

interface App {
    id: number,
    name: string,
    redirectUri: string,
    clientId: string,
    clientSecret: string,
    createdAt: string
}

interface AppGroup {
    id: number,
    name: string,
    createdAt: string,
    apps: App[]
}