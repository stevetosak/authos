import {TabsContent} from "@/components/ui/tabs.tsx";
import {CopyIcon, Eye, EyeOff, KeyIcon, LockIcon, RefreshCwIcon} from "lucide-react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Button} from "@/components/ui/button.tsx";
import {copyToClipboard} from "@/services/utils.ts";
import {App} from "@/services/types.ts";
import {useState} from "react";
import {CredentialCard} from "@/Pages/AppDetailsPage/components/CredentialCard.tsx";

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
            <CredentialCard
                icon={<KeyIcon className="w-4 h-4"/>}
                label="Client ID"
                value={app.clientId}
                actions={
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
                }
            />

            <CredentialCard
                icon={<LockIcon className="w-4 h-4"/>}
                label="Client Secret"
                value={showSecret ? app.clientSecret : '•'.repeat(app.clientSecret.length)}
                actions={
                    <>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="w-8 h-8 hover:bg-gray-600"
                                    onClick={() => setShowSecret(!showSecret)}
                                >
                                    {showSecret ? <EyeOff className="w-3 h-3"/> : <Eye className="w-3 h-3"/>}
                                    <span className="sr-only">{showSecret ? "Hide" : "Show"} secret</span>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>{showSecret ? "Hide secret" : "Show secret"}</TooltipContent>
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
                                        <RefreshCwIcon className={`w-3 h-3 ${isRegeneratingSecret ? "animate-spin" : ""}`}/>
                                        <span className="sr-only">Regenerate</span>
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent>Regenerate secret</TooltipContent>
                            </Tooltip>
                        )}
                    </>
                }
                footer={<p className="mt-2 text-xs text-gray-400 italic">Do not share this secret with anyone</p>}
            />
        </TabsContent>
    )
}