import React, { useEffect, useState} from "react";
import {User,AppGroup} from "@/services/interfaces.ts";
import axios from "axios";
import { AuthContext } from "@/components/context/AuthContext.tsx";

type AuthProviderProps = {
    children : React.ReactNode
}

export const AuthProvider = ({children} : AuthProviderProps ) => {
    const defaultUser = {apps: [], email: "", firstName: "", id: -1, lastName: "", phone: ""}
    const [user,setUser] = useState<User>(defaultUser)
    const [appGroups,setAppGroups] = useState<AppGroup[]>([])
    const [isAuthenticated,setIsAuthenticated] = useState<boolean>(false)

    const verifyToken = async () : Promise<User> => {
           return await axios.get("http://localhost:9000/verify",{
                withCredentials:true,
            })
    }

    useEffect(() => {
        verifyToken().then(resp => {
            const userResp = resp
            const groupsMap = new Map<number,AppGroup>()

            userResp.apps.forEach(app => {
                if(app.group){
                    if(!groupsMap.has(app.group.id)){
                        groupsMap.set(app.group.id,app.group)
                    }
                    const group = groupsMap.get(app.group.id)
                    group?.apps.push(app)
                }
            })
            setUser(userResp)
            setAppGroups(Array.from(groupsMap.values()))
            setIsAuthenticated(true)
        }).catch(err => {
            console.error(err)
            setUser(defaultUser)
        })
    },[])

    return(
        <AuthContext.Provider value={
            {user,setUser,isAuthenticated,setIsAuthenticated,appGroups}
        }>
            {children}
        </AuthContext.Provider>
    )
}