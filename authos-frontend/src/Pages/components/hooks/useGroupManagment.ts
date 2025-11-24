// hooks/useGroupManagement.ts
import React, {SetStateAction, useState} from "react";
import { AppGroup, AppGroupEditableField } from "@/services/types.ts";
import { apiPostAuthenticated } from "@/services/netconfig.ts";
import { toast } from "sonner";

export const useGroupManagement = (initialGroups: AppGroup[], setGroups: React.Dispatch<SetStateAction<AppGroup[]>>) => {


    const handleGroupClick = (group: AppGroup) => {
        if (selectedGroup && selectedGroup.id === group.id) {
            setSelectedGroup(initialGroups[0]);
            setSelectedGroupEditing(initialGroups[0]);
        } else {
            setSelectedGroup(group);
            setSelectedGroupEditing(group);
        }
    };

    const handleGroupUpdate = (param: AppGroupEditableField, value: string | boolean) => {
        setSelectedGroupEditing((prev: AppGroup) => ({
            ...prev,
            [param]: value
        }));
    };



    const handleGroupSave = async () => {
        try {
            if (selectedGroupEditing.id !== selectedGroup.id ||
                JSON.stringify(selectedGroupEditing) !== JSON.stringify(selectedGroup)) {
                await apiPostAuthenticated<AppGroup>("/group/update", selectedGroupEditing);
                setSelectedGroup(selectedGroupEditing);
                setGroups(prevGroups => prevGroups.map(gr =>
                    gr.id === selectedGroupEditing.id ? { ...gr, ...selectedGroupEditing } : gr
                ));
                toast.success("Group updated successfully!");
            }
            setIsEditingGroup(false);
        } catch (error) {
            toast.error("Failed to update group");
        }
    };

    const handleDeleteGroup = async () => {
        if (initialGroups.length < 2) {
            toast.error("Cannot delete the only group");
            return;
        }

        try {
            await apiPostAuthenticated(`/group/delete?group_id=${selectedGroup.id}`);
            toast.success("Successfully deleted group!");

            const filteredGroups = initialGroups.filter(g => g.id !== selectedGroup.id);
            setGroups(filteredGroups);
            setSelectedGroup(filteredGroups[0]);
            setSelectedGroupEditing(filteredGroups[0]);
        } catch (error) {
            toast.error("Failed to delete group");
        }
    };

    const toggleEditGroup = () => {
        if (!isEditingGroup) {
            setSelectedGroupEditing(selectedGroup);
        }
        setIsEditingGroup(!isEditingGroup);
    };

    const handleGroupCancel = () => {
        setSelectedGroupEditing(selectedGroup);
        setIsEditingGroup(false);
    };

    return {
        selectedGroup,
        isEditingGroup,
        selectedGroupEditing,
        handleGroupClick,
        handleGroupUpdate,
        handleGroupSave,
        handleDeleteGroup,
        toggleEditGroup,
        handleGroupCancel,
        setSelectedGroup,
        setSelectedGroupEditing
    };
};