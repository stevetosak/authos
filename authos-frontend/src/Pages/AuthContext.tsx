import React, {createContext, useState} from "react";
import {User} from "@/services/interfaces.ts";

interface AuthContextType{
    user: User,
    setUser: React.Dispatch<React.SetStateAction<User>>
}

export const AuthContext = createContext<AuthContextType | null>(null)

type AuthProviderProps = {
    children : React.ReactNode
}

export const AuthProvider = ({children} : AuthProviderProps ) => {
    const [user,setUser] = useState<User>({appGroups: [], email: "", firstName: "", id: -1, lastName: "", phone: ""})

    return(
        <AuthContext.Provider value={
            {user,setUser}
        }>
            {children}
        </AuthContext.Provider>
    )
}