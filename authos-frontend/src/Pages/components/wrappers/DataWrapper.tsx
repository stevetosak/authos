import {App} from "@/services/types.ts";
import {JSX} from "react";
import {TitleDescState, TitleDescWrapper} from "@/Pages/components/wrappers/TitleDescWrapper.tsx";
import {RedirectUriWrapper} from "@/Pages/components/wrappers/RedirectUriWrapper.tsx";
import {ScopeWrapper} from "@/Pages/components/wrappers/ScopeWrapper.tsx";
import {GrantTypeWrapper} from "@/Pages/components/wrappers/GrantTypesWrapper.tsx";
import {ResponseTypesWrapper} from "@/Pages/components/wrappers/ResponseTypesWrapper.tsx";
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
    redirectUri: RedirectUriWrapper,
    scope: ScopeWrapper,
    grantType: GrantTypeWrapper,
    responseType: ResponseTypesWrapper,
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