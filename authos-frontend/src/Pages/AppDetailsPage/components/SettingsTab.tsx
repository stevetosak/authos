import {TabsContent} from "@/components/ui/tabs.tsx";
import {CodeIcon, GlobeIcon, Info, ShieldIcon} from "lucide-react";
import {DataWrapper, WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {App} from "@/services/types.ts";
import React from "react";
import {SettingsSectionCard} from "@/Pages/AppDetailsPage/components/SettingsSectionCard.tsx";

type SettingsTabProps = {value?: string, baseState: WrapperState, app: App, handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void}

export const SettingsTab = ({value = "settings", baseState, app, handleChange}: SettingsTabProps) => {
    return (
        <TabsContent value={value} className="p-6 space-y-8">
            <div className="space-y-6">
                <SettingsSectionCard icon={<GlobeIcon className="w-4 h-4"/>} title="Redirect URIs" description="Allowed callback URLs after authentication">
                    <DataWrapper state={{...baseState}} wrapper={"redirectUri"}/>
                </SettingsSectionCard>

                <SettingsSectionCard icon={<ShieldIcon className="w-4 h-4"/>} title="Permissions" description="Scopes and grant types this application can request">
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
                </SettingsSectionCard>

                <SettingsSectionCard icon={<CodeIcon className="w-4 h-4"/>} title="Response Configuration" description="Token and response format settings">
                    <div className="grid gap-6 md:grid-cols-2">
                        <div className="space-y-4">
                            <Label className="flex items-center gap-2">
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
                            <Label className="flex items-center gap-2">
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
                </SettingsSectionCard>

                <SettingsSectionCard icon={<CodeIcon className="w-4 h-4"/>} title="Duster" description="Webhook callback for the Duster integration" className="bg-gray-700/50">
                    <div className="grid gap-6 md:grid-cols-2">
                        <div className="space-y-4">
                            <h4 className="font-medium text-gray-300">Callback URL</h4>
                            <DataWrapper state={{...baseState, onChange: handleChange}} wrapper={"dusterCallback"}/>
                        </div>
                    </div>
                </SettingsSectionCard>
            </div>
        </TabsContent>
    )
}
