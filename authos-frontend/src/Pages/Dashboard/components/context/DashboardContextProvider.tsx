import React, {useState} from "react";
import {AppGroup, AppGroupEditableField, defaultAppGroup} from "@/services/types.ts";
import {apiPostAuthenticated} from "@/services/netconfig.ts";
import {toast} from "sonner";
import {useAuth} from "@/services/useAuth.ts";
import {DashboardContext} from "@/Pages/Dashboard/components/context/DashboardContext.ts";

export const DashboardContextProvider = ({children}: { children: React.ReactNode }) => {
    const {groups,setGroups} = useAuth();

    const [selectedGroup, setSelectedGroup] = useState<AppGroup>(groups[0]);
    const [isEditingGroup, setIsEditingGroup] = useState<boolean>(false);
    const [selectedGroupEditing, setSelectedGroupEditing] = useState<AppGroup>(groups[0]);


    const handleGroupClick = (group: AppGroup) => {
        if (selectedGroup && selectedGroup === group) {
            setSelectedGroup(defaultAppGroup);
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
                    gr.id === selectedGroupEditing.id ? {...gr, ...selectedGroupEditing} : gr
                ));
                toast.success("Group updated successfully!");
            }
            setIsEditingGroup(false);
        } catch (error) {
            toast.error("Failed to update group");
        }
    };

    const handleDeleteGroup = async () => {
        if (groups.length < 2) {
            toast.error("Cannot delete the only group");
            return;
        }

        try {
            await apiPostAuthenticated(`/group/delete?group_id=${selectedGroup.id}`);
            toast.success("Successfully deleted group!");

            const filteredGroups = groups.filter(g => g.id !== selectedGroup.id);
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

    return (
        <DashboardContext
            value={{
                isEditingGroup: isEditingGroup,
                selectedGroup: selectedGroup,
                selectedGroupEditing: selectedGroupEditing,

                handleGroupClick: handleGroupClick,
                handleGroupUpdate: handleGroupUpdate,
                handleGroupSave: handleGroupSave,
                handleDeleteGroup: handleDeleteGroup,
                toggleEditGroup: toggleEditGroup,
                handleGroupCancel: handleGroupCancel
            }}
        >
            {children}
        </DashboardContext>
    );


}