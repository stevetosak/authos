import React, {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Tooltip, TooltipTrigger, TooltipContent} from "@/components/ui/tooltip";
import {Check, Cpu, Globe, Info, Key, Reply, Shield, LockIcon, HelpCircle, XIcon} from "lucide-react";
import RedirectUriFormInput from "@/Pages/ClientRegistrationPage/components/RedirectUriFormInput.tsx";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import MultiSelectBadge from "@/Pages/components/MultiSelectBadge.tsx";
import {motion} from "framer-motion";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar.tsx";
import {useNavigate} from "react-router-dom";
import {apiPostAuthenticated} from "@/services/netconfig.ts";
import {toast} from "sonner";
import {useAuth} from "@/services/useAuth.ts";
import {App} from "@/services/types.ts";
import {SettingsSectionCard} from "@/Pages/AppDetailsPage/components/SettingsSectionCard.tsx";

export default function RegisterAppPage() {
    const [formData, setFormData] = useState({
        appName: "",
        appIconUrl: "",
        shortDescription: "",
        tokenEndpointAuthMethod: "client_secret_basic",
        grantTypes: ["authorization_code"],
        responseTypes: ["code"],
        appInfoUri: "",
    });
    const {groups,setApps} = useAuth()

    const [selectedScopes, setSelectedScopes] = useState<string[]>(["openid"])
    const [redirectUris, setRedirectUris] = useState<string[]>([])
    const nav = useNavigate()

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleArrayChange = (field: string, value: string[]) => {
        setFormData({...formData, [field]: value});
    };

    const registerApp = async () => {
        const selectedGroupId = new URLSearchParams(location.search).get("group")
        let group = null;
        if(selectedGroupId != null){
          group = groups.find(gr => gr.id === Number(selectedGroupId))
            if(group == null) nav("/dashboard")
        }
        const data = {
            ...formData,
            redirectUris: redirectUris,
            scope: selectedScopes,
            group: selectedGroupId
        }
        console.log("REDIRECT URIS: " + data.redirectUris)

        if (formData.appName.trim() == "") throw Error("app name empty")
        if (formData.grantTypes.length == 0) throw Error("you need to add a grant type")
        if (redirectUris.length == 0) throw Error("At least one redirect uri must be present")


        try {
            const resp = await apiPostAuthenticated<App>("/app/register", data)
            setApps(prev => ([...prev,resp.data]))
            toast.success(`Successfully registered app: ${formData.appName}`)
            setTimeout(() => {
                nav("/dashboard")
            }, 300)


        } catch (err) {
            console.error(err)
        }
    }


    return (
        <div className="min-h-screen text-gray-100 p-4 md:p-10 font-sans w-full">
            <motion.div
                initial={{opacity: 0, scale: 0.98}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.2}}
                className="max-w-5xl mx-auto w-full"
            >
                <Card className="border border-gray-700 shadow-xl rounded-xl overflow-hidden w-full">
                    <CardHeader className="border-b border-gray-700 p-6">
                        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                            <div className="flex items-center gap-3">
                                <span className="p-2 rounded-md bg-gray-700/50">
                                    <Key className="w-5 h-5"/>
                                </span>
                                <div>
                                    <CardTitle className="text-2xl font-bold">
                                        Register OAuth Application
                                    </CardTitle>
                                    <CardDescription className="mt-1">
                                        Configure your application's authentication settings
                                    </CardDescription>
                                </div>
                            </div>
                            <Button variant="outline" size="sm" className="border-gray-600 hover:bg-gray-700/50">
                                <HelpCircle className="w-4 h-4 mr-2"/>
                                Guide
                            </Button>
                        </div>
                    </CardHeader>

                    <CardContent className="p-6 space-y-6">
                        <SettingsSectionCard
                            icon={<Info className="w-4 h-4"/>}
                            title="Basic Information"
                            description="Public details shown to users on the consent screen"
                        >
                            <div className="space-y-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label className="flex items-center gap-2">
                                            Application Name
                                            <Tooltip delayDuration={100}>
                                                <TooltipTrigger asChild>
                                                    <Info className="w-4 h-4 text-gray-400"/>
                                                </TooltipTrigger>
                                                <TooltipContent className="bg-gray-800 text-white">
                                                    The public-facing name of your application
                                                </TooltipContent>
                                            </Tooltip>
                                        </Label>
                                        <Input
                                            name="appName"
                                            value={formData.appName}
                                            onChange={handleChange}
                                            placeholder="My Awesome App"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label className="flex items-center gap-2">
                                            Application Icon URL
                                            <Tooltip delayDuration={100}>
                                                <TooltipTrigger asChild>
                                                    <Info className="w-4 h-4 text-gray-400"/>
                                                </TooltipTrigger>
                                                <TooltipContent className="bg-gray-800 text-white">
                                                    HTTPS URL to a square image (512×512 recommended)
                                                </TooltipContent>
                                            </Tooltip>
                                        </Label>
                                        <div className="flex gap-2">
                                            <Input
                                                name="appIconUrl"
                                                value={formData.appIconUrl}
                                                onChange={handleChange}
                                                placeholder="https://example.com/logo.png"
                                            />
                                            {formData.appIconUrl && (
                                                <Avatar className="h-9 w-9 border border-gray-600">
                                                    <AvatarImage src={formData.appIconUrl}/>
                                                    <AvatarFallback className="bg-gray-700">
                                                        {formData.appName?.[0] || "A"}
                                                    </AvatarFallback>
                                                </Avatar>
                                            )}
                                        </div>
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label className="flex items-center gap-2">
                                        Description
                                        <Tooltip delayDuration={100}>
                                            <TooltipTrigger asChild>
                                                <Info className="w-4 h-4 text-gray-400"/>
                                            </TooltipTrigger>
                                            <TooltipContent className="bg-gray-800 text-white">
                                                Brief explanation of your application's purpose
                                            </TooltipContent>
                                        </Tooltip>
                                    </Label>
                                    <Textarea
                                        name="shortDescription"
                                        value={formData.shortDescription}
                                        onChange={handleChange}
                                        className="min-h-[100px]"
                                        placeholder="Describe what your application does..."
                                    />
                                </div>
                            </div>
                        </SettingsSectionCard>

                        <SettingsSectionCard
                            icon={<LockIcon className="w-4 h-4"/>}
                            title="OAuth Configuration"
                            description="Endpoints, grants and scopes this application can use"
                        >
                            <div className="space-y-6">
                                <div className="space-y-2">
                                    <Label className="flex items-center gap-2">
                                        Redirect URIs
                                        <Tooltip delayDuration={100}>
                                            <TooltipTrigger asChild>
                                                <Info className="w-4 h-4 text-gray-400"/>
                                            </TooltipTrigger>
                                            <TooltipContent className="bg-gray-800 text-white">
                                                Approved callback locations (exact match required)
                                            </TooltipContent>
                                        </Tooltip>
                                    </Label>
                                    <RedirectUriFormInput
                                        redirectUris={redirectUris}
                                        setRedirectUris={setRedirectUris}
                                        className="bg-gray-700/20 border border-gray-600 p-3 rounded-md"
                                    />
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                    <div className="space-y-3">
                                        <h4 className="font-medium text-gray-300 flex items-center gap-2">
                                            <Shield className="w-4 h-4"/>
                                            Grant Types
                                        </h4>
                                        <MultiSelectBadge
                                            label={""}
                                            selected={formData.grantTypes}
                                            setSelected={(values) => handleArrayChange("grantTypes", values)}
                                            options={[
                                                "authorization_code",
                                                "client_credentials",
                                                "refresh_token",
                                                "password",
                                                "implicit"
                                            ]}
                                        />
                                    </div>

                                    <div className="space-y-3">
                                        <h4 className="font-medium text-gray-300 flex items-center gap-2">
                                            <Reply className="w-4 h-4"/>
                                            Response Types
                                        </h4>
                                        <MultiSelectBadge
                                            label={""}
                                            selected={formData.responseTypes}
                                            setSelected={(values) => handleArrayChange("responseTypes", values)}
                                            options={["code", "token", "id_token"]}
                                        />
                                    </div>

                                    <div className="space-y-3">
                                        <h4 className="font-medium text-gray-300 flex items-center gap-2">
                                            <Key className="w-4 h-4"/>
                                            OAuth Scopes
                                        </h4>
                                        <MultiSelectBadge
                                            label={""}
                                            selected={selectedScopes}
                                            setSelected={setSelectedScopes}
                                            options={["openid", "profile", "email", "offline_access"]}
                                            disabledItems={["openid"]}
                                        />
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label className="flex items-center gap-2">
                                            <Cpu className="w-4 h-4"/>
                                            Token Endpoint Auth
                                        </Label>
                                        <Select
                                            value={formData.tokenEndpointAuthMethod}
                                            onValueChange={(value) => setFormData({
                                                ...formData,
                                                tokenEndpointAuthMethod: value
                                            })}
                                        >
                                            <SelectTrigger className="w-full">
                                                <SelectValue placeholder="Select method"/>
                                            </SelectTrigger>
                                            <SelectContent className="bg-gray-800 border-gray-700">
                                                <SelectItem value="client_secret_basic">
                                                    Client Secret Basic
                                                </SelectItem>
                                                <SelectItem value="client_secret_post">
                                                    Client Secret Post
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label className="flex items-center gap-2">
                                            <Globe className="w-4 h-4"/>
                                            App Info URL
                                        </Label>
                                        <Input
                                            name="appInfoUri"
                                            value={formData.appInfoUri}
                                            onChange={handleChange}
                                            placeholder="https://example.com/about"
                                        />
                                    </div>
                                </div>
                            </div>
                        </SettingsSectionCard>
                    </CardContent>

                    <CardFooter className="flex flex-col sm:flex-row justify-end gap-3 p-6 border-t border-gray-700">
                        <Button
                            variant="secondary"
                            onClick={() => nav("/dashboard")}
                            className="text-gray-300 border-gray-600 hover:bg-gray-700 w-full sm:w-auto"
                        >
                            <XIcon className="w-4 h-4 mr-2"/>
                            Cancel
                        </Button>
                        <Button
                            onClick={registerApp}
                            className="w-full sm:w-auto"
                        >
                            <Check className="w-4 h-4 mr-2"/>
                            Register Application
                        </Button>
                    </CardFooter>
                </Card>
            </motion.div>
        </div>
    );

}