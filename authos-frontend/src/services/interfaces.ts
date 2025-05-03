export interface User{
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    phone?: string,
    apps: App[]
}


export interface App {
    id: number,
    name: string,
    redirectUri: string,
    clientId: string,
    clientSecret: string,
    createdAt: string
    group: AppGroup
}

export interface AppGroup {
    id: number,
    name: string,
    createdAt: string,
    apps: App[]
}
