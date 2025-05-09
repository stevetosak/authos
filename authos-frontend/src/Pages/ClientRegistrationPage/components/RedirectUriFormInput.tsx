import React, { useState } from "react";
import { Info, X } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function RedirectUriFormInput({redirectUris,setRedirectUris} : {redirectUris : string[]; setRedirectUris : (uris : string[]) => void}) {
    const [inputValue, setInputValue] = useState("");
    const [error, setError] = useState("");

    const urlRegex = /^(https:\/\/[^\s/$.?#].[^\s]*)$|^(http:\/\/(localhost|127\.0\.0\.1)(:\d{1,5})?\/?.*)$/;

    const addRedirectUri = () => {
        const trimmedUri = inputValue.trim();

        if (!trimmedUri) return;

        if (!urlRegex.test(trimmedUri)) {
            setError("Invalid URL. Must start with 'https://' or be 'http://localhost' / 'http://127.0.0.1' on any port.");
            return;
        }

        if (!redirectUris.includes(trimmedUri)) {
            setRedirectUris([...redirectUris, trimmedUri]);
            setInputValue("");
            setError(""); 
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter") {
            e.preventDefault();
            addRedirectUri();
        }
    };

    const removeRedirectUri = (uri: string) => {
        setRedirectUris(redirectUris.filter((u) => u !== uri));
    };

    return (
        <div>
            <label className="flex items-center gap-2 p-2 text-white">
                Redirect URIs
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Info className="w-4 h-4 text-gray-400 cursor-pointer" />
                    </TooltipTrigger>
                    <TooltipContent className="bg-gray-800 text-white">
                        URIs where users are redirected after authentication. Press Enter to add.
                    </TooltipContent>
                </Tooltip>
            </label>

            <div className="flex gap-2">
                <Input
                    name="redirectUris"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className={`bg-gray-700 border-gray-600 text-white w-full ${error ? "border-red-500" : ""}`}
                    placeholder="Type a URI and press Enter..."
                />
                <Button onClick={addRedirectUri} className="bg-green-600 hover:bg-green-700 border-b-black text-white">
                    Add
                </Button>
            </div>

            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}

            <div className="mt-2 flex flex-wrap gap-2">
                {redirectUris.map((uri) => (
                    <div key={uri} className="relative flex items-center">
                        <Badge className="flex items-center gap-1 bg-gray-500 text-white px-2 py-1 rounded-md pr-6">
                            {uri}
                        </Badge>
                        <X
                            className="absolute right-1 top-1 w-3 h-3 cursor-pointer text-white bg-gray-700 rounded-full p-0.5 hover:bg-gray-600 pointer-events-auto"
                            onClick={() => removeRedirectUri(uri)}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}
