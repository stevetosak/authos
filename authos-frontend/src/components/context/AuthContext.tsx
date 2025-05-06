import React, {createContext, Dispatch, SetStateAction} from "react";
import {AppGroup, User} from "@/services/interfaces.ts";

interface AuthContextType{
    user: User,
    setUser: React.Dispatch<React.SetStateAction<User>>
    isAuthenticated: boolean,
    setIsAuthenticated: Dispatch<SetStateAction<boolean>>
    loading: boolean
    setLoading: Dispatch<SetStateAction<boolean>>
}

export const AuthContext = createContext<AuthContextType | null>(null)