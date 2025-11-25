import React, {useState} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";

import {GroupHeader} from "@/Pages/Dashboard/components/GroupHeader.tsx";
import {GroupInputs} from "@/Pages/Dashboard/components/GroupInputs.tsx";
import {AppsList} from "@/Pages/Dashboard/components/AppsList.tsx";
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