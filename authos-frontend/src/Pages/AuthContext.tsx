import React, {createContext, useEffect, useState} from "react";
import {User,AppGroup} from "@/services/interfaces.ts";
import axios from "axios";
import App from "@/App.tsx";

interface AuthContextType{
    user: User,
    setUser: React.Dispatch<React.SetStateAction<User>>
    isAuthenticated: () => boolean
    appGroups: AppGroup[]
}

export const AuthContext = createContext<AuthContextType | null>(null)

type AuthProviderProps = {
    children : React.ReactNode
}

export const AuthProvider = ({children} : AuthProviderProps ) => {
    const defaultUser = {apps: [], email: "", firstName: "", id: -1, lastName: "", phone: ""}
    const defaultGroup = {id: -1,name: 'INVALID',createdAt: Date.now(),apps: []}
    const [user,setUser] = useState<User>(defaultUser)
    const [appGroups,setAppGroups] = useState<AppGroup[]>([])

    const verifyToken = async () : Promise<User> => {
           return await axios.get("http://localhost:9000/verify",{
                withCredentials:true,
            })
    }

    const isAuthenticated = () : boolean => {
        return user.id !== -1
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
        }).catch(err => {
            console.error(err)
            setUser(defaultUser)
        })
    },[])

    return(
        <AuthContext.Provider value={
            {user,setUser,isAuthenticated,appGroups}
        }>
            {children}
        </AuthContext.Provider>
    )
}