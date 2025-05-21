import {Input} from "@/components/ui/input.tsx";
import {CardDescription, CardFooter, CardTitle} from "@/components/ui/card.tsx";
import React from "react";
import {WrapperState} from "@/components/wrappers/DataWrapper.tsx";
import {Label} from "@/components/ui/label.tsx";


export interface TitleDescState extends WrapperState {
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export const TitleDescWrapper = ({currentApp, editedApp, onChange, editing}: TitleDescState) => {
    return (editing ? (
            <div className="space-y-2">
                <Input
                    name="name"
                    value={editedApp?.name}
                    onChange={onChange}
                    className="text-2xl font-bold bg-gray-700 border-gray-600"
                />
                <Input
                    name="shortDescription"
                    value={editedApp?.shortDescription}
                    onChange={onChange}
                    placeholder="Application description"
                    className="bg-gray-700 border-gray-600"
                />
            </div>
        ) : (
            <>
                <CardTitle className="text-2xl md:text-3xl font-bold flex items-center gap-2">
                    {currentApp.name}
                </CardTitle>
                {currentApp.shortDescription && (
                    <CardDescription className="text-gray-300">
                        {currentApp.shortDescription}
                    </CardDescription>
                )}
            </>
        )
    )
}