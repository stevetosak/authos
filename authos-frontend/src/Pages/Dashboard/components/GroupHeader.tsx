import {CardDescription, CardTitle} from "@/components/ui/card.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Button} from "@/components/ui/button.tsx";
import {CheckIcon, Pencil, Plus, Trash2, XIcon} from "lucide-react";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription, AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle
} from "@/components/ui/alert-dialog.tsx";
import {AlertDialogTrigger} from "@radix-ui/react-alert-dialog";
import {DashboardHandlersPropType, DashboardStatePropsType, defaultAppGroup} from "@/services/types.ts";
import {useNavigate} from "react-router-dom";
import {useAuth} from "@/services/useAuth.ts";
import {useGroupManagement} from "@/Pages/components/hooks/useGroupManagment.ts";
import {useContextGuarded} from "@/services/useContextGuarded.ts";
import {DashboardContext, DashboardContextType} from "@/Pages/Dashboard/components/context/DashboardContext.ts";
import React from "react";

export const GroupHeader = () => {

    const nav = useNavigate();

    const {
        isEditingGroup,
        selectedGroup,
        selectedGroupEditing,
        handleGroupUpdate,
        toggleEditGroup,
        handleGroupCancel,
        handleGroupSave,
        handleDeleteGroup
    } = useContextGuarded<DashboardContextType>(DashboardContext);

    return (
        <>
            {selectedGroup !== defaultAppGroup ?
                <>
                    <div className="md:col-span-2 space-y-2">
                        <div className="flex items-center gap-3">
                            <div
                                className="w-3 h-3 rounded-full bg-emerald-400 flex-shrink-0"/>
                            {!isEditingGroup ? (
                                <CardTitle className="text-xl text-white">
                                    {selectedGroup.name}
                                </CardTitle>
                            ) : (
                                <div className={"flex flex-col"}>
                                    <CardTitle>
                                        <Input className={"text-xl text-white"} name={"name"}
                                               value={selectedGroupEditing.name}
                                               placeholder={"Name"}
                                               onChange={(e) => handleGroupUpdate("name", e.target.value)}></Input>
                                    </CardTitle>
                                </div>
                            )}

                            {selectedGroup && (
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            className="h-8 w-8 text-gray-400 hover:text-teal-300 hover:bg-gray-700/50"
                                            onClick={() => toggleEditGroup()}
                                        >
                                            <Pencil className="w-4 h-4"/>
                                        </Button>
                                    </TooltipTrigger>
                                    <TooltipContent className={"p-2"}>Edit group details</TooltipContent>
                                </Tooltip>
                            )}
                        </div>
                        <CardDescription className="text-gray-400">
                            {selectedGroup
                                ? "Single Sign-On enabled for these applications"
                                : "All registered applications"}
                        </CardDescription>
                    </div>

                    {isEditingGroup ? (
                        <div className={"flex justify-end gap-4"}>
                            <Button
                                variant="default"
                                onClick={handleGroupCancel}
                                className="text-gray-300 border-gray-600 hover:bg-gray-700 w-full sm:w-auto"
                            >
                                <XIcon className="w-4 h-4 mr-2"/>
                                Cancel
                            </Button>
                            <Button
                                variant={"default"}
                                onClick={handleGroupSave}
                                className="border-emerald-400/30 text-emerald-400 hover:bg-emerald-400/10 hover:text-emerald-300 w-full sm:w-auto"
                            >
                                <CheckIcon className="w-4 h-4 mr-2"/>
                                Save Changes
                            </Button>
                        </div>
                    ) : (<div className="flex justify-end items-start gap-2">
                        <Button
                            variant="default"
                            onClick={() => nav(`/connect/register?group=${selectedGroup.id}`)}
                        >
                            <Plus className="w-4 h-4"/>
                            <span>New Application</span>
                        </Button>
                        <AlertDialog>
                            <AlertDialogTrigger asChild={true}>
                                <Button variant={'destructive'}
                                >
                                    <Trash2/>
                                    <span>Delete Group</span>
                                </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent
                                className={" bg-gradient-secondary border-gray-700 border-s-2"}>
                                <AlertDialogHeader>
                                    <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                                    <AlertDialogDescription>
                                        This action cannot be undone.
                                        This will permanently delete the selected group and all of the applications
                                        associated
                                        with it.
                                    </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                    <AlertDialogCancel
                                        className={"bg-gray-800 border border-gray-700 hover:bg-gray-700/80"}>
                                        Cancel
                                    </AlertDialogCancel>
                                    <AlertDialogAction  onClick={() => handleDeleteGroup()}
                                                       className={"border-red-500 text-red-500 hover:translate-x-0.5 hover:-translate-y-0.5"}>
                                        Delete
                                    </AlertDialogAction>

                                </AlertDialogFooter>
                            </AlertDialogContent>
                        </AlertDialog>
                    </div>)}

                </> : <div className="md:col-span-2 space-y-2">
                    <div className="flex items-center gap-3">
                        <div className="w-3 h-3 rounded-full bg-emerald-400 flex-shrink-0"/>
                        <CardTitle className="text-xl text-white">
                            All Apps
                        </CardTitle>
                    </div>
                </div>
            }

        </>
    )

}