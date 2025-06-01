import {WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {TrashIcon} from "lucide-react";

export const ResponseTypesWrapper = ({
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
                        value={inputValues.responseTypes}
                        onChange={(e) => handleInputChange("responseTypes",e.target.value)}
                        placeholder="Add new Response Type"
                        className="bg-gray-700 border-gray-600 flex-1"
                    />
                    <Button
                        onClick={() => addElement("responseTypes")}
                        variant="outline"
                        className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                    >
                        Add
                    </Button>
                </div>
                <div className="flex flex-wrap gap-2">
                    {editedApp.responseTypes.map((rt: string) => (
                        <Badge
                            key={rt}
                            variant="outline"
                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                        >
                            {rt}
                            <button
                                onClick={() => removeElement("responseTypes",rt)}
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
                {currentApp.responseTypes.map((rt: string) => (
                    <Badge
                        key={rt}
                        variant="outline"
                        className="bg-gray-700 border-gray-600 hover:bg-gray-600"
                    >
                        {rt}
                    </Badge>
                ))}
            </div>
        )
    )
}