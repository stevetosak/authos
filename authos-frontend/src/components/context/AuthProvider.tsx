import React, {useEffect, useState} from "react";
import {User} from "@/services/interfaces.ts";
import axios from "axios";
import {AuthContext} from "@/components/context/AuthContext.tsx";

type AuthProviderProps = {
    children: React.ReactNode
}

export const AuthProvider = ({children}: AuthProviderProps) => {
    const defaultUser: User = {appGroups: [], email: "", firstName: "", id: -1, lastName: "", phone: ""}
    const [user, setUser] = useState<User>(defaultUser)
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
    const [loading,setLoading] = useState<boolean>(true)

    const verifyToken = async (): Promise<User> => {
        const response = await axios.get("http://localhost:9000/verify", {
            withCredentials: true,
        })
        return response.data
    }

    useEffect(() => {
        verifyToken().then(resp => {
            const userResp = resp
            console.log("USER_RESP: ", userResp)
            setUser(userResp)
            setIsAuthenticated(true)
            setLoading(false)
        }).catch(err => {
            console.error(err)
            setUser(defaultUser)
            setLoading(false)
        })
    }, [])

    return (
        <AuthContext.Provider value={
            {user, setUser, isAuthenticated, setIsAuthenticated,loading,setLoading}
        }>
            {children}
        </AuthContext.Provider>
    )
}