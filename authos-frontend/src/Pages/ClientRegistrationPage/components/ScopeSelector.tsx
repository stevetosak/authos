import { useState } from "react";
import { Info, X, ChevronDown } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip.tsx";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx";
import { Command, CommandInput, CommandItem, CommandList } from "@/components/ui/command.tsx";
import { Badge } from "@/components/ui/badge.tsx";
import { Button } from "@/components/ui/button.tsx";

const availableScopes = ["openid", "profile", "email", "offline_access"];

export default function ScopeSelector({ selectedScopes, setSelectedScopes }: { selectedScopes: string[]; setSelectedScopes: (scopes: string[]) => void }) {
    const [open, setOpen] = useState(false);

    const addScope = (scope: string) => {
        if (!selectedScopes.includes(scope)) {
            setSelectedScopes([...selectedScopes, scope]);
        }
        setOpen(false);
    };

    const removeScope = (scope: string) => {
        setSelectedScopes(selectedScopes.filter((s) => s !== scope));
    };

    return (
        <div>
            <label className="flex items-center gap-2 p-2 text-white">
                Scopes
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Info className="w-4 h-4 text-gray-400 cursor-pointer" />
                    </TooltipTrigger>
                    <TooltipContent className="bg-gray-800 text-white">Select or type a scope.</TooltipContent>
                </Tooltip>
            </label>

            <div className="border border-gray-600 bg-gray-700 p-2 rounded-md w-full flex flex-wrap gap-1">
                {selectedScopes.map((scope) => (
                    <div key={scope} className="relative flex items-center">
                        <Badge className="flex items-center gap-1 bg-gray-500 text-white px-2 py-1 rounded-md pr-6">
                            {scope}
                        </Badge>
                        <X
                            className="absolute right-1 top-2 w-3 h-3 cursor-pointer text-white bg-gray-700 rounded-full p-0.5 hover:bg-gray-600 pointer-events-auto"
                            onClick={() => removeScope(scope)}
                        />
                    </div>
                ))}

                <Popover open={open} onOpenChange={setOpen}>
                    <PopoverTrigger asChild>
                        <Button variant="ghost" className="text-white bg-gray-700 px-2 py-1 flex items-center gap-1">
                            Add Scope <ChevronDown className="w-4 h-4" />
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-48 p-1 bg-gray-800 text-white rounded-md">
                        <Command>
                            <CommandInput placeholder="Search scopes..." />
                            <CommandList>
                                {availableScopes.map((scope) => (
                                    <CommandItem
                                        key={scope}
                                        onSelect={() => addScope(scope)}
                                        className="cursor-pointer flex justify-between"
                                    >
                                        {scope}
                                    </CommandItem>
                                ))}
                            </CommandList>
                        </Command>
                    </PopoverContent>
                </Popover>
            </div>
        </div>
    );
}
