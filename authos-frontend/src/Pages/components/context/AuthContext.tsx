import React, {createContext, Dispatch, SetStateAction} from "react";
import {App, AppGroup, UserInfoResponse, User} from "@/services/types.ts";

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
    setContext: (resp: UserInfoResponse) => void
    resetContext: () => void
}



export const AuthContext = createContext<AuthContextType | null>(null)