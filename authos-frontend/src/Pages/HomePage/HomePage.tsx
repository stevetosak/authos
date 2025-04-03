import React from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ShieldCheck, Lock, Settings, LogIn, LayoutDashboard } from "lucide-react";
import { useNavigate } from "react-router-dom";

const HomePage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white">
            {/* Header */}
            <header className="w-full flex justify-between items-center p-6 bg-gray-800 text-white shadow-md">
                <h1 className="text-2xl font-bold">Authos</h1>
                <div>
                    <Button
                        className="bg-green-600 text-white hover:bg-green-500 flex items-center"
                        onClick={() => navigate("/login")}
                    >
                        <LogIn className="w-5 h-5 mr-2" />
                        Login
                    </Button>
                </div>
            </header>

            {/* Hero Section */}
            <section className="text-center my-12">
                <h1 className="text-4xl font-bold">Enable SSO & OAuth Effortlessly</h1>
                <p className="mt-4 text-lg text-gray-300">
                    Configure authentication for your apps and provide seamless access with Single Sign-On.
                </p>
                <Button
                    className="mt-6 bg-green-600 hover:bg-green-500 text-white px-6 py-3 text-lg"
                    onClick={() => navigate("/dashboard")}
                >
                    <LayoutDashboard className="w-5 h-5 mr-2" />
                    Go to Dashboard
                </Button>
            </section>

            {/* Features Section */}
            <section className="grid md:grid-cols-3 gap-6 px-8">
                <Card className="shadow-md bg-gray-800">
                    <CardContent className="flex flex-col items-center p-6">
                        <ShieldCheck className="text-green-500 w-12 h-12 mb-3" />
                        <h3 className="text-xl font-semibold">Single Sign-On (SSO)</h3>
                        <p className="text-gray-300 text-center mt-2">
                            Enable secure and convenient authentication across multiple applications.
                        </p>
                    </CardContent>
                </Card>

                <Card className="shadow-md bg-gray-800">
                    <CardContent className="flex flex-col items-center p-6">
                        <Lock className="text-green-500 w-12 h-12 mb-3" />
                        <h3 className="text-xl font-semibold">OAuth Integration</h3>
                        <p className="text-gray-300 text-center mt-2">
                            Easily configure OAuth 2.0 to allow third-party applications to authenticate users.
                        </p>
                    </CardContent>
                </Card>

                <Card className="shadow-md bg-gray-800">
                    <CardContent className="flex flex-col items-center p-6">
                        <Settings className="text-green-500 w-12 h-12 mb-3" />
                        <h3 className="text-xl font-semibold">App Management</h3>
                        <p className="text-gray-300 text-center mt-2">
                            Manage and customize authentication settings for your registered applications.
                        </p>
                    </CardContent>
                </Card>
            </section>

            {/* Footer */}
            <footer className="w-full p-4 text-center text-gray-400 mt-12">
                &copy; {new Date().getFullYear()} Authos. All rights reserved.
            </footer>
        </div>
    );
};

export default HomePage;