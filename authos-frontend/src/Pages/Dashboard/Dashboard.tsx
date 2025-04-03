import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, Grid, Users } from "lucide-react";

const Dashboard: React.FC = () => {
    return (
        <div className="min-h-screen bg-gray-900 text-white p-8">
            {/* Header */}
            <header className="flex justify-between items-center p-6 bg-gray-800 shadow-md border border-gray-700 rounded-xl">
                <h1 className="text-2xl font-bold">Dashboard</h1>
                <Button className="bg-green-600 hover:bg-green-500 text-white flex items-center">
                    <Plus className="w-5 h-5 mr-2" /> Add Application
                </Button>
            </header>

            {/* Main Content */}
            <div className="grid md:grid-cols-2 gap-6 mt-8">
                {/* Applications Section */}
                <Card className="bg-gray-800 border border-gray-700 shadow-md">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Grid className="text-green-500 w-6 h-6" /> Registered Applications
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ul className="text-gray-300">
                            <li className="p-3 border-b border-gray-700">App 1 - OAuth Configured</li>
                            <li className="p-3 border-b border-gray-700">App 2 - SSO Enabled</li>
                            <li className="p-3">App 3 - Pending Setup</li>
                        </ul>
                    </CardContent>
                </Card>

                {/* Groups Section */}
                <Card className="bg-gray-800 border border-gray-700 shadow-md">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Users className="text-green-500 w-6 h-6" /> Application Groups
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ul className="text-gray-300">
                            <li className="p-3 border-b border-gray-700">Group 1 - 5 Applications</li>
                            <li className="p-3 border-b border-gray-700">Group 2 - 3 Applications</li>
                            <li className="p-3">Group 3 - 7 Applications</li>
                        </ul>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default Dashboard;