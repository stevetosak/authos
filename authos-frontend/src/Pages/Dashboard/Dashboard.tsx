import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, Grid, Users } from "lucide-react";
import { useAuth } from "@/services/useAuth.ts";
import { UserSidebar } from "@/components/my/user-sidebar.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog.tsx";
import {MainLeftSidebar} from "@/components/MainLeftSidebar.tsx";
import Layout from "@/components/Layout.tsx";
import {useNavigate} from "react-router-dom";

const Dashboard: React.FC = () => {
    const { user, isAuthenticated } = useAuth();
    const [showUserSidebar, setShowUserSidebar] = useState(true);
    const nav = useNavigate();

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
            <div className="min-h-screen bg-gray-900 text-white p-8">
                <div className="flex flex-col lg:flex-row gap-6">
                    {/*<aside className="w-64 h-[90vh] overflow-y-auto shrink-0 bg-gray-800 border border-gray-700 rounded-xl shadow-md p-6">*/}
                    {/*    <nav className="flex flex-col gap-4">*/}
                    {/*        <button className="text-left hover:text-green-500">Home</button>*/}
                    {/*        <button className="text-left hover:text-green-500">My Apps</button>*/}
                    {/*        <button className="text-left hover:text-green-500">Settings</button>*/}
                    {/*        <button onClick={toggleSidebar} className="text-left text-green-400 hover:text-green-300">*/}
                    {/*            {showUserSidebar ? "Hide" : "Show"} Profile*/}
                    {/*        </button>*/}
                    {/*    </nav>*/}
                    {/*</aside>*/}


                    {/* Main Dashboard Area */}
                    <div className="flex-1">
                        {/* Header */}
                        <header
                            className="flex justify-between items-center p-6 bg-gray-800 shadow-md border border-gray-700 rounded-xl">
                            <h1 className="text-2xl font-bold">Dashboard</h1>
                            <Dialog>
                                <DialogTrigger asChild>
                                    <Button className="bg-green-600 hover:bg-green-500 text-white">
                                        <Plus className="w-5 h-5 mr-2"/> Add Application
                                    </Button>
                                </DialogTrigger>

                                <DialogContent
                                    className="max-w-screen-2xl w-[90vw] bg-gray-900 border border-gray-700 text-white p-0 rounded-xl shadow-xl"
                                >
                                    <ClientRegistration/>
                                </DialogContent>
                            </Dialog>
                        </header>

                        {/* Main Content */}
                        <div className="grid md:grid-cols-2 gap-6 mt-8">
                            {/* Applications Section */}
                            <Card className="bg-gray-800 border border-gray-700 shadow-md">
                                <CardHeader>
                                    <CardTitle className="flex items-center gap-2">
                                        <Grid className="text-green-500 w-6 h-6"/> Registered Applications
                                    </CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <ul className="flex flex-col gap-4">
                                        {user.appGroups.flatMap(group => group.apps).map(app => (
                                            <li key={app.id}>
                                                <div
                                                    className="bg-gray-800 text-gray-300 p-2 rounded-lg shadow hover:shadow-lg hover:bg-gray-700 transition cursor-pointer"
                                                    onClick={() => handleAppClick(app.id)}
                                                >
                                                    <h3 className="text-xl font-semibold">{app.name}</h3>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                </CardContent>
                            </Card>

                            {/* Groups Section */}
                            <Card className="bg-gray-800 border border-gray-700 shadow-md">
                                <CardHeader>
                                    <CardTitle className="flex items-center gap-2">
                                        <Users className="text-green-500 w-6 h-6"/> Application Groups
                                    </CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <ul className="text-gray-300">
                                        {user.appGroups.map(group => (
                                            <li key={group.id}>
                                                <div
                                                    className="bg-gray-800 text-gray-300 p-2 rounded-lg shadow hover:shadow-lg hover:bg-gray-700 transition cursor-pointer"
                                                >
                                                    <h3 className="text-xl font-semibold">{group.name}</h3>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                </CardContent>
                            </Card>
                        </div>
                    </div>

                    <UserSidebar user={user}/>
                </div>
            </div>
        </Layout>

    );
};

export default Dashboard;
