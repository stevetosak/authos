import {WrapperState} from "@/components/wrappers/DataWrapper.tsx";
import React from "react";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {TrashIcon} from "lucide-react";

export interface GrantTypeState extends WrapperState {
    newGrantType: string,
    addGrantType: () => void,
    setNewGrantType: React.Dispatch<React.SetStateAction<string>>,
    removeGrantType: (gt: string) => void
}

export const RedirectUriWrapper = ({
                                       editing,
                                       currentApp,
                                       editedApp,
                                       newGrantType,
                                       addGrantType,
                                       setNewGrantType,
                                       removeGrantType
                                   }: GrantTypeState) => {
    return (
        editing ? (
            <>
                <div className="flex gap-2">
                    <Input
                        value={newGrantType}
                        onChange={(e) => setNewGrantType(e.target.value)}
                        placeholder="Add new Grant Type"
                        className="bg-gray-700 border-gray-600 flex-1"
                    />
                    <Button
                        onClick={addGrantType}
                        variant="outline"
                        className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                    >
                        Add
                    </Button>
                </div>
                <div className="flex flex-wrap gap-2">
                    {editedApp.redirectUris.map((gt: string) => (
                        <Badge
                            key={gt}
                            variant="outline"
                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                        >
                            {gt}
                            <button
                                onClick={() => removeGrantType(gt)}
                                className="ml-2 text-gray-400 hover:text-red-400 p-1 rounded-full"
                            >
                                <TrashIcon className="w-3 h-3"/>
                            </button>
                        </Badge>
                    ))}
                </div>
            </>
        ) : (
            <div className="flex flex-wrap gap-2">
                {currentApp.grantTypes.map((gt: string) => (
                    <Badge
                        key={gt}
                        variant="outline"
                        className="bg-gray-700 border-gray-600 hover:bg-gray-600"
                    >
                        {gt}
                    </Badge>
                ))}
            </div>
        )
    )
}
