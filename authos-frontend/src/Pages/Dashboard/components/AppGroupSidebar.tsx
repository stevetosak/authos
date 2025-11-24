import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Users} from "lucide-react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {AddGroupModal} from "@/Pages/components/AddGroupModal.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import {Button} from "@/components/ui/button.tsx";
import React from "react";
import {useContextGuarded} from "@/services/useContextGuarded.ts";
import {DashboardContext, DashboardContextType} from "@/Pages/Dashboard/components/context/DashboardContext.ts";
import {useAuth} from "@/services/useAuth.ts";

export const AppGroupSidebar = () => {
    const {groups,apps} = useAuth()

    const {selectedGroup,handleGroupClick} = useContextGuarded<DashboardContextType>(DashboardContext)

    return (
        <div className="lg:w-72 flex-shrink-0 bg-gradient-primary">
            <div className="sticky top-6 h-[calc(100vh-3rem)] overflow-hidden">
                <Card className="backdrop-blur-sm border border-gray-800 rounded-2xl overflow-hidden">
                    <CardHeader className="border-b border-gray-700/50 pb-3">
                        <div className="flex items-center justify-between">
                            <CardTitle className="flex items-center gap-2 text-lg">
                                <Users className="w-5 h-5 text-primary"/>
                                Application Groups
                            </CardTitle>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <AddGroupModal/>
                                </TooltipTrigger>
                                <TooltipContent>Create new group</TooltipContent>
                            </Tooltip>
                        </div>
                    </CardHeader>
                    <CardContent className="p-2">
                        <ScrollArea className="h-[calc(100%-60px)] pr-2">
                            <div className="space-y-1">
                                {groups.map((group) => (
                                    <Button
                                        key={group.id}
                                        onClick={() => handleGroupClick(group)}
                                        variant={selectedGroup.id === group.id ? "secondary" : "ghost"}
                                        className={`w-full justify-start h-12 px-4 transition-all ${
                                            selectedGroup.id === group.id
                                                ? "bg-gray-700/80 border border-gray-600 shadow-lg"
                                                : "hover:bg-gray-700/40"
                                        }`}
                                    >
                                        <div className="flex items-center gap-3 w-full">
                                            <div className={`w-2 h-2 rounded-full ${
                                                selectedGroup.id === group.id ? "bg-primary" : "bg-gray-500"
                                            }`}/>
                                            <span className="truncate flex-1 text-left">{group.name}</span>
                                            <span className="text-xs bg-gray-700/70 rounded-full px-2 py-1">
                                                        {apps.filter(app => app.group == group.id).length}
                                                    </span>
                                        </div>
                                    </Button>
                                ))}
                            </div>
                        </ScrollArea>
                    </CardContent>
                </Card>
            </div>
        </div>
    )
}