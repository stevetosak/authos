import {App} from "@/services/interfaces.ts";
import {JSX} from "react";
import {TitleDescState, TitleDescWrapper} from "@/components/wrappers/TitleDescWrapper.tsx";
import {redirectUriState, RedirectUriWrapper} from "@/components/wrappers/RedirectUriWrapper.tsx";
import {scopeState, ScopeWrapper} from "@/components/wrappers/ScopeWrapper.tsx";

export interface WrapperState {
    editing: boolean,
    currentApp: App,
    editedApp: App,
}

interface WrapperProps<K extends WrapperKey> {
    state: WRAPPER_STATE_MAP[K]
    wrapper: K
}

type WRAPPER_STATE_MAP = {
    titleDesc: TitleDescState;
    redirectUri: redirectUriState;
    scope: scopeState
}

type WrapperKey = keyof WRAPPER_STATE_MAP


const WRAPPER_COMPONENT_MAP: {
    [K in WrapperKey]: (state: WRAPPER_STATE_MAP[K]) => JSX.Element;
} = {
    titleDesc: TitleDescWrapper,
    redirectUri: RedirectUriWrapper,
    scope: ScopeWrapper
};

export const DataWrapper = <K extends WrapperKey>({wrapper, state}: WrapperProps<K>) => {
    const component = WRAPPER_COMPONENT_MAP[wrapper]
    return component(state)
}