import {TabsContent} from "@/components/ui/tabs.tsx";
import {CodeIcon, GlobeIcon, Info, ShieldIcon} from "lucide-react";
import {DataWrapper, WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {App} from "@/services/types.ts";
import React from "react";

type SettingsTabProps = {value?: string,baseState: WrapperState,app:App,handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void}

export const SettingsTab = ({value = "settings",baseState,app,handleChange}: SettingsTabProps) => {
    return (
        <TabsContent value={value} className="p-6 space-y-8">
            <div className="space-y-6">
                <div className="bg-gray-700/30 p-5 rounded-lg border border-gray-600">
                    <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                        <GlobeIcon className="w-4 h-4"/>
                        Redirect URIs
                    </h3>
                    <DataWrapper state={{...baseState}} wrapper={"redirectUri"}/>
                </div>

                <div className="bg-gray-700/30 p-5 rounded-lg border border-gray-600">
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

                <div className="bg-gray-700/30 p-5 rounded-lg border border-gray-600">
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
    )
}