import React, { useState } from "react";
import { App } from "@/services/interfaces";

export function useAppEditor(initialApp: App) {
    const [editedApp, setEditedApp] = useState<App>(initialApp);
    const [newRedirectUri, setNewRedirectUri] = useState("");
    const [newScope, setNewScope] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setEditedApp(prev => ({ ...prev, [name]: value }));
    };

    const addRedirectUri = () => {
        if (newRedirectUri && !editedApp.redirectUris.includes(newRedirectUri)) {
            setEditedApp(prev => ({
                ...prev,
                redirectUris: [...prev.redirectUris, newRedirectUri]
            }));
            setNewRedirectUri("");
        }
    };

    const removeRedirectUri = (uri: string) => {
        setEditedApp(prev => ({
            ...prev,
            redirectUris: prev.redirectUris.filter(u => u !== uri)
        }));
    };

    const addScope = () => {
        if (newScope && !editedApp.scopes.includes(newScope)) {
            setEditedApp(prev => ({
                ...prev,
                scopes: [...prev.scopes, newScope]
            }));
            setNewScope("");
        }
    };

    const removeScope = (scope: string) => {
        setEditedApp(prev => ({
            ...prev,
            scopes: prev.scopes.filter(s => s !== scope)
        }));
    };

    return {
        editedApp,
        setEditedApp,
        newRedirectUri,
        setNewRedirectUri,
        newScope,
        setNewScope,
        handleChange,
        addRedirectUri,
        removeRedirectUri,
        addScope,
        removeScope
    };
}
