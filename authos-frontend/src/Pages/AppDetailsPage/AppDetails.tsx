import {
    Card,
    CardHeader,
    CardContent,
    CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import Layout from "@/components/Layout.tsx";
import {useEffect, useState} from "react";
import { Label } from "@/components/ui/label";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { toast } from "sonner";
import {CheckIcon, CopyIcon, EditIcon, RefreshCwIcon, TrashIcon} from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {useParams} from "react-router";
import {useNavigate} from "react-router-dom";
import {App} from "@/services/interfaces.ts";
import {Factory} from "@/services/Factory.ts";
import {DataWrapper, WrapperState} from "@/components/wrappers/DataWrapper.tsx";
import {useAppEditor} from "@/hooks/use-app-editor.ts";

//TODO AUTHORIZATION PER USER

export default function AppDetails() {
    const {user} = useAuth()
    const {appId} = useParams();
    const [app, setApp] = useState<App>(Factory.appDefault());

    const [isEditing, setIsEditing] = useState(false);
    const [isRegeneratingSecret, setIsRegeneratingSecret] = useState(false);
    const nav = useNavigate()

    const {
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
    } = useAppEditor(app);



    useEffect(() => {
        if(appId != undefined){
            const targetApp = user.appGroups.flatMap(ag => ag.apps).find(a => a.id === (parseInt(appId)))
            if(targetApp != undefined){
                setApp(targetApp);
                setEditedApp(targetApp)
            }
            console.error("Cant find app with that id")
        }

        console.error("appid undefined")

    }, []);




    const handleEdit = () => {
        setIsEditing(true);
        setEditedApp(app);
    };

    const handleSave = () => {
        setApp(editedApp);
        setIsEditing(false);
        toast.success("Application updated successfully");
    };

    const handleCancel = () => {
        setIsEditing(false);
    };


    const toggleStatus = () => {
    };


    const regenerateSecret = () => {
        setIsRegeneratingSecret(true);
        // Simulate API call
        setTimeout(() => {
            toast.success("Client secret regenerated");
        }, 1000);
    };

    const copyToClipboard = (text: string) => {
        navigator.clipboard.writeText(text);
        toast.success("Copied to clipboard");
    };


    const deleteApp = () => {
        toast.warning("Are you sure you want to delete this application?", {
            action: {
                label: "Delete",
                onClick: () => {
                    toast.error("Application deleted");
                    //redirect tuka do dashboard pak
                }
            },
            cancel: {
                label: "Cancel",
                onClick: () => {}
            }
        });
    };

    const currentApp = isEditing ? editedApp : app;

    const baseState : WrapperState = {
        editing: isEditing,
        currentApp: currentApp,
        editedApp: editedApp
    }

    return (
        <Layout>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800 text-gray-100 p-4 md:p-10 font-sans">
                <Card className="bg-gray-800 border border-gray-700 shadow-xl rounded-xl overflow-hidden max-w-4xl mx-auto">
                    <CardHeader className="border-b border-gray-700 p-6">
                        <div className={"flex flex-row justify-between"}>
                            <div
                                className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                                <div className="space-y-1">
                                    <DataWrapper state={{...baseState, onChange: handleChange}} wrapper={"titleDesc"}>
                                    </DataWrapper>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 text-sm text-gray-400">
                                <span className="font-medium text-gray-300">Group:</span>
                                <span className="truncate max-w-[150px]">{app.group.name}</span>
                            </div>

                        </div>

                    </CardHeader>

                    <CardContent className="p-0 w-full">
                        <Tabs defaultValue="general" className="w-full">
                            <TabsList
                                className="bg-gray-800 rounded-none border-b border-gray-700 px-8 py-0 flex gap-0 w-full">
                                <TabsTrigger
                                    value="general"
                                    className="flex-1 data-[state=active]:bg-gray-700 data-[state=active]:text-white rounded-none border-b-2 border-transparent data-[state=active]:border-emerald-500 py-4"
                                >
                                    General
                                </TabsTrigger>
                                <TabsTrigger
                                    value="credentials"
                                    className="flex-1 data-[state=active]:bg-gray-700 data-[state=active]:text-white rounded-none border-b-2 border-transparent data-[state=active]:border-emerald-500 py-4"
                                >
                                    Credentials
                                </TabsTrigger>
                                <TabsTrigger
                                    value="settings"
                                    className="flex-1 data-[state=active]:bg-gray-700 data-[state=active]:text-white rounded-none border-b-2 border-transparent data-[state=active]:border-emerald-500 py-4"
                                >
                                    Settings
                                </TabsTrigger>
                            </TabsList>

                            <TabsContent value="general" className="p-6">
                                <div className="space-y-6">
                                    <div className="space-y-4">
                                        <h3 className="text-lg font-semibold">Redirect URIs</h3>
                                        <div className="space-y-2">
                                            <DataWrapper state={{...baseState,newRedirectUri: newRedirectUri,addRedirectUri,setNewRedirectUri,removeRedirectUri}} wrapper={"redirectUri"}/>
                                        </div>
                                    </div>

                                    <div className="space-y-4">
                                        <h3 className="text-lg font-semibold">Scopes</h3>
                                        <div className="space-y-2">
                                            <DataWrapper state={{...baseState,newScope,setNewScope,addScope,removeScope}} wrapper={"scope"}/>
                                        </div>
                                    </div>
                                </div>
                            </TabsContent>

                            <TabsContent value="credentials" className="p-6 space-y-6">
                                <div className="space-y-2">
                                    <div className="flex items-center justify-between">
                                        <Label className="text-gray-300">Client ID</Label>
                                        <Tooltip>
                                            <TooltipTrigger asChild>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="w-6 h-6"
                                                    onClick={() => copyToClipboard(currentApp.clientId)}
                                                >
                                                    <CopyIcon className="w-3 h-3" />
                                                </Button>
                                            </TooltipTrigger>
                                            <TooltipContent>Copy to clipboard</TooltipContent>
                                        </Tooltip>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <code className="bg-gray-700 px-3 py-2 rounded-md text-sm flex-1 overflow-x-auto">
                                            {currentApp.clientId}
                                        </code>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="flex items-center justify-between">
                                        <Label className="text-gray-300">Client Secret</Label>
                                        <div className="flex gap-1">
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="w-6 h-6"
                                                        onClick={() => copyToClipboard(currentApp.clientSecret)}
                                                    >
                                                        <CopyIcon className="w-3 h-3" />
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>Copy to clipboard</TooltipContent>
                                            </Tooltip>
                                            {isEditing && (
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="w-6 h-6"
                                                            onClick={regenerateSecret}
                                                            disabled={isRegeneratingSecret}
                                                        >
                                                            {isRegeneratingSecret ? (
                                                                <RefreshCwIcon className="w-3 h-3 animate-spin" />
                                                            ) : (
                                                                <RefreshCwIcon className="w-3 h-3" />
                                                            )}
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Regenerate secret</TooltipContent>
                                                </Tooltip>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <code className="bg-gray-700 px-3 py-2 rounded-md text-sm flex-1 overflow-x-auto">
                                            {currentApp.clientSecret}
                                        </code>
                                    </div>
                                    <p className="text-xs text-gray-400">
                                        {isEditing
                                            ? "This secret will only be shown once. Make sure to copy it now."
                                            : "For security reasons, we can't show your client secret again."}
                                    </p>
                                </div>
                            </TabsContent>

                            <TabsContent value="settings" className="p-6 space-y-6">
                                <div className="space-y-4">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <Label className="text-gray-300">Created At</Label>
                                            <p className="text-sm text-gray-400">{currentApp.createdAt}</p>
                                        </div>
                                    </div>
                                </div>
                            </TabsContent>
                        </Tabs>
                    </CardContent>

                    <CardFooter className="flex justify-end space-x-3 p-6 border-t border-gray-700">
                        {isEditing ? (
                            <>
                                <Button
                                    variant="outline"
                                    onClick={handleCancel}
                                    className="text-gray-300 border-gray-600 hover:bg-gray-700"
                                >
                                    Cancel
                                </Button>
                                <Button
                                    onClick={handleSave}
                                    className="bg-emerald-600 hover:bg-emerald-500"
                                >
                                    <CheckIcon className="w-4 h-4 mr-2" />
                                    Save Changes
                                </Button>
                            </>
                        ) : (
                            <>
                                <Button
                                    variant="outline"
                                    onClick={handleEdit}
                                    className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                                >
                                    <EditIcon className="w-4 h-4 mr-2" />
                                    Edit
                                </Button>
                                <Button
                                    variant="destructive"
                                    onClick={deleteApp}
                                    className="hover:bg-red-500/90"
                                >
                                    <TrashIcon className="w-4 h-4 mr-2" />
                                    Delete
                                </Button>
                            </>
                        )}
                    </CardFooter>
                </Card>
            </div>
        </Layout>
    );
}