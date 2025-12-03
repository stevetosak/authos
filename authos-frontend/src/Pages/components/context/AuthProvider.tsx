import React, {useEffect, useState} from "react";
import {App, AppGroup, defaultApp, defaultUser, UserInfoResponse, User} from "@/services/types.ts";
import axios, {AxiosResponse} from "axios";
import {AuthContext} from "@/Pages/components/context/AuthContext.tsx";
import {apiGetAuthenticated} from "@/services/netconfig.ts";

type AuthProviderProps = {
    children: React.ReactNode
}


export const AuthProvider = ({children}: AuthProviderProps) => {
    const [user, setUser] = useState<User>(defaultUser);
    const [apps, setApps] = useState<App[]>([])
    const [groups, setGroups] = useState<AppGroup[]>([])
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [authLoading, setAuthLoading] = useState<boolean>(true);
    const [pageLoading, setPageLoading] = useState<boolean>(true);


    const setContext = (data: UserInfoResponse) => {
        setUser(data.user)
        setApps(data.apps)
        setGroups(data.groups)
    }
    const resetContext = () => {
        setUser(defaultUser)
        setApps([])
        setGroups([])
    }

    const verifyToken = async (): Promise<UserInfoResponse> => {
        const response = await apiGetAuthenticated<UserInfoResponse>("/verify")
        return response.data;
    };

    const refreshAuth = async () => {
        setAuthLoading(true);
        try {
            const respData = await verifyToken();
            setContext(respData)
            setIsAuthenticated(true);
        } catch (err) {
            setUser(defaultUser);
            setIsAuthenticated(false);
        } finally {
            setAuthLoading(false)
        }
    };





    useEffect(() => {
       console.log("context load")
        refreshAuth()

        const interval = setInterval(() => {
            if (isAuthenticated) {
                refreshAuth()
            }
        }, 5 * 60 * 1000);
        return () => clearInterval(interval)
    }, []);

    return (
        <AuthContext.Provider
            value={{
                user,
                setUser,
                apps,
                setApps,
                groups,
                setGroups,
                setContext,
                isAuthenticated,
                setIsAuthenticated,
                authLoading: authLoading,
                setAuthLoading: setAuthLoading,
                refreshAuth,
                resetContext,
                setPageLoading,
                pageLoading
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};