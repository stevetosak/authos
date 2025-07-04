import React, {useEffect} from "react";
import {apiGetAuthenticated} from "@/services/config.ts";
import {defaultUser, LoginResponse} from "@/services/types.ts";
import {useAuth} from "@/services/useAuth.ts";
import {useNavigate} from "react-router-dom";

export const AuthCallback : React.FC  = () => {

    const {setIsAuthenticated, setContext, setUser, setAuthLoading} = useAuth()
    const nav = useNavigate()
    const verify = async () => {
        try {
            const resp = await apiGetAuthenticated<LoginResponse>("/verify-sub")
            setAuthLoading(true);
            setContext(resp.data)
            setIsAuthenticated(true);
            nav("/profile")
        } catch (err) {
            console.error(err);
            setUser(defaultUser);
            setIsAuthenticated(false);
        } finally {
            setAuthLoading(false)
        }
    }

    useEffect(() => {
       verify()
    },[])

    return <></>
}