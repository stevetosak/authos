import {
    Card,
    CardHeader,
    CardContent,
    CardFooter,
} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Tabs, TabsList, TabsTrigger, TabsContent} from "@/components/ui/tabs";
import  {useEffect, useState} from "react";
import {Label} from "@/components/ui/label";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import {toast} from "sonner";
import {
    CheckIcon,
    CodeIcon,
    CopyIcon,
    EditIcon, Eye, EyeOff,
    GlobeIcon, Info, KeyIcon, LockIcon,
    RefreshCwIcon,
    ShieldIcon, SlidersIcon,
    TrashIcon,
    XIcon
} from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {useParams} from "react-router";
import {App} from "@/services/types.ts";
import {Factory} from "@/services/Factory.ts";
import {DataWrapper, WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {useAppEditor} from "@/Pages/components/hooks/use-app-editor.ts";
import {Badge} from "@/components/ui/badge.tsx";
import {apiPostAuthenticated} from "@/services/config.ts";
import {useNavigate} from "react-router-dom";

//TODO AUTHORIZATION PER USER
// TODO GRAPHS AND METRICS IN GENERAL

export default function AppDetails() {
    const {apps,setApps} = useAuth()
    const {appId} = useParams();
    const [app, setApp] = useState<App>(Factory.appDefault());
    const nav = useNavigate();

    const [isEditing, setIsEditing] = useState(false);
    const [isRegeneratingSecret, setIsRegeneratingSecret] = useState(false);
    const [showSecret,setShowSecret] = useState(false)

    const {
        editedApp,
        setEditedApp,
        inputValues,
        handleInputChange,
        handleChange,
        addToArrayField,
        removeFromArrayField
    } = useAppEditor(app);


    useEffect(() => {
        if (appId != undefined) {
            const targetApp = apps.find(a => a.id === (parseInt(appId)))
            if (targetApp != undefined) {
                setApp(targetApp);
                setEditedApp(targetApp)
            }else {
                console.error("app undefined")
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
        if (app === editedApp) return

        console.log("sending update request")
        console.log(`${JSON.stringify(editedApp)}`)

        apiPostAuthenticated<App>("/app/update", editedApp)
            .then(() => {
            setApp(editedApp);
            setIsEditing(false);
            toast.success("Application updated successfully");
        }).catch(err => {
            toast.error(err)
        })


    };


    const handleCancel = () => {
        setIsEditing(false);
    };


    const regenerateSecret = () => {
        setIsRegeneratingSecret(true);
        toast.warning("Regenerating Secret...")
        setTimeout(() => {
            apiPostAuthenticated<App>("/app/regenerate-secret",app)
                .then(resp => {
                    setApp(resp.data)
                    setEditedApp(resp.data)
                    setIsRegeneratingSecret(false)
                    toast.success("New secret successfully generated!")
                })
        },200)

    };

    const copyToClipboard = async (text: string) => {
        await navigator.clipboard.writeText(text);
        toast.success("Copied to clipboard");
    };


    const deleteApp = () => {
        toast.warning("Are you sure you want to delete this application?", {
            action: {
                label: "Delete",
                onClick: () => {
                    apiPostAuthenticated(`/app/delete?app_id=${app.id}`).then( () =>{
                        toast.success("Successfully deleted app!")
                        setApps(apps.filter(a => a.id !== app.id))
                        setTimeout(() => {
                            nav("/dashboard")
                        },300)
                    })
                }
            },
            cancel: {
                label: "Cancel",
                onClick: () => {
                }
            }
        });
    };

    const currentApp = isEditing ? editedApp : app;

    const baseState: WrapperState = {
        editing: isEditing,
        currentApp: currentApp,
        editedApp: editedApp,
        addElement: addToArrayField,
        removeElement: removeFromArrayField,
        handleInputChange: handleInputChange,
        inputValues: inputValues
    }

    return (
            <div
                className="min-h-screen bg-gradient-to-br text-gray-100 p-4 md:p-10 font-sans">
                <Card
                    className="bg-gray-800 border border-gray-700 shadow-xl rounded-xl overflow-hidden max-w-4xl mx-auto">
                    <CardHeader className="border-b border-gray-700 p-6">
                        <div className="flex flex-col md:flex-row justify-between gap-4">
                            <div className="space-y-2">
                                <DataWrapper state={{...baseState, onChange: handleChange}} wrapper={"titleDesc"}/>
                            </div>

                            <div className="flex flex-col gap-3 text-sm text-gray-400">
                                <div className="flex items-center gap-2">
              {/*                      <span className="font-medium text-gray-300 min-w-[60px]">Group:</span>*/}
              {/*                      <span className="truncate max-w-[180px] bg-gray-700 px-2 py-1 rounded">*/}
              {/*  {app.group.name}*/}
              {/*</span>*/}
                                </div>
                                <div className="flex items-center gap-2">
                                    <span className="font-medium text-gray-300 min-w-[60px]">Created:</span>
                                    <span className="bg-gray-700 px-2 py-1 rounded">
                {currentApp.createdAt}
              </span>
                                </div>
                            </div>
                        </div>
                    </CardHeader>

                    <CardContent className="p-0 w-full">
                        <Tabs defaultValue="credentials" className="w-full">
                            <div className="border-b border-gray-700 h-17">
                                <TabsList className="grid grid-cols-3 bg-gray-800 rounded-none">
                                    <TabsTrigger
                                        value="credentials"
                                        className="py-4 data-[state=active]:bg-gray-700 data-[state=active]:text-white data-[state=active]:shadow-[0_-2px_0_0_theme(colors.emerald.500)_inset]"
                                    >
                                        <KeyIcon className="w-4 h-4 mr-2"/>
                                        Credentials
                                    </TabsTrigger>
                                    <TabsTrigger
                                        value="settings"
                                        className="py-4 data-[state=active]:bg-gray-700 data-[state=active]:text-white data-[state=active]:shadow-[0_-2px_0_0_theme(colors.emerald.500)_inset]"
                                    >
                                        <SlidersIcon className="w-4 h-4 mr-2"/>
                                        Settings
                                    </TabsTrigger>
                                </TabsList>
                            </div>


                            <TabsContent value="credentials" className="p-6 space-y-6">
                                <div className="bg-gray-700/50 p-4 rounded-lg border border-gray-600">
                                    <div className="flex items-center justify-between mb-2">
                                        <Label className="text-gray-300 flex items-center gap-2">
                                            <KeyIcon className="w-4 h-4"/>
                                            Client ID
                                        </Label>
                                        <Tooltip>
                                            <TooltipTrigger asChild>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    className="w-8 h-8 hover:bg-gray-600"
                                                    onClick={() => copyToClipboard(currentApp.clientId)}
                                                >
                                                    <CopyIcon className="w-3 h-3"/>
                                                    <span className="sr-only">Copy</span>
                                                </Button>
                                            </TooltipTrigger>
                                            <TooltipContent>Copy to clipboard</TooltipContent>
                                        </Tooltip>
                                    </div>
                                    <code className="block bg-gray-800 px-3 py-2 rounded text-sm font-mono break-all">
                                        {currentApp.clientId}
                                    </code>
                                </div>

                                <div className="bg-gray-700/50 p-4 rounded-lg border border-gray-600">
                                    <div className="flex items-center justify-between mb-2">
                                        <Label className="text-gray-300 flex items-center gap-2">
                                            <LockIcon className="w-4 h-4"/>
                                            Client Secret
                                        </Label>
                                        <div className="flex gap-1">
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        className="w-8 h-8 hover:bg-gray-600"
                                                        onClick={() => setShowSecret(!showSecret)}
                                                    >
                                                        {showSecret ? (
                                                            <EyeOff className="w-3 h-3"/>
                                                        ) : (
                                                            <Eye className="w-3 h-3"/>
                                                        )}
                                                        <span className="sr-only">
              {showSecret ? "Hide" : "Show"} secret
            </span>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    {showSecret ? "Hide secret" : "Show secret"}
                                                </TooltipContent>
                                            </Tooltip>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        className="w-8 h-8 hover:bg-gray-600"
                                                        onClick={() => copyToClipboard(currentApp.clientSecret)}
                                                    >
                                                        <CopyIcon className="w-3 h-3"/>
                                                        <span className="sr-only">Copy</span>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>Copy to clipboard</TooltipContent>
                                            </Tooltip>
                                            {isEditing && (
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            className="w-8 h-8 hover:bg-gray-600"
                                                            onClick={regenerateSecret}
                                                            disabled={isRegeneratingSecret}
                                                        >
                                                            {isRegeneratingSecret ? (
                                                                <RefreshCwIcon className="w-3 h-3 animate-spin"/>
                                                            ) : (
                                                                <RefreshCwIcon className="w-3 h-3"/>
                                                            )}
                                                            <span className="sr-only">Regenerate</span>
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Regenerate secret</TooltipContent>
                                                </Tooltip>
                                            )}
                                        </div>
                                    </div>
                                    <code className="block bg-gray-800 px-3 py-2 rounded text-sm font-mono break-all">
                                        {showSecret ? currentApp.clientSecret : '••••••••••••••••••••••••••••••••'}
                                    </code>
                                    <p className="mt-2 text-xs text-gray-400 italic">
                                        Do not share this secret with anyone
                                    </p>
                                </div>
                            </TabsContent>

                            <TabsContent value="settings" className="p-6 space-y-8">
                                <div className="space-y-6">
                                    <div className="bg-gray-700/50 p-5 rounded-lg border border-gray-600">
                                        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                                            <GlobeIcon className="w-4 h-4"/>
                                            Redirect URIs
                                        </h3>
                                        <DataWrapper state={{...baseState}} wrapper={"redirectUri"}/>
                                    </div>

                                    <div className="bg-gray-700/50 p-5 rounded-lg border border-gray-600">
                                        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                                            <ShieldIcon className="w-4 h-4"/>
                                            Permissions
                                        </h3>
                                        <div className="grid gap-6 md:grid-cols-2">
                                            <div className="space-y-4">
                                                <h4 className="font-medium text-gray-300">Scopes</h4>
                                                <DataWrapper state={{...baseState}} wrapper={"scope"}/>
                                            </div>
                                            <div className="space-y-4">
                                                <h4 className="font-medium text-gray-300">Grant Types</h4>
                                                <DataWrapper state={{...baseState}} wrapper={"grantType"}/>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="bg-gray-700/50 p-5 rounded-lg border border-gray-600">
                                        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                                            <CodeIcon className="w-4 h-4"/>
                                            Response Configuration
                                        </h3>
                                        <div className="grid gap-6 md:grid-cols-2">
                                            <div className="space-y-4">
                                            <Label
                                                    className="flex items-center gap-2">
                                                    Response Types
                                                    <Tooltip>
                                                        <TooltipTrigger asChild>
                                                            <Info className="w-4 h-4 text-gray-400"/>
                                                        </TooltipTrigger>
                                                        <TooltipContent className="bg-gray-800 text-white">
                                                            The response the token endpoint returns.
                                                            Can be code, id_token or both
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </Label>
                                                <DataWrapper state={{...baseState}} wrapper={"responseType"}/>
                                            </div>
                                            <div className="space-y-4">
                                                <Label
                                                className="flex items-center gap-2">
                                                Token Endpoint Auth Method
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Info className="w-4 h-4 text-gray-400"/>
                                                    </TooltipTrigger>
                                                    <TooltipContent className="bg-gray-800 text-white">
                                                        The authentication method used at the token endpoint.
                                                    </TooltipContent>
                                                </Tooltip>
                                            </Label>

                                                <Badge
                                                    variant="outline"
                                                    className="bg-gray-700 border-gray-600 hover:bg-gray-600 px-3 py-1"
                                                >
                                                    {app.tokenEndpointAuthMethod}
                                                </Badge>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="bg-gray-700/50 p-5 rounded-lg border border-gray-600">
                                        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                                            <CodeIcon className="w-4 h-4"/>
                                            Duster
                                        </h3>
                                        <div className="grid gap-6 md:grid-cols-2">
                                            <div className="space-y-4">
                                                <h4 className="font-medium text-gray-300">Callback URL</h4>
                                                <DataWrapper state={{...baseState,onChange:handleChange}} wrapper={"dusterCallback"}/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </TabsContent>
                        </Tabs>
                    </CardContent>

                    <CardFooter
                        className="flex flex-col sm:flex-row justify-end gap-3 p-6 border-t border-gray-700 bg-gray-800/50">
                        {isEditing ? (
                            <>
                                <Button
                                    variant="outline"
                                    onClick={handleCancel}
                                    className="text-gray-300 border-gray-600 hover:bg-gray-700 w-full sm:w-auto"
                                >
                                    <XIcon className="w-4 h-4 mr-2"/>
                                    Cancel
                                </Button>
                                <Button
                                    onClick={handleSave}
                                    className="bg-emerald-600 hover:bg-emerald-500 w-full sm:w-auto"
                                >
                                    <CheckIcon className="w-4 h-4 mr-2"/>
                                    Save Changes
                                </Button>
                            </>
                        ) : (
                            <>
                                <Button
                                    variant="outline"
                                    onClick={handleEdit}
                                    className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10 w-full sm:w-auto"
                                >
                                    <EditIcon className="w-4 h-4 mr-2"/>
                                    Edit Application
                                </Button>
                                <Button
                                    variant="destructive"
                                    onClick={deleteApp}
                                    className="hover:bg-red-500/90 w-full sm:w-auto"
                                >
                                    <TrashIcon className="w-4 h-4 mr-2"/>
                                    Delete Application
                                </Button>
                            </>
                        )}
                    </CardFooter>
                </Card>
            </div>
    );
}