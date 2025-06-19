import React, {useEffect, useState} from "react";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {
    Plus,
    Grid,
    Users,
    FolderOpen,
    Settings,
    CheckCircle,
    Shield,
    Key,
    Calendar,
    Pencil,
    XIcon,
    CheckIcon, DeleteIcon, Trash2
} from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {motion} from "framer-motion"
import {useNavigate} from "react-router-dom";
import {Avatar, AvatarFallback} from "@/components/ui/avatar.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {ScrollArea} from "@/components/ui/scroll-area";
import {AddGroupModal} from "@/Pages/components/AddGroupModal.tsx";
import {AppGroup, AppGroupEditableField, defaultAppGroup} from "@/services/interfaces.ts";
import {Label} from "@/components/ui/label.tsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.tsx";
import {apiPostAuthenticated} from "@/services/config.ts";
import {
    AlertDialog, AlertDialogAction, AlertDialogCancel,
    AlertDialogContent, AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger
} from "@/components/ui/alert-dialog.tsx";
import {toast} from "sonner";
import {Input} from "@/components/ui/input.tsx";

const Dashboard: React.FC = () => {
    const {user, isAuthenticated, groups, apps,setGroups} = useAuth();
    const nav = useNavigate();
    const [selectedGroup, setSelectedGroup] = useState<AppGroup>(defaultAppGroup)
    const [isEditingGroup,setIsEditingGroup] = useState<boolean>(false)
    const [selectedGroupEditing,setSelectedGroupEditing] = useState<AppGroup>(defaultAppGroup)
    const [showDeleteDialog,setShowDeleteDialog] = useState<boolean>(false);


    const handleGroupClick = (group: AppGroup) => {
        if (selectedGroup && selectedGroup === group) {
            setSelectedGroup(defaultAppGroup)
        } else {
            setSelectedGroup(group)
            setSelectedGroupEditing(group)
        }
    }

    const handleGroupUpdate = (param:AppGroupEditableField,value:any) => {
        setSelectedGroupEditing((prev : AppGroup) => ({
            ...prev,
            [param] : value
        }))
    }
    const handleGroupSave = async () => {
        console.log("Group: " + JSON.stringify(selectedGroupEditing))
        if(selectedGroupEditing !== selectedGroup){
            await apiPostAuthenticated<AppGroup>("/group/update",selectedGroupEditing)
            setSelectedGroup(selectedGroupEditing);
            setGroups(prevGroups => prevGroups.map(gr =>
            gr.id === selectedGroupEditing.id ? {...gr,...selectedGroupEditing} : gr) )
        }
        setIsEditingGroup(false)

    }
    const handleDeleteGroup = async () => {
        if(groups.length < 2) {
            alert("Cant delete, no other groups present")
            return;
        }
        await apiPostAuthenticated(`/group/delete?group_id=${selectedGroup.id}`)
        toast.success("Successfully deleted group!")
        const filteredGroups = groups.filter(g => g.id !== selectedGroup.id)
        setGroups(filteredGroups)
        setSelectedGroup(filteredGroups[0])
        setSelectedGroupEditing(filteredGroups[0])

    }
    const handleGroupCancel = () => {
        setIsEditingGroup(false)
    }

    useEffect(() => {
        console.log("USER:::   " + JSON.stringify(user));
        console.log("IS AUTH:" + isAuthenticated);
        setSelectedGroup(groups[0])
    }, [user, isAuthenticated]);

    const toggleEditGroup = () => {
        if(!isEditingGroup) setSelectedGroupEditing(selectedGroup)
        setIsEditingGroup(!isEditingGroup)
    }

    const handleAppClick = (appId: number) => {
        nav(`/dashboard/${appId}`)
    };

    return (
            <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white py-8 w-full">
                <div className="flex flex-col lg:flex-row gap-4 sm:gap-6">
                    <div className="lg:w-72 flex-shrink-0">
                        <div className="sticky top-6 h-[calc(100vh-3rem)] overflow-hidden">
                            <div
                                className="absolute inset-0 bg-gradient-to-b from-gray-800/50 to-transparent pointer-events-none"/>
                            <Card className="bg-gray-800/50 backdrop-blur-sm border border-gray-700/50 overflow-hidden">
                                <CardHeader className="border-b border-gray-700/50 pb-3">
                                    <div className="flex items-center justify-between">
                                        <CardTitle className="flex items-center gap-2 text-lg">
                                            <Users className="w-5 h-5 text-green-400"/>
                                            Application Groups
                                        </CardTitle>
                                        <Tooltip>
                                            <TooltipTrigger asChild>
                                                <AddGroupModal/>
                                            </TooltipTrigger>
                                            <TooltipContent>Create new group</TooltipContent>
                                        </Tooltip>


                                    </div>
                                </CardHeader>
                                <CardContent className="p-2">
                                    <ScrollArea className="h-[calc(100%-60px)] pr-2">
                                        <div className="space-y-1">
                                            {groups.map((group) => (
                                                <Button
                                                    key={group.id}
                                                    onClick={() => handleGroupClick(group)}
                                                    variant={selectedGroup.id === group.id ? "secondary" : "ghost"}
                                                    className={`w-full justify-start h-12 px-4 transition-all ${
                                                        selectedGroup.id === group.id
                                                            ? "bg-gray-700/80 border border-gray-600 shadow-lg"
                                                            : "hover:bg-gray-700/40"
                                                    }`}
                                                >
                                                    <div className="flex items-center gap-3 w-full">
                                                        <div className={`w-2 h-2 rounded-full ${
                                                            selectedGroup.id === group.id ? "bg-green-400" : "bg-gray-500"
                                                        }`}/>
                                                        <span className="truncate flex-1 text-left">{group.name}</span>
                                                        <span className="text-xs bg-gray-700/70 rounded-full px-2 py-1">
                          {apps.filter(app => app.group == group.id).length}
                        </span>
                                                    </div>
                                                </Button>
                                            ))}
                                        </div>
                                    </ScrollArea>
                                </CardContent>
                            </Card>
                        </div>
                    </div>

                    <div className="flex-1">
                        <div className="sticky top-0 z-10 bg-gradient-to-b from-gray-900/90 to-transparent pb-6 pt-2">
                            <Card className="border border-gray-700/50 bg-gray-800/50 backdrop-blur-sm">
                                <CardHeader className="pb-3">
                                    <div id="group-section" className="space-y-4">
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full">
                                            {selectedGroup !== defaultAppGroup && (
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
                                                                        <Input className={"text-xl text-white"} name={"name"} value={selectedGroupEditing.name} placeholder={"Name"} onChange={(e) => handleGroupUpdate("name",e.target.value)}></Input>
                                                                    </CardTitle>
                                                                </div>
                                                            ) }

                                                            {selectedGroup && (
                                                                <Tooltip>
                                                                    <TooltipTrigger asChild>
                                                                        <Button
                                                                            variant="ghost"
                                                                            size="icon"
                                                                            className="h-8 w-8 text-gray-400 hover:text-emerald-400 hover:bg-gray-700/50"
                                                                            onClick={() => toggleEditGroup()}
                                                                        >
                                                                            <Pencil className="w-4 h-4"/>
                                                                        </Button>
                                                                    </TooltipTrigger>
                                                                    <TooltipContent>Edit group details</TooltipContent>
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
                                                                variant="outline"
                                                                onClick={handleGroupCancel}
                                                                className="text-gray-300 border-gray-600 hover:bg-gray-700 w-full sm:w-auto"
                                                            >
                                                                <XIcon className="w-4 h-4 mr-2"/>
                                                                Cancel
                                                            </Button>
                                                            <Button
                                                                variant={"outline"}
                                                                onClick={handleGroupSave}
                                                                className="border-emerald-400/30 text-emerald-400 hover:bg-emerald-400/10 hover:text-emerald-300 w-full sm:w-auto"
                                                            >
                                                                <CheckIcon className="w-4 h-4 mr-2"/>
                                                                Save Changes
                                                            </Button>
                                                        </div>
                                                    ) : (<div className="flex justify-end items-start gap-2">
                                                        <Button
                                                            variant="outline"
                                                            className="flex items-center gap-2 hover:translate-0.5 border-emerald-400/30 text-emerald-400 hover:bg-emerald-400/10 hover:text-emerald-300"
                                                            onClick={() => nav(`/connect/register?group=${selectedGroup.id}`)}
                                                        >
                                                            <Plus className="w-4 h-4"/>
                                                            <span>New Application</span>
                                                        </Button>
                                                        <AlertDialog>
                                                            <AlertDialogTrigger asChild={true}>
                                                                <Button variant={"default"} className={"flex items-center gap-2 " +
                                                                    "hover:bg-red-500 hover:translate-x-0.5 hover:-translate-y-0.5"}
                                                                >
                                                                    <Trash2/>
                                                                    <span>Delete Group</span>
                                                                </Button>
                                                        </AlertDialogTrigger>
                                                            <AlertDialogContent className={"bg-gradient-to-br from-gray-800 to to-gray-900 border-gray-700 border-s-2"}>
                                                                <AlertDialogHeader>
                                                                    <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                                                                    <AlertDialogDescription>
                                                                        This action cannot be undone.
                                                                        This will permanently delete the selected group and all of the applications associated with it.
                                                                    </AlertDialogDescription>
                                                                </AlertDialogHeader>
                                                                <AlertDialogFooter>
                                                                    <AlertDialogCancel className={"bg-emerald-600 border-gray-900 border-2 hover:bg-emerald-400"}>
                                                                            Cancel
                                                                    </AlertDialogCancel>
                                                                    <AlertDialogAction onClick={() => handleDeleteGroup()} className={"border-red-500 hover:bg-red-700 hover:translate-x-0.5 hover:-translate-y-0.5"}>
                                                                        Confirm
                                                                    </AlertDialogAction>

                                                                </AlertDialogFooter>
                                                            </AlertDialogContent>
                                                        </AlertDialog>
                                                    </div>)}



                                                    <div className="bg-gray-800/50 p-4 rounded-lg border border-gray-700/50 md:col-span-3">
                                                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                                                            <div className="space-y-1">
                                                                <Label className="text-gray-400 text-sm flex items-center gap-1">
                                                                    <CheckCircle className="w-4 h-4 text-emerald-400" />
                                                                    Default Group
                                                                </Label>
                                                                {isEditingGroup ? (
                                                                    <Select
                                                                        value={selectedGroupEditing?.isDefault ? "true" : "false"}
                                                                        onValueChange={(value) => handleGroupUpdate("isDefault", value === "true")}
                                                                    >
                                                                        <SelectTrigger className="w-full bg-gray-700 border-gray-600 text-white">
                                                                            <SelectValue placeholder="Select" />
                                                                        </SelectTrigger>
                                                                        <SelectContent className="bg-gray-800 border-gray-700 text-white">
                                                                            <SelectItem value="true">Yes</SelectItem>
                                                                            <SelectItem value="false">No</SelectItem>
                                                                        </SelectContent>
                                                                    </Select>
                                                                ) : (
                                                                    <p className="text-white font-medium">
                                                                        {selectedGroup?.isDefault ? "Yes" : "No"}
                                                                    </p>
                                                                )}
                                                            </div>

                                                            <div className="space-y-1">
                                                                <Label className="text-gray-400 text-sm flex items-center gap-1">
                                                                    <Shield className="w-4 h-4 text-blue-400" />
                                                                    MFA Policy
                                                                </Label>
                                                                {isEditingGroup ? (
                                                                    <Select
                                                                        value={selectedGroupEditing?.mfaPolicy || ""}
                                                                        onValueChange={(value) => handleGroupUpdate("mfaPolicy", value)}
                                                                    >
                                                                        <SelectTrigger className="w-full bg-gray-700 border-gray-600 text-white">
                                                                            <SelectValue placeholder="Select policy" />
                                                                        </SelectTrigger>
                                                                        <SelectContent className="bg-gray-800 border-gray-700 text-white">
                                                                           <SelectItem value={"Email"}>Email</SelectItem>
                                                                            <SelectItem value={"Phone"}>Phone</SelectItem>
                                                                            <SelectItem value={"Disabled"}>Disabled</SelectItem>
                                                                        </SelectContent>
                                                                    </Select>
                                                                ) : (
                                                                    <Badge
                                                                        variant="outline"
                                                                        className="text-white border-gray-600 bg-gray-700/50"
                                                                    >
                                                                        {selectedGroup?.mfaPolicy || "Not set"}
                                                                    </Badge>
                                                                )}
                                                            </div>

                                                            <div className="space-y-1">
                                                                <Label className="text-gray-400 text-sm flex items-center gap-1">
                                                                    <Key className="w-4 h-4 text-purple-400" />
                                                                    SSO Policy
                                                                </Label>
                                                                {isEditingGroup ? (
                                                                    <Select
                                                                        value={selectedGroupEditing?.ssoPolicy || ""}
                                                                        onValueChange={(value) => handleGroupUpdate("ssoPolicy", value)}
                                                                    >
                                                                        <SelectTrigger className="w-full bg-gray-700 border-gray-600 text-white">
                                                                            <SelectValue placeholder="Select policy" />
                                                                        </SelectTrigger>
                                                                        <SelectContent className="bg-gray-800 border-gray-700 text-white">
                                                                            <SelectItem value="Full">Full</SelectItem>
                                                                            <SelectItem value="Partial">Partial</SelectItem>
                                                                            <SelectItem value="Same-Domain">Same-Domain</SelectItem>
                                                                            <SelectItem value="Disabled">Disabled</SelectItem>
                                                                        </SelectContent>
                                                                    </Select>
                                                                ) : (
                                                                    <Badge
                                                                        variant="outline"
                                                                        className="text-white border-gray-600 bg-gray-700/50"
                                                                    >
                                                                        {selectedGroup?.ssoPolicy || "Not set"}
                                                                    </Badge>
                                                                )}
                                                            </div>

                                                            <div className="space-y-1">
                                                                <Label className="text-gray-400 text-sm flex items-center gap-1">
                                                                    <Calendar className="w-4 h-4 text-yellow-400" />
                                                                    Created At
                                                                </Label>
                                                                <p className="text-white font-medium">
                                                                    {selectedGroup?.createdAt
                                                                        ? new Date(selectedGroup.createdAt).toLocaleDateString()
                                                                        : "N/A"}
                                                                </p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </>
                                            )}
                                            {selectedGroup === defaultAppGroup && (
                                                <div className="md:col-span-2 space-y-2">
                                                    <div className="flex items-center gap-3">
                                                        <div
                                                            className="w-3 h-3 rounded-full bg-emerald-400 flex-shrink-0"/>
                                                        <CardTitle className="text-xl text-white">
                                                            All Apps
                                                        </CardTitle>
                                                    </div>
                                                </div>
                                            )}

                                        </div>
                                    </div>
                                </CardHeader>
                            </Card>
                        </div>



                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                            {(selectedGroup !== defaultAppGroup
                                    ? apps.filter((app) => app.group === selectedGroup.id)
                                    : apps
                            )?.map((app) => (
                                <motion.div
                                    key={app.id}
                                    initial={{opacity: 0, y: 10}}
                                    animate={{opacity: 1, y: 0}}
                                    transition={{duration: 0.2}}
                                    whileHover={{y: -3}}
                                >
                                    <Card
                                        className="h-full bg-gray-800/60 border border-gray-700/50 hover:border-green-400/30 transition-colors group overflow-hidden">
                                        <CardHeader className="pb-3">
                                            <div className="flex items-start justify-between">
                                                <div>
                                                    <CardTitle className="flex items-center gap-2">
                                                        <div
                                                            className="w-3 h-3 rounded-full bg-green-400 flex-shrink-0"/>
                                                        {app.name}
                                                    </CardTitle>
                                                    <CardDescription className="text-gray-400 mt-1 line-clamp-2">
                                                        {app.shortDescription || "No description provided"}
                                                    </CardDescription>
                                                </div>
                                                <Avatar className="h-10 w-10 border border-gray-600">
                                                    <AvatarFallback
                                                        className="bg-gray-700/50 group-hover:bg-green-400/10 transition-colors">
                                                        {app.name.charAt(0)}
                                                    </AvatarFallback>
                                                </Avatar>
                                            </div>
                                        </CardHeader>
                                        <CardContent className="pb-4">
                                            <div className="flex gap-2 flex-wrap">
                                                <Badge variant="outline"
                                                       className="text-xs border-gray-600 text-gray-300">
                                                    {app.type || "OIDC"}
                                                </Badge>
                                                <Badge variant="outline"
                                                       className="text-xs border-blue-500/30 text-blue-400">
                                                    {app.status || "Active"}
                                                </Badge>
                                            </div>
                                        </CardContent>
                                        <CardFooter className="border-t border-gray-700/50 pt-3">
                                            <Button
                                                variant="outline"
                                                className="w-full border-gray-600 hover:bg-gray-700/50 hover:border-green-400/30 group-hover:shadow-[0_0_15px_-3px_rgba(74,222,128,0.3)] transition-all"
                                                onClick={() => handleAppClick(app.id)}
                                            >
                                                <Settings
                                                    className="w-4 h-4 mr-2 opacity-70 group-hover:opacity-100 transition-opacity"/>
                                                Manage
                                            </Button>
                                        </CardFooter>
                                    </Card>
                                </motion.div>
                            ))}
                        </div>


                        {!(apps.filter(app => app.group == selectedGroup.id).length > 0) && selectedGroup !== defaultAppGroup && (
                            <motion.div
                                initial={{opacity: 0}}
                                animate={{opacity: 1}}
                                className="flex flex-col items-center justify-center py-16 text-center"
                            >
                                <div className="bg-gray-800/50 border border-gray-700/50 rounded-full p-6 mb-6">
                                    <FolderOpen className="w-10 h-10 text-gray-500"/>
                                </div>
                                <h3 className="text-xl font-medium mb-2">No applications found</h3>
                                <p className="text-gray-400 max-w-md mb-6">
                                    Get started by adding your first application to this group
                                </p>
                            </motion.div>
                        )}
                    </div>
                </div>
            </div>
    );
};

export default Dashboard;
