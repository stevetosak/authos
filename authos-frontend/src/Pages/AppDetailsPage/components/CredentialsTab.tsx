import {TabsContent} from "@/components/ui/tabs.tsx";
import {Label} from "@/components/ui/label.tsx";
import {CopyIcon, Eye, EyeOff, KeyIcon, LockIcon, RefreshCwIcon} from "lucide-react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Button} from "@/components/ui/button.tsx";
import {copyToClipboard} from "@/services/utils.ts";
import {App} from "@/services/types.ts";
import {useState} from "react";
import {toast} from "sonner";
import {apiPostAuthenticated} from "@/services/netconfig.ts";

type CredentialsTabProps = {
    value?: string,
    app: App,
    isEditing: boolean,
    regenerateSecret: () => void,
    isRegeneratingSecret: boolean
}

export const CredentialsTab = ({value = "credentials", app, isEditing, regenerateSecret,isRegeneratingSecret}: CredentialsTabProps) => {
    const [showSecret, setShowSecret] = useState(false)

    return (
        <TabsContent value={value} className="p-6 space-y-6">
            <div className="bg-gray-700/30 p-4 rounded-lg border border-gray-600">
                <div className="flex items-center justify-between mb-2">
                    <Label className="text-gray-300 flex items-center gap-2">
                        <KeyIcon className="w-4 h-4"/>
                        Client ID
                    </Label>
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant="ghost"
                                size="sm"
                                className="w-8 h-8 hover:bg-gray-600"
                                onClick={() => copyToClipboard(app.clientId)}
                            >
                                <CopyIcon className="w-3 h-3"/>
                                <span className="sr-only">Copy</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>Copy to clipboard</TooltipContent>
                    </Tooltip>
                </div>
                <code className="block bg-gray-800/80 px-3 py-2 rounded-md text-sm font-mono break-all">
                    {app.clientId}
                </code>
            </div>

            <div className="bg-gray-700/30 p-4 rounded-lg border border-gray-600">
                <div className="flex items-center justify-between mb-2">
                    <Label className="text-gray-300 flex items-center gap-2">
                        <LockIcon className="w-4 h-4"/>
                        Client Secret
                    </Label>
                    <div className="flex gap-1">
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="w-8 h-8 hover:bg-gray-600"
                                    onClick={() => setShowSecret(!showSecret)}
                                >
                                    {showSecret ? (
                                        <EyeOff className="w-3 h-3"/>
                                    ) : (
                                        <Eye className="w-3 h-3"/>
                                    )}
                                    <span className="sr-only">
              {showSecret ? "Hide" : "Show"} secret
            </span>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                {showSecret ? "Hide secret" : "Show secret"}
                            </TooltipContent>
                        </Tooltip>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="w-8 h-8 hover:bg-gray-600"
                                    onClick={() => copyToClipboard(app.clientSecret)}
                                >
                                    <CopyIcon className="w-3 h-3"/>
                                    <span className="sr-only">Copy</span>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>Copy to clipboard</TooltipContent>
                        </Tooltip>
                        {isEditing && (
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="w-8 h-8 hover:bg-gray-600"
                                        onClick={regenerateSecret}
                                        disabled={isRegeneratingSecret}
                                    >
                                        {isRegeneratingSecret ? (
                                            <RefreshCwIcon className="w-3 h-3 animate-spin"/>
                                        ) : (
                                            <RefreshCwIcon className="w-3 h-3"/>
                                        )}
                                        <span className="sr-only">Regenerate</span>
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent>Regenerate secret</TooltipContent>
                            </Tooltip>
                        )}
                    </div>
                </div>
                <code className="block bg-gray-800 px-3 py-2 rounded text-sm font-mono break-all">
                    {showSecret ? app.clientSecret : '•'.repeat(app.clientSecret.length)}
                </code>
                <p className="mt-2 text-xs text-gray-400 italic">
                    Do not share this secret with anyone
                </p>
            </div>
        </TabsContent>
    )
}