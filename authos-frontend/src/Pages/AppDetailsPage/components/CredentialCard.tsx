import {Label} from "@/components/ui/label.tsx";
import {ReactNode} from "react";

type CredentialCardProps = {
    icon: ReactNode,
    label: string,
    value: string,
    actions: ReactNode,
    containerClassName?: string,
    valueClassName?: string,
    footer?: ReactNode,
}

export const CredentialCard = ({
    icon,
    label,
    value,
    actions,
    containerClassName = "bg-gray-700/20 border-gray-700",
    valueClassName = "bg-gray-800/80",
    footer,
}: CredentialCardProps) => {
    return (
        <div className={`p-4 rounded-lg border ${containerClassName}`}>
            <div className="flex items-center justify-between mb-2">
                <Label className="text-gray-300 flex items-center gap-2">
                    {icon}
                    {label}
                </Label>
                <div className="flex gap-1">
                    {actions}
                </div>
            </div>
            <code className={`block px-3 py-2 rounded-md text-sm font-mono break-all ${valueClassName}`}>
                {value}
            </code>
            {footer}
        </div>
    )
}