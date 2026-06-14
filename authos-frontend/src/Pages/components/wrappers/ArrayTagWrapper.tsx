import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {PencilIcon, TrashIcon} from "lucide-react";
import {WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {App} from "@/services/types.ts";

interface ArrayTagWrapperProps extends WrapperState {
    field: keyof App;
    placeholder: string;
}

export const ArrayTagWrapper = ({
    editing,
    currentApp,
    editedApp,
    inputValues,
    handleInputChange,
    addElement,
    removeElement,
    field,
    placeholder,
}: ArrayTagWrapperProps) => {
    const fieldKey = field as string;
    const viewItems = (currentApp[field] as string[]) ?? [];
    const editItems = (editedApp[field] as string[]) ?? [];

    return editing ? (
        <>
            <div className="flex gap-2">
                <Input
                    value={inputValues[fieldKey] ?? ""}
                    onChange={(e) => handleInputChange(fieldKey, e.target.value)}
                    placeholder={placeholder}
                    className="bg-gray-700 border-gray-600 flex-1"
                />
                <Button onClick={() => addElement(field)}>
                    Add
                </Button>
            </div>
            <div className="mt-3 flex flex-wrap gap-2">
                {editItems.map((item) => (
                    <Badge
                        key={item}
                        variant="outline"
                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                    >
                        {item}
                        <button
                            onClick={() => { handleInputChange(fieldKey, item); removeElement(field, item); }}
                            className="ml-2 text-gray-400 hover:text-teal-400 p-1 rounded-full"
                        >
                            <PencilIcon className="w-3 h-3"/>
                        </button>
                        <button
                            onClick={() => removeElement(field, item)}
                            className="text-gray-400 hover:text-red-400 p-1 rounded-full"
                        >
                            <TrashIcon className="w-3 h-3"/>
                        </button>
                    </Badge>
                ))}
            </div>
        </>
    ) : (
        <div className="flex flex-wrap gap-2">
            {viewItems.map((item) => (
                <Badge
                    key={item}
                    variant="outline"
                    className="bg-gray-700 border-gray-600 hover:bg-gray-600"
                >
                    {item}
                </Badge>
            ))}
        </div>
    );
};