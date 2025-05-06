import React, {useEffect} from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, Grid, Users } from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {UserSidebar} from "@/components/my/user-sidebar.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";
import {Dialog, DialogContent, DialogTrigger} from "@/components/ui/dialog.tsx";

const Dashboard: React.FC = () => {

    const {user,isAuthenticated,appGroups} = useAuth()
    useEffect(() => {
        console.log("USER:::   " + JSON.stringify(user))
        console.log("IS AUTH:" + isAuthenticated)
        console.log("Groups: ", JSON.stringify(appGroups))
    })

    return (
        <div className="min-h-screen bg-gray-900 text-white p-8">
            <div className="flex flex-col lg:flex-row gap-6">
                {/* Main Dashboard Area */}
                <div className="flex-1">
                    {/* Header */}
                    <header className="flex justify-between items-center p-6 bg-gray-800 shadow-md border border-gray-700 rounded-xl">
                        <h1 className="text-2xl font-bold">Dashboard</h1>
                        <Dialog>
                            <DialogTrigger asChild>
                                <Button className="bg-green-600 hover:bg-green-500 text-white">
                                    <Plus className="w-5 h-5 mr-2" /> Add Application
                                </Button>
                            </DialogTrigger>

                            <DialogContent
                                className="max-w-screen-2xl w-[90vw] bg-gray-900 border border-gray-700 text-white p-0 rounded-xl shadow-xl"
                            >
                                <ClientRegistration />
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
                                    {user.apps.map(app => (
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
                                    {appGroups.map(group => (
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

                {/* Right Sidebar for UserProfile */}
                <aside className="w-full lg:w-32 shrink-0 bg-gray-800 border border-gray-700 rounded-xl shadow-md p-6">
                    <UserSidebar user={user}>

                    </UserSidebar>
                </aside>
            </div>
        </div>

    );
};

export default Dashboard;