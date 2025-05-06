import {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {Tooltip, TooltipTrigger, TooltipContent} from "@/components/ui/tooltip";
import {Info} from "lucide-react";
import RedirectUriFormInput from "@/Pages/ClientRegistrationPage/components/RedirectUriFormInput.tsx";
import axios from "axios";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import MultiSelectBadge from "@/components/my/MultiSelectBadge.tsx";

export default function ClientRegistration() {
    const [formData, setFormData] = useState({
        appName: "",
        appIconUrl: "",
        shortDescription: "",
        tokenEndpointAuthMethod: "client_secret_basic",
        grantTypes: ["authorization_code"],
        responseTypes: ["code"],
        appInfoUri: "",
    });

    const [selectedScopes, setSelectedScopes] = useState<string[]>(["openid"])
    const [redirectUris, setRedirectUris] = useState<string[]>([])

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleArrayChange = (field: string, value: string[]) => {
        setFormData({...formData, [field]: value});
    };

    const registerApp = async () => {
        const data = {
            ...formData,
            redirectUris: redirectUris,
            scope: selectedScopes
        }

        try {
            await axios.post("http://localhost:9000/connect/register", data)
            alert(`Successfully registered app: ${formData.appName}`)
        } catch (err) {
            console.error(err)
        }
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
            <div className="w-[50vw] max-h-screen overflow-y-auto bg-gray-800 rounded-xl shadow-lg flex-shrink-0">
                <Card>
                    <CardContent className="space-y-4 p-4 md:p-6">
                        <h2 className="text-2xl font-bold text-green-400 text-center mb-6">Register OAuth App</h2>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* App Name */}
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    Application Name
                                    <Tooltip>
                                        <TooltipTrigger asChild>
                                            <Info className="w-4 h-4 text-gray-400"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 text-white">The name of your
                                            application.</TooltipContent>
                                    </Tooltip>
                                </Label>
                                <Input
                                    name="appName"
                                    value={formData.appName}
                                    onChange={handleChange}
                                    className="bg-gray-700 border-gray-600 text-white focus:ring-green-500 focus:border-green-500"
                                />
                            </div>

                            {/* App Icon URL */}
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    Application Icon (URL)
                                    <Tooltip>
                                        <TooltipTrigger asChild>
                                            <Info className="w-4 h-4 text-gray-400"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 text-white">URL to the application's
                                            icon.</TooltipContent>
                                    </Tooltip>
                                </Label>
                                <Input
                                    name="appIconUrl"
                                    value={formData.appIconUrl}
                                    onChange={handleChange}
                                    className="bg-gray-700 border-gray-600 text-white focus:ring-green-500 focus:border-green-500"
                                />
                            </div>
                        </div>

                        {/* Short Description */}
                        <div className="space-y-2">
                            <Label className="flex items-center gap-2">
                                Short Description
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <Info className="w-4 h-4 text-gray-400"/>
                                    </TooltipTrigger>
                                    <TooltipContent className="bg-gray-800 text-white">Brief description of your
                                        application.</TooltipContent>
                                </Tooltip>
                            </Label>
                            <Textarea
                                name="shortDescription"
                                value={formData.shortDescription}
                                onChange={handleChange}
                                className="bg-gray-700 border-gray-600 text-white focus:ring-green-500 focus:border-green-500 min-h-[100px]"
                                rows={3}
                            />
                        </div>

                        {/* Redirect URIs */}
                        <div className="space-y-2">
                            <RedirectUriFormInput redirectUris={redirectUris} setRedirectUris={setRedirectUris}/>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            {/* Grant Types */}
                            <div className="bg-gray-700 border border-gray-600 rounded-md p-4 space-y-2">
                                <MultiSelectBadge
                                    label="Grant Types"
                                    tooltip="The OAuth 2.0 grant types your application will use."
                                    selected={formData.grantTypes}
                                    setSelected={(values) => handleArrayChange("grantTypes", values)}
                                    options={[
                                        "authorization_code",
                                        "client_credentials",
                                        "refresh_token",
                                        "password",
                                        "implicit"
                                    ]}
                                    placeholder="Select grant types..."
                                />
                            </div>

                            {/* Response Types */}
                            <div className="bg-gray-700 border border-gray-600 rounded-md p-4 space-y-2">
                                <MultiSelectBadge
                                    label="Response Types"
                                    tooltip="The OAuth 2.0 response types your application will use."
                                    selected={formData.responseTypes}
                                    setSelected={(values) => handleArrayChange("responseTypes", values)}
                                    options={["code", "token", "id_token"]}
                                    placeholder="Select response types..."
                                />
                            </div>

                            {/* OAuth Scopes */}
                            <div className="bg-gray-700 border border-gray-600 rounded-md p-4 space-y-2">
                                <MultiSelectBadge
                                    label="OAuth Scopes"
                                    tooltip="Select the permissions your application needs."
                                    selected={selectedScopes}
                                    setSelected={setSelectedScopes}
                                    options={["openid", "profile", "email", "offline_access"]}
                                    placeholder="Add a scope..."
                                    disabledItems={["openid"]}
                                />
                            </div>
                        </div>


                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Token Endpoint Auth Method */}
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    Token Endpoint Auth Method
                                    <Tooltip>
                                        <TooltipTrigger asChild>
                                            <Info className="w-4 h-4 text-gray-400"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 text-white">
                                            How your application will authenticate with the token endpoint.
                                        </TooltipContent>
                                    </Tooltip>
                                </Label>
                                <Select
                                    value={formData.tokenEndpointAuthMethod}
                                    onValueChange={(value) => setFormData({
                                        ...formData,
                                        tokenEndpointAuthMethod: value
                                    })}
                                >
                                    <SelectTrigger
                                        className="bg-gray-700 border-gray-600 text-white hover:bg-gray-600">
                                        <SelectValue placeholder="Select authentication method"/>
                                    </SelectTrigger>
                                    <SelectContent className="bg-gray-800 border-gray-600 text-white">
                                        <SelectItem value="client_secret_basic">Client Secret Basic</SelectItem>
                                        <SelectItem value="client_secret_post">Client Secret Post</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            {/* App Info URI */}
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    App Info URI
                                    <Tooltip>
                                        <TooltipTrigger asChild>
                                            <Info className="w-4 h-4 text-gray-400"/>
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-gray-800 text-white">A webpage that provides
                                            information
                                            about your application.</TooltipContent>
                                    </Tooltip>
                                </Label>
                                <Input
                                    name="appInfoUri"
                                    value={formData.appInfoUri}
                                    onChange={handleChange}
                                    className="bg-gray-700 border-gray-600 text-white focus:ring-green-500 focus:border-green-500"
                                />
                            </div>
                        </div>



                        {/* Register Button */}
                        <Button
                            className="w-full bg-green-600 hover:bg-green-700 text-white py-3 rounded-lg text-lg mt-6 transition-colors duration-200"
                            onClick={registerApp}
                        >
                            Register Application
                        </Button>
                    </CardContent>
                </Card>
            </div>
        </div>
    );

}