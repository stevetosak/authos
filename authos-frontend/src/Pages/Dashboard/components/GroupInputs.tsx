import {AppGroup} from "@/services/types.ts";
import React, {SetStateAction} from "react";
import {useAuth} from "@/services/useAuth.ts";
import {Label} from "@/components/ui/label";
import {Calendar, CheckCircle, Key, Shield} from "lucide-react";
import {Select, SelectTrigger, SelectValue, SelectContent, SelectItem} from "@/components/ui/select";
import {Badge} from "@/components/ui/badge.tsx";
import {useContextGuarded} from "@/services/useContextGuarded.ts";
import {DashboardContext, DashboardContextType} from "@/Pages/Dashboard/components/context/DashboardContext.ts";


export const GroupInputs = () => {
    const {
        isEditingGroup,
        selectedGroup,
        selectedGroupEditing,
        handleGroupUpdate
    } = useContextGuarded<DashboardContextType>(DashboardContext)

    return (
        <div
            className="bg-foreground p-4 rounded-lg border border-gray-700/50 md:col-span-3">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="space-y-1">
                    <Label className="text-gray-400 text-sm flex items-center gap-1">
                        <CheckCircle className="w-4 h-4 text-emerald-400"/>
                        Default Group
                    </Label>
                    {isEditingGroup ? (
                        <Select
                            value={selectedGroupEditing?.isDefault ? "true" : "false"}
                            onValueChange={(value) => handleGroupUpdate("isDefault", value === "true")}
                        >
                            <SelectTrigger
                                className="w-full bg-gray-700 border-gray-600 text-white">
                                <SelectValue placeholder="Select"/>
                            </SelectTrigger>
                            <SelectContent
                                className="bg-gray-800 border-gray-700 text-white">
                                <SelectItem value="true">Yes</SelectItem>
                                <SelectItem value="false">No</SelectItem>
                            </SelectContent>
                        </Select>
                    ) : (
                        <p className="text-white font-medium">
                            {selectedGroup?.isDefault ? "Yes" : "No"}
                        </p>
                    )}
                </div>

                <div className="space-y-1">
                    <Label className="text-gray-400 text-sm flex items-center gap-1">
                        <Shield className="w-4 h-4 text-blue-400"/>
                        MFA Policy
                    </Label>
                    {isEditingGroup ? (
                        <Select
                            value={selectedGroupEditing?.mfaPolicy || ""}
                            onValueChange={(value) => handleGroupUpdate("mfaPolicy", value)}
                        >
                            <SelectTrigger
                                className="w-full bg-gray-700 border-gray-600 text-white">
                                <SelectValue placeholder="Select policy"/>
                            </SelectTrigger>
                            <SelectContent
                                className="bg-gray-800 border-gray-700 text-white">
                                <SelectItem value={"Email"}>Email</SelectItem>
                                <SelectItem value={"Phone"}>Phone</SelectItem>
                                <SelectItem value={"Disabled"}>Disabled</SelectItem>
                            </SelectContent>
                        </Select>
                    ) : (
                        <Badge
                            variant="outline"
                            className="text-white border-gray-600 bg-gray-700/50"
                        >
                            {selectedGroup?.mfaPolicy || "Not set"}
                        </Badge>
                    )}
                </div>

                <div className="space-y-1">
                    <Label className="text-gray-400 text-sm flex items-center gap-1">
                        <Key className="w-4 h-4 text-purple-400"/>
                        SSO Policy
                    </Label>
                    {isEditingGroup ? (
                        <Select
                            value={selectedGroupEditing?.ssoPolicy || ""}
                            onValueChange={(value) => handleGroupUpdate("ssoPolicy", value)}
                        >
                            <SelectTrigger
                                className="w-full bg-gray-700 border-gray-600 text-white">
                                <SelectValue placeholder="Select policy"/>
                            </SelectTrigger>
                            <SelectContent
                                className="bg-gray-800 border-gray-700 text-white">
                                <SelectItem value="Full">Full</SelectItem>
                                <SelectItem value="Partial">Partial</SelectItem>
                                <SelectItem value="Same-Domain">Same-Domain</SelectItem>
                                <SelectItem value="Disabled">Disabled</SelectItem>
                            </SelectContent>
                        </Select>
                    ) : (
                        <Badge
                            variant="outline"
                            className="text-white border-gray-600 bg-gray-700/50"
                        >
                            {selectedGroup?.ssoPolicy || "Not set"}
                        </Badge>
                    )}
                </div>

                <div className="space-y-1">
                    <Label className="text-gray-400 text-sm flex items-center gap-1">
                        <Calendar className="w-4 h-4 text-yellow-400"/>
                        Created At
                    </Label>
                    <p className="text-white font-medium">
                        {selectedGroup?.createdAt
                            ? new Date(selectedGroup.createdAt).toLocaleDateString()
                            : "N/A"}
                    </p>
                </div>
            </div>
        </div>
    )
}