import React, {createContext, Dispatch, SetStateAction} from "react";
import {App, AppGroup, LoginResponse, User} from "@/services/interfaces.ts";

interface AuthContextType{
    user: User,
    setUser: React.Dispatch<React.SetStateAction<User>>
    isAuthenticated: boolean,
    setIsAuthenticated: Dispatch<SetStateAction<boolean>>
    authLoading: boolean
    setAuthLoading: Dispatch<SetStateAction<boolean>>
    pageLoading: boolean
    setPageLoading: Dispatch<SetStateAction<boolean>>
    apps: App[]
    setApps: Dispatch<SetStateAction<App[]>>
    groups: AppGroup[]
    setGroups: Dispatch<SetStateAction<AppGroup[]>>
    refreshAuth: () => Promise<void>,
    setContext: (resp: LoginResponse) => void
    resetContext: () => void
}



export const AuthContext = createContext<AuthContextType | null>(null)