import {useState, useEffect, useRef} from "react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Switch} from "@/components/ui/switch";
import {Label} from "@/components/ui/label";
import {Card, CardHeader, CardTitle, CardContent, CardFooter} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {
    BookOpen,
    Link,
    RefreshCw,
    Settings,
    Trash2,
    Check,
    X,
    ChevronRight,
    Shield,
    Settings2,
    Key,
    Copy, Wrench,
} from "lucide-react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {defaultDusterApp, DusterApp} from "@/services/types.ts";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/netconfig.ts";

export const DusterClientPage = () => {
    const [clientUrl, setClientUrl] = useState("");
    const [healthStatus, setHealthStatus] = useState<"unknown" | "checking" | "healthy" | "unhealthy">("unknown");
    const [isConfigured, setIsConfigured] = useState(false)
    const [settings, setSettings] = useState({
        callbackUrl: "",
        mode: "auto" as "auto" | "fresh"
    });
    const [linkedApps, setLinkedApps] = useState<Array<{
        id: string;
        name: string;
        lastSynced: string;
        status: "active" | "inactive";
    }>>([]);
    const [isEditing, setIsEditing] = useState(false);

    const [dusterApp, setDusterApp] = useState<DusterApp>(defaultDusterApp)
    const dusterAppStackRef: React.RefObject<DusterApp[]> = useRef<DusterApp[]>([])



    const loadApp = async () => {
        try{
            const resp = await apiGetAuthenticated<DusterApp>("/duster/app")
            setDusterApp(resp.data)
            setIsConfigured(true)
        } catch (err){
            console.error(err)
        }

    }

    useEffect(() => {
        loadApp()
    },[])


    const createDusterClient = async () => {

        try {
            const resp = await apiPostAuthenticated<DusterApp>(`/duster/create`)
            setDusterApp(resp.data)
            setIsConfigured(true)
        } catch (e) {
            console.error(e)
        }
    };

    const copyToClipboard = (text: string) => {
        navigator.clipboard.writeText(text);
    };

    const handleEdit = () => {
        dusterAppStackRef.current.push({...dusterApp})
        setIsEditing(true)
    }
    const handleCancel = () => {
        const initialState = dusterAppStackRef.current.pop()
        if (initialState == undefined) return
        setDusterApp(initialState)
        setIsEditing(false)
    }
    const checkHealth = async () => {
        if (!clientUrl) return;

        setHealthStatus("checking");
        try {
            const response = await fetch(`${clientUrl}/health`);
            if (response.ok) {
                setHealthStatus("healthy");
            } else {
                setHealthStatus("unhealthy");
            }
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (err) {
            setHealthStatus("unhealthy");
        }
    };

    const saveSettings = () => {
        console.log("Saving settings:", settings);
        setIsEditing(false);
    };

    const unlinkApp = (appId: string) => {
        setLinkedApps(linkedApps.filter(app => app.id !== appId));
    };


    return (
        <div className="min-h-screen text-white p-4 md:p-8 w-full">
            <div className="max-w-6xl mx-auto">
                {!isConfigured && (
                    <Card className="bg-gray-800/50 border-2 border-dashed border-gray-600/50 backdrop-blur-sm mb-6">
                        <CardHeader className="text-center">
                            <div className="mx-auto flex flex-col items-center justify-center p-8">
                                <div className="mb-6 rounded-full bg-gray-700/50 p-6 border border-gray-600/50">
                                    <Settings2 className="h-12 w-12 text-gray-400" strokeWidth={1.5}/>
                                </div>

                                <CardTitle className="text-2xl font-medium text-white mb-2">
                                    Duster Client Not Enabled
                                </CardTitle>

                                <p className="text-gray-400 max-w-md mb-6">
                                    Enabling your duster client generates credentials for it, just like any other OAuth app.
                                </p>

                                <div className="space-y-4 mt-4 w-full">
                                    <div className="flex justify-center gap-3">
                                        <Button
                                            variant={"outline"}
                                            onClick={createDusterClient}
                                            className="flex items-center gap-2 justify-start bg-gray-800/50 border-emerald-400/30 hover:bg-emerald-400/10 text-xl w-1/2 text-emerald-500"
                                        >
                                            <Wrench className={"w-10 h-10"}></Wrench>
                                            <span className={"flex-1"}>
                                                  Enable
                                            </span>

                                        </Button>
                                    </div>
                                </div>


                                {/*<Dialog open={registrationModalOpen} onOpenChange={setRegistrationModalOpen}>*/}
                                {/*    <DialogTrigger asChild>*/}
                                {/*        <Button className="bg-emerald-600 hover:bg-emerald-500 px-6 py-3 text-lg">*/}
                                {/*            <Plus className="w-5 h-5 mr-2"/>*/}
                                {/*            Set Up Duster Client*/}
                                {/*        </Button>*/}
                                {/*    </DialogTrigger>*/}
                                {/*    <DialogContent className="bg-gray-800 border border-gray-700 max-w-md">*/}
                                {/*        <DialogHeader>*/}
                                {/*            <DialogTitle className="flex items-center gap-2">*/}
                                {/*                <Key className="w-5 h-5 text-emerald-400"/>*/}
                                {/*                Register New Client*/}
                                {/*            </DialogTitle>*/}
                                {/*        </DialogHeader>*/}

                                {/*    </DialogContent>*/}
                                {/*</Dialog>*/}
                            </div>
                        </CardHeader>
                    </Card>)}

                {isConfigured && (<>
                    <div className="flex justify-between items-center mb-8">
                        <h1 className="text-3xl font-bold flex items-center gap-3">
                            <Settings2 className="w-6 h-6 text-emerald-400"/>
                            Duster Client
                        </h1>
                        <Button
                            asChild
                            variant="outline"
                            className="border-emerald-400 text-emerald-400 hover:bg-emerald-400/10"
                        >
                            <a href="https://docs.authos.com/duster-setup" target="_blank" rel="noopener noreferrer">
                                <BookOpen className="w-4 h-4 mr-2"/>
                                Setup Guide
                            </a>
                        </Button>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                        <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm lg:col-span-2">
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Link className="w-5 h-5 text-emerald-400"/>
                                    Client Connection
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    <div>
                                        <Label htmlFor="client-url">Duster Client URL</Label>
                                        <Input
                                            id="client-url"
                                            value={clientUrl}
                                            onChange={(e) => setClientUrl(e.target.value)}
                                            placeholder="https://your-duster-client.example.com"
                                            className="bg-gray-700 border-gray-600 mt-2"
                                        />
                                    </div>

                                    <div className="flex items-center gap-4">
                                        <Button
                                            onClick={checkHealth}
                                            disabled={!clientUrl || healthStatus === "checking"}
                                            className="bg-emerald-600 hover:bg-emerald-500"
                                        >
                                            {healthStatus === "checking" ? (
                                                <RefreshCw className="w-4 h-4 mr-2 animate-spin"/>
                                            ) : (
                                                <RefreshCw className="w-4 h-4 mr-2"/>
                                            )}
                                            Check Health
                                        </Button>

                                        <div className="flex items-center gap-2">
                                            <div className={`w-3 h-3 rounded-full ${
                                                healthStatus === "healthy" ? "bg-emerald-400" :
                                                    healthStatus === "unhealthy" ? "bg-red-400" :
                                                        healthStatus === "checking" ? "bg-yellow-400" : "bg-gray-400"
                                            }`}/>
                                            <span className="text-sm">
                      {healthStatus === "healthy" ? "Connected" :
                          healthStatus === "unhealthy" ? "Connection failed" :
                              healthStatus === "checking" ? "Checking..." : "Not checked"}
                    </span>
                                        </div>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>

                        <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Shield className="w-5 h-5 text-emerald-400"/>
                                    Connection Status
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="flex flex-col items-center justify-center h-full">
                                    <div className={`p-4 rounded-full mb-3 ${
                                        healthStatus === "healthy" ? "bg-emerald-500/10" :
                                            healthStatus === "unhealthy" ? "bg-red-500/10" : "bg-gray-500/10"
                                    }`}>
                                        {healthStatus === "healthy" ? (
                                            <Check className="w-8 h-8 text-emerald-400"/>
                                        ) : healthStatus === "unhealthy" ? (
                                            <X className="w-8 h-8 text-red-400"/>
                                        ) : (
                                            <Settings className="w-8 h-8 text-gray-400"/>
                                        )}
                                    </div>
                                    <p className="text-center text-sm text-gray-300">
                                        {healthStatus === "healthy" ? "Your Duster client is connected and healthy" :
                                            healthStatus === "unhealthy" ? "Could not connect to Duster client" :
                                                "Health status unknown"}
                                    </p>
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm mb-8">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Settings className="w-5 h-5 text-emerald-400"/>
                                Client Settings
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-6">
                                    <div>
                                        <Label>Token Mode</Label>
                                        <div className="flex items-center gap-4 mt-2">
                                            <div className="flex items-center gap-2">
                                                <Switch
                                                    id="token-mode"
                                                    checked={settings.mode === "auto"}
                                                    onCheckedChange={(checked) => setSettings({
                                                        ...settings,
                                                        mode: checked ? "auto" : "fresh"
                                                    })}
                                                    className="data-[state=checked]:bg-emerald-500"
                                                />
                                                <span className="text-sm">
                                                {settings.mode === "auto" ? "Auto (refresh tokens)" : "Fresh (re-auth each time)"}
                                            </span>
                                            </div>
                                            <Badge variant="outline" className="border-gray-600">
                                                {settings.mode === "auto" ? "Recommended" : "More secure"}
                                            </Badge>
                                        </div>
                                        <p className="text-xs text-gray-400 mt-2">
                                            {settings.mode === "auto"
                                                ? "Uses refresh tokens for seamless re-authentication"
                                                : "Requires full authorization flow each time"}
                                        </p>
                                    </div>
                                </div>
                                
                                    <div className="space-y-4">
                                        <div>
                                            <Label className="flex items-center gap-2 mb-2">
                                                <Key className="w-4 h-4"/>
                                                Client ID
                                            </Label>
                                            <div className="flex items-center gap-2">
                                                <Input
                                                    value={dusterApp.clientId}
                                                    readOnly
                                                    className="bg-gray-700 border-gray-600"
                                                />
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => copyToClipboard(dusterApp.clientId)}
                                                        >
                                                            <Copy className="w-4 h-4"/>
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Copy to clipboard</TooltipContent>
                                                </Tooltip>
                                            </div>
                                        </div>

                                        <div>
                                            <Label className="flex items-center gap-2 mb-2">
                                                <Key className="w-4 h-4"/>
                                                Client Secret
                                            </Label>
                                            <div className="flex items-center gap-2">
                                                <Input
                                                    value={dusterApp.clientSecret}
                                                    readOnly
                                                    type="password"
                                                    className="bg-gray-700 border-gray-600"
                                                />
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => copyToClipboard(dusterApp.clientSecret)}
                                                        >
                                                            <Copy className="w-4 h-4"/>
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Copy to clipboard</TooltipContent>
                                                </Tooltip>
                                            </div>
                                            <p className="text-xs text-gray-400 mt-2">
                                                ⚠️ Save this secret securely - it won't be shown again
                                            </p>
                                        </div>
                                    </div>

                            </div>
                        </CardContent>
                        <CardFooter className="flex justify-end space-x-2">

                            {isEditing && (<Button
                                onClick={saveSettings}
                                className="bg-emerald-600 hover:bg-emerald-500"
                            >
                                Save Settings
                            </Button>)}

                            {isEditing ? (
                                <Button
                                    onClick={() => handleCancel()}
                                    variant={"outline"}
                                    className="border-gray-500 text-white hover:bg-gray-600/20 w-full sm:w-auto"
                                >
                                    <Trash2 className="w-4 h-4 mr-2"/>
                                    Cancel
                                </Button>
                            ) : (
                                <Button
                                    variant="outline"
                                    onClick={() => handleEdit()}
                                    className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10 w-full sm:w-auto"
                                >
                                    Edit
                                </Button>
                            )}

                        </CardFooter>
                    </Card>
                    <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Link className="w-5 h-5 text-emerald-400"/>
                                Linked Applications
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            {linkedApps.length > 0 ? (
                                <div className="space-y-3">
                                    {linkedApps.map((app) => (
                                        <div
                                            key={app.id}
                                            className="flex items-center justify-between p-4 bg-gray-700/30 rounded-lg border border-gray-600/50"
                                        >
                                            <div className="flex items-center gap-4">
                                                <div className={`w-3 h-3 rounded-full ${
                                                    app.status === "active" ? "bg-emerald-400" : "bg-gray-400"
                                                }`}/>
                                                <div>
                                                    <p className="font-medium">{app.name}</p>
                                                    <p className="text-sm text-gray-400">
                                                        Last synced: {app.lastSynced || "Never"}
                                                    </p>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    className="border-gray-600"
                                                    onClick={() => unlinkApp(app.id)}
                                                >
                                                    <Trash2 className="w-4 h-4 mr-2"/>
                                                    Unlink
                                                </Button>
                                                <ChevronRight className="w-5 h-5 text-gray-400"/>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-8">
                                    <p className="text-gray-400 mb-4">No applications linked to Duster yet</p>
                                    <Button variant="outline" className="border-emerald-400 text-emerald-400">
                                        Link Application
                                    </Button>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </>)}
            </div>
        </div>
    );
};

export default DusterClientPage;