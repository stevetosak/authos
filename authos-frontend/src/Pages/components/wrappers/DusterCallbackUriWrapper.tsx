import {WrapperState} from "@/Pages/components/wrappers/DataWrapper.tsx";
import {Input} from "@/components/ui/input.tsx";
import React from "react";
import {Badge} from "@/components/ui/badge.tsx";

export interface DusterCallbackUriState extends WrapperState {
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export const DusterCallbackUriWrapper = ({
                                      editing,
                                      currentApp,
                                      editedApp,
                                      onChange
                                  }: DusterCallbackUriState) => {
    return (editing ? (
            <div className="space-y-2 bg-gray-700 border-gray-600">
                <Input
                    name="dusterCallbackUri"
                    value={editedApp?.dusterCallbackUri}
                    onChange={onChange}
                    placeholder="Callback Url"
                    className="bg-gray-700 border-gray-600"
                />
            </div>
        ) : (
            <>
                    <Badge variant="outline"
                           className="bg-gray-700 border-gray-600 hover:bg-gray-600">
                        {currentApp.dusterCallbackUri}
                    </Badge>
            </>
        )
    )

}