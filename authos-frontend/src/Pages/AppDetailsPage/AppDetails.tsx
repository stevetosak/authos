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
import {apiPostAuthenticated} from "@/services/netconfig.ts";
import {useNavigate} from "react-router-dom";
import {CredentialsTab} from "@/Pages/AppDetailsPage/components/CredentialsTab.tsx";
import {SettingsTab} from "@/Pages/AppDetailsPage/components/SettingsTab.tsx";


export default function AppDetails() {
    const {apps,setApps} = useAuth()
    const {appId} = useParams();
    const [app, setApp] = useState<App>(Factory.appDefault());
    const nav = useNavigate();

    const [isEditing, setIsEditing] = useState(false);


    const {
        editedApp,
        setEditedApp,
        inputValues,
        handleInputChange,
        handleChange,
        addToArrayField,
        removeFromArrayField
    } = useAppEditor(app);

    const [isRegeneratingSecret, setIsRegeneratingSecret] = useState(false);
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
                className="min-h-screen lg:min-w-4xl text-gray-100 p-4 md:p-10 font-sans">
                <Card className="border border-gray-700 shadow-xl rounded-xl overflow-hidden max-w-4xl mx-auto">
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
                                <TabsList className="grid grid-cols-3 rounded-none h-fit">
                                    <TabsTrigger
                                        value="credentials"
                                        className="p-4 data-[state=active]:bg-gray-700/30 text-gray-100 data-[state=active]:shadow-[0_-2px_0_0_theme(colors.teal.300)_inset]"
                                    >
                                        <KeyIcon className="w-4 h-4 mr-2"/>
                                        Credentials
                                    </TabsTrigger>
                                    <TabsTrigger
                                        value="settings"
                                        className="p-4 data-[state=active]:bg-gray-700/30 text-gray-100 data-[state=active]:shadow-[0_-2px_0_0_theme(colors.teal.300)_inset]"
                                    >
                                        <SlidersIcon className="w-4 h-4 mr-2"/>
                                        Settings
                                    </TabsTrigger>
                                </TabsList>
                            </div>

                            <CredentialsTab app={currentApp} isEditing={isEditing} regenerateSecret={regenerateSecret} isRegeneratingSecret={isRegeneratingSecret}/>
                            <SettingsTab baseState={baseState} app={app} handleChange={handleChange}/>


                        </Tabs>
                    </CardContent>

                    <CardFooter
                        className="flex flex-col sm:flex-row justify-end gap-3 p-6 border-t border-gray-700 bg-gray-800/50">
                        {isEditing ? (
                            <>
                                <Button
                                    variant="secondary"
                                    onClick={handleCancel}
                                    className="text-gray-300 border-gray-600 hover:bg-gray-700 w-full sm:w-auto"
                                >
                                    <XIcon className="w-4 h-4 mr-2"/>
                                    Cancel
                                </Button>
                                <Button
                                    onClick={handleSave}
                                    className="w-full sm:w-auto"
                                >
                                    <CheckIcon className="w-4 h-4 mr-2"/>
                                    Save Changes
                                </Button>
                            </>
                        ) : (
                            <>
                                <Button
                                    variant="default"
                                    onClick={handleEdit}
                                    className=" w-full sm:w-auto"
                                >
                                    <EditIcon className="w-4 h-4 mr-2"/>
                                    Edit Application
                                </Button>
                                <Button
                                    variant="destructive"
                                    onClick={deleteApp}
                                    className=" w-full sm:w-auto"
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