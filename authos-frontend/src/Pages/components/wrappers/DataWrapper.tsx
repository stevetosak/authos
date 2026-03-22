import {App} from "@/services/types.ts";
import {JSX} from "react";
import {TitleDescState, TitleDescWrapper} from "@/Pages/components/wrappers/TitleDescWrapper.tsx";
import {ArrayTagWrapper} from "@/Pages/components/wrappers/ArrayTagWrapper.tsx";
import {
    DusterCallbackUriState,
    DusterCallbackUriWrapper
} from "@/Pages/components/wrappers/DusterCallbackUriWrapper.tsx";


export interface WrapperState {
    editing: boolean,
    currentApp: App,
    editedApp: App,
    addElement: (field: keyof App) => void
    removeElement: (field: keyof App,value: string) => void
    handleInputChange: (field:string, value:string) => void
    inputValues: Record<string, string>
}

interface WrapperProps<K extends WrapperKey> {
    state: WRAPPER_STATE_MAP[K]
    wrapper: K
}

type WRAPPER_STATE_MAP = {
    titleDesc: TitleDescState;
    redirectUri: WrapperState;
    scope: WrapperState,
    grantType: WrapperState,
    responseType: WrapperState,
    dusterCallback: DusterCallbackUriState
}

type WrapperKey = keyof WRAPPER_STATE_MAP


const WRAPPER_COMPONENT_MAP: {
    [K in WrapperKey]: (state: WRAPPER_STATE_MAP[K]) => JSX.Element;
} = {
    titleDesc: TitleDescWrapper,
    redirectUri: (state) => ArrayTagWrapper({...state, field: "redirectUris", placeholder: "Add new redirect URI"}),
    scope: (state) => ArrayTagWrapper({...state, field: "scopes", placeholder: "Add new scope"}),
    grantType: (state) => ArrayTagWrapper({...state, field: "grantTypes", placeholder: "Add new grant type"}),
    responseType: (state) => ArrayTagWrapper({...state, field: "responseTypes", placeholder: "Add new response type"}),
    dusterCallback: DusterCallbackUriWrapper
};

type FieldValidation = {
    required?: boolean;
    validate?: (value: string) => boolean;
    errorMessage?: string;
};
export const fieldValidations: Partial<Record<keyof App, FieldValidation>> = {
    redirectUris: {
        validate: (value) => /^(https:\/\/[^\s/$.?#].[^\s]*)$|^(http:\/\/(localhost|127\.0\.0\.1)(:\d{1,5})?\/?.*)$/.test(value),
        errorMessage: "Invalid URL format"
    },
};

export const DataWrapper = <K extends WrapperKey>({wrapper, state}: WrapperProps<K>) => {
    const component = WRAPPER_COMPONENT_MAP[wrapper]
    return component(state)
}