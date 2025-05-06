import { Badge } from "@/components/ui/badge.tsx";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip.tsx";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select.tsx";
import { Label } from "@/components/ui/label.tsx";
import { Info } from "lucide-react";

interface MultiSelectBadgeProps {
    label: string;
    tooltip?: string;
    selected: string[];
    setSelected: (values: string[]) => void;
    options: string[];
    placeholder?: string;
    disabledItems?: string[];
}

export default function MultiSelectBadge({
                                             label,
                                             tooltip,
                                             selected,
                                             setSelected,
                                             options,
                                             placeholder = "Select...",
                                             disabledItems = [],
                                         }: MultiSelectBadgeProps) {
    const handleSelect = (value: string) => {
        if (!selected.includes(value)) {
            setSelected([...selected, value]);
        }
    };

    const handleRemove = (value: string) => {
        if (disabledItems.includes(value)) return;
        setSelected(selected.filter((v) => v !== value));
    };

    return (
        <div className="space-y-4">
            <Label className="flex items-center gap-2 p-2">
                {label}
                {tooltip && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Info className="w-4 h-4 text-gray-400" />
                        </TooltipTrigger>
                        <TooltipContent className="bg-gray-800 text-white">{tooltip}</TooltipContent>
                    </Tooltip>
                )}
            </Label>

            <div className="flex gap-2 flex-wrap">
                {selected.map((value) => (
                    <Badge
                        key={value}
                        variant="outline"
                        className={`px-3 py-1 rounded-full flex items-center gap-1 ${
                            disabledItems.includes(value) ? "bg-gray-700" : "bg-gray-800 hover:bg-gray-700"
                        }`}
                    >
                        <span>{value}</span>
                        {!disabledItems.includes(value) && (
                            <button
                                type="button"
                                onClick={() => handleRemove(value)}
                                className="ml-1 text-gray-400 hover:text-white"
                            >
                                ×
                            </button>
                        )}
                    </Badge>
                ))}
            </div>

            <Select onValueChange={handleSelect}>
                <SelectTrigger className="bg-gray-700 border-gray-600 text-white">
                    <SelectValue placeholder={placeholder} />
                </SelectTrigger>
                <SelectContent className="bg-gray-800 border-gray-700 text-white">
                    {options.filter((o) => !selected.includes(o)).map((value) => (
                        <SelectItem
                            key={value}
                            value={value}
                            className="hover:bg-gray-700 focus:bg-gray-700"
                        >
                            {value}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
