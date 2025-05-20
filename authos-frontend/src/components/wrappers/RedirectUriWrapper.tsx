import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {TrashIcon} from "lucide-react";
import React from "react";
import {WrapperState} from "@/components/wrappers/DataWrapper.tsx";

export interface redirectUriState extends WrapperState {
    newRedirectUri: string,
    addRedirectUri: () => void,
    setNewRedirectUri: React.Dispatch<React.SetStateAction<string>>,
    removeRedirectUri: (uri: string) => void
}

export const RedirectUriWrapper = ({
                                editing,
                                currentApp,
                                editedApp,
                                newRedirectUri,
                                addRedirectUri,
                                setNewRedirectUri,
                                removeRedirectUri
                            }: redirectUriState) => {
    return (
        editing ? (
            <>
                <div className="flex gap-2">
                    <Input
                        value={newRedirectUri}
                        onChange={(e) => setNewRedirectUri(e.target.value)}
                        placeholder="Add new redirect URI"
                        className="bg-gray-700 border-gray-600 flex-1"
                    />
                    <Button
                        onClick={addRedirectUri}
                        variant="outline"
                        className="border-emerald-500 text-emerald-500 hover:bg-emerald-500/10"
                    >
                        Add
                    </Button>
                </div>
                <div className="flex flex-wrap gap-2">
                    {editedApp.redirectUris.map((uri: string) => (
                        <Badge
                            key={uri}
                            variant="outline"
                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 group pr-1"
                        >
                            {uri}
                            <button
                                onClick={() => removeRedirectUri(uri)}
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
                {currentApp.redirectUris.map((uri: string) => (
                    <Badge
                        key={uri}
                        variant="outline"
                        className="bg-gray-700 border-gray-600 hover:bg-gray-600"
                    >
                        {uri}
                    </Badge>
                ))}
            </div>
        )
    )
}
