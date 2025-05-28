import React, {useEffect, useState} from "react";
import {App, AppGroup, defaultApp, defaultUser, LoginResponse, User} from "@/services/interfaces.ts";
import axios from "axios";
import {AuthContext} from "@/components/context/AuthContext.tsx";

type AuthProviderProps = {
    children: React.ReactNode
}


export const AuthProvider = ({children}: AuthProviderProps) => {
    const [user, setUser] = useState<User>(defaultUser);
    const [apps, setApps] = useState<App[]>([])
    const [groups, setGroups] = useState<AppGroup[]>([])
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(true);


    const setContext = (resp: LoginResponse) => {
        setUser(resp.user)
        setApps(resp.apps)
        setGroups(resp.groups)
    }
    const resetContext = () => {
        setUser(defaultUser)
        setApps([])
        setGroups([])
    }

    const verifyToken = async (): Promise<LoginResponse> => {
        const response = await axios.get("http://localhost:9000/verify", {
            withCredentials: true,
        });
        return response.data;
    };

    const refreshAuth = async () => {
        setLoading(true);
        try {
            const resp = await verifyToken();
            setContext(resp)
            setIsAuthenticated(true);
        } catch (err) {
            console.error(err);
            setUser(defaultUser);
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        refreshAuth()

        const interval = setInterval(() => {
            if (isAuthenticated) {
                refreshAuth()
            }
        }, 5 * 60 * 1000);
        return () => clearInterval(interval)
    }, [isAuthenticated]);

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
                loading,
                setLoading,
                refreshAuth,
                resetContext
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};