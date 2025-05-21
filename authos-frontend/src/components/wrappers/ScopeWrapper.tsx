import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {TrashIcon} from "lucide-react";
import {WrapperState} from "@/components/wrappers/DataWrapper.tsx";


export const ScopeWrapper = ({editing,currentApp,editedApp,handleInputChange,addElement,removeElement,inputValues} : WrapperState) => {
    return (editing ? (
        <>
            <div className="flex gap-2">
                <Input
                    value={inputValues.scopes}
                    onChange={(e) => handleInputChange("scopes",e.target.value)}
                    placeholder="Add new scope"
                    className="bg-gray-700 border-gray-600 flex-1"
                />
                <Button
                    onClick={() => addElement("scopes")}
                    variant="outline"
                    className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                >
                    Add
                </Button>
            </div>
            <div className="flex flex-wrap gap-2">
                {editedApp.scopes.map((scope) => (
                    <Badge
                        key={scope}
                        variant="outline"
                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                    >
                        {scope}
                        <button
                            onClick={() => removeElement("scopes",scope)}
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
            {currentApp.scopes.map((scope) => (
                <Badge
                    key={scope}
                    variant="outline"
                    className="bg-gray-700 border-gray-600 hover:bg-gray-600"
                >
                    {scope}
                </Badge>
            ))}
        </div>
    ))
}