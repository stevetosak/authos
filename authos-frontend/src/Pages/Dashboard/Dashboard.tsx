import React, {useState} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Users, CheckIcon, XIcon} from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {useNavigate} from "react-router-dom";
import {Badge} from "@/components/ui/badge.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {ScrollArea} from "@/components/ui/scroll-area";
import {AddGroupModal} from "@/Pages/components/AddGroupModal.tsx";
import {AppGroup, AppGroupEditableField, defaultAppGroup} from "@/services/types.ts";
import {useGroupManagement} from "@/Pages/components/hooks/useGroupManagment.ts";
import {GroupHeader} from "@/Pages/Dashboard/components/GroupHeader.tsx";
import {GroupInputs} from "@/Pages/Dashboard/components/GroupInputs.tsx";
import {AppsList} from "@/Pages/Dashboard/components/AppsList.tsx";
import {apiPostAuthenticated} from "@/services/netconfig.ts";
import {toast} from "sonner";
import {DashboardContext} from "@/Pages/Dashboard/components/context/DashboardContext.ts";
import {DashboardContextProvider} from "@/Pages/Dashboard/components/context/DashboardContextProvider.tsx";
import {AppGroupSidebar} from "@/Pages/Dashboard/components/AppGroupSidebar.tsx";

const Dashboard: React.FC = () => {

    return (
        <div className="min-h-screen text-white py-8 w-full">
            <div className="flex flex-col lg:flex-row gap-4 sm:gap-6">
                <DashboardContextProvider>
                    {/* Groups Sidebar */}
                    <AppGroupSidebar/>

                    {/* Main Content */}

                    <div className="flex-1">
                        <div className="top-0 z-10 pb-6 pt-2">
                            <Card className="border bg-gray-900/70 border-gray-700/50 backdrop-blur-sm">
                                <CardHeader className="pb-3">
                                    <div id="group-section" className="space-y-4">
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full">
                                            <GroupHeader/>
                                            <GroupInputs/>
                                        </div>
                                    </div>
                                </CardHeader>
                            </Card>
                        </div>

                        <AppsList/>
                    </div>

                </DashboardContextProvider>


            </div>
        </div>
    );
};

export default Dashboard;