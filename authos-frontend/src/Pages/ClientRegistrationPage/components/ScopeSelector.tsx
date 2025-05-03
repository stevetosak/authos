import { Badge } from "@/components/ui/badge";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {Label} from "@/components/ui/label.tsx";
import {Info} from "lucide-react";

const AVAILABLE_SCOPES = ["openid", "profile", "email", "offline_access"];

interface ScopeSelectorProps {
    selectedScopes: string[];
    setSelectedScopes: (scopes: string[]) => void;
}

export default function ScopeSelector({
                                          selectedScopes,
                                          setSelectedScopes,
                                      }: ScopeSelectorProps) {
    const handleScopeSelect = (scope: string) => {
        if (!selectedScopes.includes(scope)) {
            setSelectedScopes([...selectedScopes, scope]);
        }
    };

    const removeScope = (scopeToRemove: string) => {
        // Don't allow removing the openid scope
        if (scopeToRemove === "openid") return;

        setSelectedScopes(selectedScopes.filter((scope) => scope !== scopeToRemove));
    };

    return (
        <div className="space-y-4">
            <Label className="flex items-center gap-2 p-2">
                OAuth Scopes
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Info className="w-4 h-4 text-gray-400" />
                    </TooltipTrigger>
                    <TooltipContent className="bg-gray-800 text-white">
                        Select the permissions your application needs
                    </TooltipContent>
                </Tooltip>
            </Label>

            <div className="flex gap-2 flex-wrap">
                {selectedScopes.map((scope) => (
                    <Badge
                        key={scope}
                        variant="outline"
                        className={`px-3 py-1 rounded-full flex items-center gap-1 ${
                            scope === "openid" ? "bg-gray-700" : "bg-gray-800 hover:bg-gray-700"
                        }`}
                    >
                        <span>{scope}</span>
                        {scope !== "openid" && (
                            <button
                                type="button"
                                onClick={() => removeScope(scope)}
                                className="ml-1 text-gray-400 hover:text-white"
                            >
                                ×
                            </button>
                        )}
                    </Badge>
                ))}
            </div>

            <Select onValueChange={handleScopeSelect}>
                <SelectTrigger className="bg-gray-700 border-gray-600 text-white">
                    <SelectValue placeholder="Add a scope..." />
                </SelectTrigger>
                <SelectContent className="bg-gray-800 border-gray-700 text-white">
                    {AVAILABLE_SCOPES.filter((scope) => !selectedScopes.includes(scope)).map(
                        (scope) => (
                            <SelectItem
                                key={scope}
                                value={scope}
                                className="hover:bg-gray-700 focus:bg-gray-700"
                            >
                                {scope}
                            </SelectItem>
                        )
                    )}
                </SelectContent>
            </Select>
        </div>
    );
}