import {ReactNode} from "react";

type SettingsSectionCardProps = {
    icon: ReactNode;
    title: string;
    children: ReactNode;
    className?: string;
}

export const SettingsSectionCard = ({icon, title, children, className = "bg-gray-700/20"}: SettingsSectionCardProps) => {
    return (
        <div className={`${className} p-5 rounded-lg border border-gray-600`}>
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                {icon}
                {title}
            </h3>
            {children}
        </div>
    )
}
