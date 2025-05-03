import {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {Tooltip, TooltipTrigger, TooltipContent} from "@/components/ui/tooltip";
import {Info} from "lucide-react";
import ScopeSelector from "@/Pages/ClientRegistrationPage/components/ScopeSelector.tsx";
import RedirectUriFormInput from "@/Pages/ClientRegistrationPage/components/RedirectUriFormInput.tsx";
import axios from "axios";

//todo app icon validation to match app uri
export default function ClientRegistration() {
    const [formData, setFormData] = useState({
        appName: String,
        appIconUrl: String,
        shortDescription: String,
        tokenEndpointAuthMethod: String,
        grantType : String,
        responseType : String,
        appInfoUri : String,
    });

    const [selectedScopes, setSelectedScopes] = useState<string[]>(["openid"])
    const [redirectUris, setRedirectUris] = useState<string[]>([])

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const registerApp = async () => {
        const data = {
            ...formData,
            redirectUris: redirectUris,
            scopes: selectedScopes
        }

        try {
            await axios.post("http://localhost:9000/connect/register", data)
            alert(`Successfully registered app: ${formData.appName}`)
        } catch (err) {
            console.error(err)
        }
    }

    return (
        <div className="flex justify-center items-center min-h-screen bg-gray-900 text-white p-6">
            <Card className="w-full max-w-3xl bg-gray-800 rounded-xl shadow-lg p-6">
                <CardContent className="space-y-6">
                    <h2 className="text-2xl font-bold text-green-400 text-center">Register OAuth App</h2>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <Label className="flex items-center gap-2 p-2">Application Name
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <Info className="w-4 h-4 text-gray-400"/>
                                    </TooltipTrigger>
                                    <TooltipContent className="bg-gray-800 text-white">The name of your
                                        application.</TooltipContent>
                                </Tooltip>
                            </Label>
                            <Input name="appName" value={formData.appName} onChange={handleChange}
                                   className="bg-gray-700 border-gray-600 text-white w-full"/>
                        </div>
                        <div>
                            <Label className="flex items-center gap-2 p-2">Application Icon (URL)
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <Info className="w-4 h-4 text-gray-400"/>
                                    </TooltipTrigger>
                                    <TooltipContent className="bg-gray-800 text-white">URL to the application's
                                        icon.</TooltipContent>
                                </Tooltip>
                            </Label>
                            <Input name="appIcon" value={formData.appIcon} onChange={handleChange}
                                   className="bg-gray-700 border-gray-600 text-white w-full"/>
                        </div>
                    </div>

                    <div>
                        <Label className="flex items-center gap-2 p-2">Short Description
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Info className="w-4 h-4 text-gray-400"/>
                                </TooltipTrigger>
                                <TooltipContent className="bg-gray-800 text-white">Brief description of your
                                    application.</TooltipContent>
                            </Tooltip>
                        </Label>
                        <Textarea name="shortDescription" value={formData.shortDescription} onChange={handleChange}
                                  className="bg-gray-700 border-gray-600 text-white w-full"/>
                    </div>

                    <RedirectUriFormInput redirectUris={redirectUris} setRedirectUris={setRedirectUris}/>


                    <div>
                        <Label className="flex items-center gap-2 p-2">App Info URI
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Info className="w-4 h-4 text-gray-400"/>
                                </TooltipTrigger>
                                <TooltipContent className="bg-gray-800 text-white">A webpage that provides information
                                    about your application.</TooltipContent>
                            </Tooltip>
                        </Label>
                        <Input name="appInfoUri" value={formData.appInfoUri} onChange={handleChange}
                               className="bg-gray-700 border-gray-600 text-white w-full"/>
                    </div>


                    <ScopeSelector selectedScopes={selectedScopes} setSelectedScopes={setSelectedScopes}/>

                    <Button className="w-full bg-green-500 hover:bg-green-600 text-white py-3 rounded-lg text-lg mt-4"
                            onClick={() => registerApp()}
                    >Register</Button>
                </CardContent>
            </Card>
        </div>
    );
}