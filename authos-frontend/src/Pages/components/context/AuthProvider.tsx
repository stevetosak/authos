import React, {useEffect, useState} from "react";
import {App, AppGroup, defaultApp, defaultUser, LoginResponse, User} from "@/services/types.ts";
import axios, {AxiosResponse} from "axios";
import {AuthContext} from "@/Pages/components/context/AuthContext.tsx";
import {apiGetAuthenticated} from "@/services/config.ts";

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


    const setContext = (data: LoginResponse) => {
        setUser(data.user)
        setApps(data.apps)
        setGroups(data.groups)
    }
    const resetContext = () => {
        setUser(defaultUser)
        setApps([])
        setGroups([])
    }

    const verifyToken = async (): Promise<LoginResponse> => {
        const response = await apiGetAuthenticated<LoginResponse>("/verify")
        return response.data;
    };

    const refreshAuth = async () => {
        setAuthLoading(true);
        try {
            const respData = await verifyToken();
            setContext(respData)
            setIsAuthenticated(true);
        } catch (err) {
            console.error(err);
            setUser(defaultUser);
            setIsAuthenticated(false);
        } finally {
            setAuthLoading(false)
        }
    };

    const silentAuth = (): Promise<void> => {
        return new Promise((resolve, reject) => {
            const iframe = document.createElement('iframe');
            iframe.src = `http://localhost:8785/duster/api/v1/oauth/start?client_id=33e16ab8cdb2c9d01de2400475db0472a1922949c34a3c987750e6abc2b6516f&mode=fresh`;
            iframe.style.display = 'none';

            const messageHandler = (event: MessageEvent) => {
                console.warn("EVENT ORIGIN:" + event.origin)
                // if (event.origin !== 'http://localhost:8785') return;

                console.warn("EVENT DATA: ", event.data)

                // Success case
                if (event.data.type === 'duster-auth-complete') {
                    cleanup();
                    refreshAuth().then(resolve);
                }

                // Error case
                else if (event.data.type === 'duster-auth-error') {
                    cleanup();
                    reject(new Error(event.data.authorize_url || 'Authentication failed'));
                }
            };

            const cleanup = () => {
                window.removeEventListener('message', messageHandler);
                document.body.removeChild(iframe);
            };

            // Timeout fallback
            // const timeoutId = setTimeout(() => {
            //     cleanup();
            //     reject(new Error('Authentication timed out'));
            // }, 10000); // 10 second timeout

            window.addEventListener('message', messageHandler);
            document.body.appendChild(iframe);

            // Handle iframe load errors
            // iframe.onerror = () => {
            //     cleanup();
            //     clearTimeout(timeoutId);
            //     reject(new Error('Failed to load auth endpoint'));
            // };
        });
    };


    useEffect(() => {
        refreshAuth()
        // silentAuth()
        //     .then(() => console.log('Silent auth successful'))
        //     .catch(error => {
        //         console.error('Silent auth failed:', error);
        //         console.warn("ERROR MSG: " + error.message)
        //         window.location.href = error.message;
        //     });

        const interval = setInterval(() => {
            if (isAuthenticated) {
                refreshAuth()
                // silentAuthTest()
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