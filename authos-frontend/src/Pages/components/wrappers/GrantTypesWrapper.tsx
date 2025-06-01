import {WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import React from "react";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {TrashIcon} from "lucide-react";


export const GrantTypeWrapper = ({
                                       editing,
                                       currentApp,
                                       editedApp,
                                       inputValues,
                                       handleInputChange,
                                       addElement,
                                       removeElement
                                   }: WrapperState) => {
    return (
        editing ? (
            <>
                <div className="flex gap-2">
                    <Input
                        value={inputValues.grantTypes}
                        onChange={(e) => handleInputChange("grantTypes",e.target.value)}
                        placeholder="Add new Grant Type"
                        className="bg-gray-700 border-gray-600 flex-1"
                    />
                    <Button
                        onClick={() => addElement("grantTypes")}
                        variant="outline"
                        className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                    >
                        Add
                    </Button>
                </div>
                <div className="flex flex-wrap gap-2">
                    {editedApp.grantTypes.map((gt: string) => (
                        <Badge
                            key={gt}
                            variant="outline"
                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                        >
                            {gt}
                            <button
                                onClick={() => removeElement("grantTypes",gt)}
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
