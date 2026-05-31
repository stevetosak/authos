import {ReactNode} from "react";

type SettingsSectionCardProps = {
    icon: ReactNode;
    title: string;
    description?: string;
    children: ReactNode;
    className?: string;
}

export const SettingsSectionCard = ({icon, title, description, children, className = "bg-gray-700/20"}: SettingsSectionCardProps) => {
    return (
        <div className={`${className} p-5 rounded-lg border border-gray-600`}>
            <div className="flex items-center gap-3 mb-1">
                <span className="p-1.5 rounded-md bg-gray-600/50">
                    {icon}
                </span>
                <div>
                    <h3 className="text-lg font-semibold leading-tight">{title}</h3>
                    {description && <p className="text-sm text-gray-400 mt-0.5">{description}</p>}
                </div>
            </div>
            <div className="border-b border-gray-600/40 mb-4"/>
            {children}
        </div>
    )
}
