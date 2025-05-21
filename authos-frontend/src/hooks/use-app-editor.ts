import React, {useState} from "react";
import {App} from "@/services/interfaces";
import {fieldValidations} from "@/components/wrappers/DataWrapper.tsx";


export function useAppEditor(initialApp: App) {
    const [editedApp, setEditedApp] = useState<App>(initialApp);
    const [inputValues, setInputValues] = useState<Record<string, string>>({
        redirectUris: "",
        scopes: "",
        grantTypes: "",
        responseTypes: ""
    });

    const handleInputChange = (field: string, value: string) => {
        setInputValues(prev => ({...prev, [field]: value}));
    };

    const addToArrayField = (field: keyof App) => {
        const newValue = inputValues[field as string];
        const currentArray = editedApp[field] as string[];

        const validation = fieldValidations[field];

        // Run validation if it exists
        if (validation) {
            if (validation.validate && !validation.validate(newValue)) {
                // setErrors(prev => ({
                //     ...prev,
                //     [field]: validation.errorMessage || "Invalid value"
                // }));
                console.error("error validating")
                return;
            }
        }


        if (newValue && !currentArray.includes(newValue)) {

            setEditedApp(prev => ({
                ...prev,
                [field]: [...(prev[field] as string[]), newValue]
            }));
            handleInputChange(field as string, "");

        }
    };

    const removeFromArrayField = (field: keyof App, value: string) => {
        setEditedApp(prev => ({
            ...prev,
            [field]: (prev[field] as string[]).filter(item => item !== value)
        }));
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setEditedApp(prev => ({...prev, [name]: value}));
    };

    return {
        editedApp,
        setEditedApp,
        inputValues,
        handleInputChange,
        handleChange,
        addToArrayField,
        removeFromArrayField
    };
}
