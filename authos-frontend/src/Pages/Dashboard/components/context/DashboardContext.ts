import {createContext} from "react";
import {AppGroup, AppGroupEditableField} from "@/services/types.ts";

export interface DashboardContextType {
    isEditingGroup: boolean;
    selectedGroup: AppGroup;
    selectedGroupEditing: AppGroup;

    handleGroupClick: (group: AppGroup) => void;
    handleGroupUpdate: (param: AppGroupEditableField, value: string | boolean) => void;
    handleGroupSave: () => Promise<void>;
    handleDeleteGroup: () => Promise<void>;
    toggleEditGroup: () => void;
    handleGroupCancel: () => void;
}


export const DashboardContext = createContext<DashboardContextType | null>(null)