import React, {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Tooltip, TooltipTrigger, TooltipContent} from "@/components/ui/tooltip";
import {Check, Cpu, Globe, Info, Key, Reply, Shield, LockIcon, HelpCircle} from "lucide-react";
import RedirectUriFormInput from "@/Pages/ClientRegistrationPage/components/RedirectUriFormInput.tsx";
import axios from "axios";
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
import Layout from "@/Pages/components/Layout.tsx";
import {useNavigate} from "react-router-dom";
import {api, apiPostAuthenticated} from "@/services/config.ts";
import {toast} from "sonner";
import {useAuth} from "@/services/useAuth.ts";
import {App} from "@/services/types.ts";

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
    const {refreshAuth,groups,setApps} = useAuth()

    const [selectedScopes, setSelectedScopes] = useState<string[]>(["openid"])
    const [redirectUris, setRedirectUris] = useState<string[]>([])
    const nav = useNavigate()

    const handleChange = (e) => {
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
        <div className="inset-0 z-50 flex items-center justify-center my-5">
            <motion.div
                initial={{opacity: 0, scale: 0.95}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.2}}
                className="w-full max-w-4xl max-h-[90vh] overflow-y-auto"
            >
                <Card
                    className="bg-gray-800/70 backdrop-blur-md border border-gray-700/50 shadow-xl overflow-hidden">
                    <CardHeader
                        className="border-b border-gray-700/50 bg-gradient-to-r from-gray-800/50 to-green-900/10">
                        <div className="flex items-center justify-between p-4">
                            <div>
                                <CardTitle className="text-2xl font-bold text-green-400 flex items-center gap-2">
                                    <Key className="w-6 h-6"/>
                                    Register OAuth Application
                                </CardTitle>
                                <CardDescription className="text-gray-400 mt-1">
                                    Configure your application's authentication settings
                                </CardDescription>
                            </div>
                            <div className="flex gap-2">
                                <Button variant="outline" size="sm"
                                        className="border-gray-600 hover:bg-gray-700/50">
                                    <HelpCircle className="w-4 h-4 mr-2"/>
                                    Guide
                                </Button>
                            </div>
                        </div>
                    </CardHeader>

                    <CardContent className="p-6 space-y-6">
                        <div className="space-y-4">
                            <h3 className="text-lg font-semibold text-white flex items-center gap-2">
                                <Info className="w-5 h-5 text-green-400"/>
                                Basic Information
                            </h3>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                        Application Name
                                        <Tooltip delayDuration={100}>
                                            <TooltipTrigger asChild>
                                                <Info
                                                    className="w-4 h-4 text-gray-400 hover:text-green-400 transition-colors"/>
                                            </TooltipTrigger>
                                            <TooltipContent className="bg-gray-800 border border-gray-700 text-sm">
                                                The public-facing name of your application
                                            </TooltipContent>
                                        </Tooltip>
                                    </Label>
                                    <Input
                                        name="appName"
                                        value={formData.appName}
                                        onChange={handleChange}
                                        className="bg-gray-700/50 border-gray-600 focus:border-green-400/50 focus:ring-1 focus:ring-green-400/30"
                                        placeholder="My Awesome App"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                        Application Icon URL
                                        <Tooltip delayDuration={100}>
                                            <TooltipTrigger asChild>
                                                <Info
                                                    className="w-4 h-4 text-gray-400 hover:text-green-400 transition-colors"/>
                                            </TooltipTrigger>
                                            <TooltipContent className="bg-gray-800 border border-gray-700 text-sm">
                                                HTTPS URL to a square image (512×512 recommended)
                                            </TooltipContent>
                                        </Tooltip>
                                    </Label>
                                    <div className="flex gap-2">
                                        <Input
                                            name="appIconUrl"
                                            value={formData.appIconUrl}
                                            onChange={handleChange}
                                            className="bg-gray-700/50 border-gray-600 focus:border-green-400/50 focus:ring-1 focus:ring-green-400/30"
                                            placeholder="https://example.com/logo.png"
                                        />
                                        {formData.appIconUrl && (
                                            <Avatar className="h-10 w-10 border border-gray-600">
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
                                <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                    Description
                                    <Tooltip delayDuration={100}>
                                        <TooltipTrigger asChild>
                                            <Info
                                                className="w-4 h-4 text-gray-400 hover:text-green-400 transition-colors"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 border border-gray-700 text-sm">
                                            Brief explanation of your application's purpose
                                        </TooltipContent>
                                    </Tooltip>
                                </Label>
                                <Textarea
                                    name="shortDescription"
                                    value={formData.shortDescription}
                                    onChange={handleChange}
                                    className="bg-gray-700/50 border-gray-600 focus:border-green-400/50 focus:ring-1 focus:ring-green-400/30 min-h-[100px]"
                                    placeholder="Describe what your application does..."
                                />
                            </div>
                        </div>

                        <div className="space-y-4">
                            <h3 className="text-lg font-semibold text-white flex items-center gap-2">
                                <LockIcon className="w-5 h-5 text-green-400"/>
                                OAuth Configuration
                            </h3>

                            <div className="space-y-2">
                                <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                    Redirect URIs
                                    <Tooltip delayDuration={100}>
                                        <TooltipTrigger asChild>
                                            <Info
                                                className="w-4 h-4 text-gray-400 hover:text-green-400 transition-colors"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 border border-gray-700 text-sm">
                                            Approved callback locations (exact match required)
                                        </TooltipContent>
                                    </Tooltip>
                                </Label>
                                <RedirectUriFormInput
                                    redirectUris={redirectUris}
                                    setRedirectUris={setRedirectUris}
                                    className="bg-gray-700/50 border-gray-600 focus-within:border-green-400/50 focus-within:ring-1 focus-within:ring-green-400/30 p-3 rounded"
                                />
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <Card
                                    className="bg-gray-700/40 border border-gray-600/50 hover:border-green-400/30 transition-colors">
                                    <CardHeader className="pb-3">
                                        <CardTitle className="text-sm font-medium flex items-center gap-2">
                                            <Shield className="w-4 h-4 text-green-400"/>
                                            Grant Types
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent>
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
                                            className="bg-gray-700/50 border-gray-600"
                                        />
                                    </CardContent>
                                </Card>
                                
                                <Card
                                    className="bg-gray-700/40 border border-gray-600/50 hover:border-green-400/30 transition-colors">
                                    <CardHeader className="pb-3">
                                        <CardTitle className="text-sm font-medium flex items-center gap-2">
                                            <Reply className="w-4 h-4 text-green-400"/>
                                            Response Types
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <MultiSelectBadge
                                            label={""}
                                            selected={formData.responseTypes}
                                            setSelected={(values) => handleArrayChange("responseTypes", values)}
                                            options={["code", "token", "id_token"]}
                                            className="bg-gray-700/50 border-gray-600"
                                        />
                                    </CardContent>
                                </Card>

                                <Card
                                    className="bg-gray-700/40 border border-gray-600/50 hover:border-green-400/30 transition-colors">
                                    <CardHeader className="pb-3">
                                        <CardTitle className="text-sm font-medium flex items-center gap-2">
                                            <Key className="w-4 h-4 text-green-400"/>
                                            OAuth Scopes
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <MultiSelectBadge
                                            label={""}
                                            selected={selectedScopes}
                                            setSelected={setSelectedScopes}
                                            options={["openid", "profile", "email", "offline_access"]}
                                            disabledItems={["openid"]}
                                            className=""
                                        />
                                    </CardContent>
                                </Card>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                        <Cpu className="w-4 h-4 text-green-400"/>
                                        Token Endpoint Auth
                                    </Label>
                                    <Select
                                        value={formData.tokenEndpointAuthMethod}
                                        onValueChange={(value) => setFormData({
                                            ...formData,
                                            tokenEndpointAuthMethod: value
                                        })}
                                    >
                                        <SelectTrigger
                                            className="bg-gray-700/50 border-gray-600 text-white hover:bg-gray-600/50">
                                            <SelectValue placeholder="Select method"/>
                                        </SelectTrigger>
                                        <SelectContent className="bg-gray-800 border-gray-700">
                                            <SelectItem value="client_secret_basic" className="hover:bg-gray-700">
                                                Client Secret Basic
                                            </SelectItem>
                                            <SelectItem value="client_secret_post" className="hover:bg-gray-700">
                                                Client Secret Post
                                            </SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label className="text-sm font-medium text-gray-300 flex items-center gap-2">
                                        <Globe className="w-4 h-4 text-green-400"/>
                                        App Info URL
                                    </Label>
                                    <Input
                                        name="appInfoUri"
                                        value={formData.appInfoUri}
                                        onChange={handleChange}
                                        className="bg-gray-700/50 border-gray-600 focus:border-green-400/50 focus:ring-1 focus:ring-green-400/30"
                                        placeholder="https://example.com/about"
                                    />
                                </div>
                            </div>
                        </div>

                        <div
                            className="flex flex-col sm:flex-row justify-end gap-3 pt-4 border-t border-gray-700/50">
                            <Button variant="outline" className="border-gray-600 hover:bg-gray-700/50"
                                    onClick={() => nav("/dashboard")}>
                                Cancel
                            </Button>
                            <Button
                                onClick={registerApp}
                                className="bg-green-600 hover:bg-green-500/90 shadow-[0_0_15px_-3px_rgba(74,222,128,0.3)] hover:shadow-[0_0_20px_-3px_rgba(74,222,128,0.4)] transition-all"
                            >
                                <Check className="w-5 h-5 mr-2"/>
                                Register Application
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </motion.div>
        </div>
    );

}