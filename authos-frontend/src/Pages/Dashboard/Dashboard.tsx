import React, { useEffect, useState } from "react";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {Plus, Grid, Users, FolderOpen, Settings} from "lucide-react";
import { useAuth } from "@/services/useAuth.ts";
import { UserSidebar } from "@/components/my/user-sidebar.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog.tsx";
import {motion} from "framer-motion"
import Layout from "@/components/Layout.tsx";
import {useNavigate} from "react-router-dom";
import {Avatar, AvatarFallback} from "@/components/ui/avatar.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import { ScrollArea } from "@/components/ui/scroll-area";

const Dashboard: React.FC = () => {
    const { user, isAuthenticated } = useAuth();
    const [showUserSidebar, setShowUserSidebar] = useState(true);
    const nav = useNavigate();
    const [selectedGroup,setSelectedGroup] = useState<number>(0)

    useEffect(() => {
        console.log("USER:::   " + JSON.stringify(user));
        console.log("IS AUTH:" + isAuthenticated);
    }, [user, isAuthenticated]);

    const toggleSidebar = () => setShowUserSidebar(prev => !prev);

    const handleAppClick = (appId: number) => {
        nav(`/dashboard/${appId}`)
    };

    return (
        <Layout>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white p-4 sm:p-8 w-full">
                <div className="flex flex-col lg:flex-row gap-4 sm:gap-6 max-w-7xl mx-auto">
                    {/* Groups Sidebar - Sticky with fade effect */}
                    <div className="lg:w-72 flex-shrink-0">
                        <div className="sticky top-6 h-[calc(100vh-3rem)] overflow-hidden">
                            <div className="absolute inset-0 bg-gradient-to-b from-gray-800/50 to-transparent pointer-events-none" />
                            <Card className="h-full bg-gray-800/50 backdrop-blur-sm border border-gray-700/50 overflow-hidden">
                                <CardHeader className="border-b border-gray-700/50 pb-3">
                                    <div className="flex items-center justify-between">
                                        <CardTitle className="flex items-center gap-2 text-lg">
                                            <Users className="w-5 h-5 text-green-400" />
                                            Application Groups
                                        </CardTitle>
                                        <Tooltip>
                                            <TooltipTrigger asChild>
                                                <Button variant="ghost" size="icon" className="w-8 h-8">
                                                    <Plus className="w-4 h-4 text-gray-400 hover:text-green-400" />
                                                </Button>
                                            </TooltipTrigger>
                                            <TooltipContent>Create new group</TooltipContent>
                                        </Tooltip>
                                    </div>
                                </CardHeader>
                                <CardContent className="p-2">
                                    <ScrollArea className="h-[calc(100%-60px)] pr-2">
                                        <div className="space-y-1">
                                            {user.appGroups.map((group) => (
                                                <Button
                                                    key={group.id}
                                                    onClick={() => setSelectedGroup(group.id)}
                                                    variant={selectedGroup === group.id ? "secondary" : "ghost"}
                                                    className={`w-full justify-start h-12 px-4 transition-all ${
                                                        selectedGroup === group.id
                                                            ? "bg-gray-700/80 border border-gray-600 shadow-lg"
                                                            : "hover:bg-gray-700/40"
                                                    }`}
                                                >
                                                    <div className="flex items-center gap-3 w-full">
                                                        <div className={`w-2 h-2 rounded-full ${
                                                            selectedGroup === group.id ? "bg-green-400" : "bg-gray-500"
                                                        }`} />
                                                        <span className="truncate flex-1 text-left">{group.name}</span>
                                                        <span className="text-xs bg-gray-700/70 rounded-full px-2 py-1">
                          {group.apps.length}
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

                    {/* Main Content Area */}
                    <div className="flex-1">
                        <div className="sticky top-0 z-10 bg-gradient-to-b from-gray-900/90 to-transparent pb-6 pt-2">
                            <Card className="border border-gray-700/50 bg-gray-800/50 backdrop-blur-sm">
                                <CardHeader className="pb-3">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <CardTitle className="text-xl">
                                                {selectedGroup
                                                    ? user.appGroups.find((g) => g.id === selectedGroup)?.name
                                                    : "All Applications"}
                                            </CardTitle>
                                            <CardDescription className="text-gray-400">
                                                {selectedGroup
                                                    ? "Single Sign-On enabled for these applications"
                                                    : "All registered applications"}
                                            </CardDescription>
                                        </div>
                                    </div>
                                </CardHeader>
                            </Card>
                        </div>

                        {/* Applications Grid with animated cards */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                            {(selectedGroup
                                    ? user.appGroups.find((g) => g.id === selectedGroup)?.apps
                                    : user.appGroups.flatMap((g) => g.apps)
                            )?.map((app) => (
                                <motion.div
                                    key={app.id}
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ duration: 0.2 }}
                                    whileHover={{ y: -3 }}
                                >
                                    <Card className="h-full bg-gray-800/60 border border-gray-700/50 hover:border-green-400/30 transition-colors group overflow-hidden">
                                        <CardHeader className="pb-3">
                                            <div className="flex items-start justify-between">
                                                <div>
                                                    <CardTitle className="flex items-center gap-2">
                                                        <div className="w-3 h-3 rounded-full bg-green-400 flex-shrink-0" />
                                                        {app.name}
                                                    </CardTitle>
                                                    <CardDescription className="text-gray-400 mt-1 line-clamp-2">
                                                        {app.shortDescription || "No description provided"}
                                                    </CardDescription>
                                                </div>
                                                <Avatar className="h-10 w-10 border border-gray-600">
                                                    <AvatarFallback className="bg-gray-700/50 group-hover:bg-green-400/10 transition-colors">
                                                        {app.name.charAt(0)}
                                                    </AvatarFallback>
                                                </Avatar>
                                            </div>
                                        </CardHeader>
                                        <CardContent className="pb-4">
                                            <div className="flex gap-2 flex-wrap">
                                                <Badge variant="outline" className="text-xs border-gray-600 text-gray-300">
                                                    {app.type || "OIDC"}
                                                </Badge>
                                                <Badge variant="outline" className="text-xs border-blue-500/30 text-blue-400">
                                                    {app.status || "Active"}
                                                </Badge>
                                            </div>
                                        </CardContent>
                                        <CardFooter className="border-t border-gray-700/50 pt-3">
                                            <Button
                                                variant="outline"
                                                className="w-full border-gray-600 hover:bg-gray-700/50 hover:border-green-400/30 group-hover:shadow-[0_0_15px_-3px_rgba(74,222,128,0.3)] transition-all"
                                                onClick={() => handleAppClick(app.id)}
                                            >
                                                <Settings className="w-4 h-4 mr-2 opacity-70 group-hover:opacity-100 transition-opacity" />
                                                Manage
                                            </Button>
                                        </CardFooter>
                                    </Card>
                                </motion.div>
                            ))}
                        </div>

                        {/* Empty state */}
                        {!user.appGroups.some((g) => g.apps.length > 0) && (
                            <motion.div
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 1 }}
                                className="flex flex-col items-center justify-center py-16 text-center"
                            >
                                <div className="bg-gray-800/50 border border-gray-700/50 rounded-full p-6 mb-6">
                                    <FolderOpen className="w-10 h-10 text-gray-500" />
                                </div>
                                <h3 className="text-xl font-medium mb-2">No applications found</h3>
                                <p className="text-gray-400 max-w-md mb-6">
                                    Get started by adding your first application to this group
                                </p>
                                <Button  className="gap-2 bg-green-600 hover:bg-green-500/90">
                                    <Plus className="w-4 h-4" />
                                    Add Application
                                </Button>
                            </motion.div>
                        )}
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default Dashboard;
