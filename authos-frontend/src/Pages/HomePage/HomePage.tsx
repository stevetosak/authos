import React from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
    ShieldCheck,
    Lock,
    LayoutDashboard,
    BookOpen,
    CheckCircle,
    Activity,
    Shield, EyeOff, Key, UserPlus
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import Layout from "@/Pages/components/Layout.tsx";
import { motion } from "framer-motion";
import {Badge} from "@/components/ui/badge.tsx";

const HomePage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <Layout>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white">
                <section className="relative overflow-hidden py-24 px-4 sm:px-6 lg:px-8">
                    <div className="max-w-7xl mx-auto text-center">
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.6 }}
                        >
                            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-emerald-400 to-green-300">
                                AuthOS
                            </h1>
                            <p className="mt-6 text-xl text-gray-300 max-w-3xl mx-auto">
                                Secure, scalable authentication with SSO, OAuth, and advanced user management.
                            </p>
                            <div className="mt-10 flex flex-col sm:flex-row justify-center gap-4">
                                <Button
                                    className="bg-emerald-600 hover:bg-emerald-500 text-white px-8 py-6 text-lg font-medium transition-all hover:shadow-lg hover:shadow-emerald-500/20"
                                    onClick={() => navigate("/dashboard")}
                                >
                                    <LayoutDashboard className="w-6 h-6 mr-2" />
                                    Go to Dashboard
                                </Button>
                                <Button
                                    variant="outline"
                                    className="border-emerald-400 text-emerald-400 hover:bg-emerald-400/10 px-8 py-6 text-lg font-medium"
                                >
                                    <BookOpen className="w-6 h-6 mr-2" />
                                    Documentation
                                </Button>
                            </div>
                        </motion.div>
                    </div>
                </section>

                {/* Features Section */}
                <section className="py-20 px-4 sm:px-6 lg:px-8 bg-gray-900/50">
                    <div className="max-w-7xl mx-auto">
                        <div className="text-center mb-16">
                            <Badge className="bg-emerald-500/10 text-emerald-400 px-4 py-1.5 text-sm font-medium mb-4">
                                ENTERPRISE FEATURES
                            </Badge>
                            <h2 className="text-3xl md:text-4xl font-bold text-white">
                                Everything You Need for Secure Authentication
                            </h2>
                        </div>

                        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                            <motion.div
                                whileHover={{ y: -5 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm h-full">
                                    <CardContent className="p-8">
                                        <div className="bg-emerald-500/10 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-6">
                                            <ShieldCheck className="text-emerald-400 w-6 h-6" />
                                        </div>
                                        <h3 className="text-xl font-semibold mb-3">Single Sign-On</h3>
                                        <p className="text-gray-300">
                                            Enable seamless authentication across all your applications with OpenID Connect.
                                        </p>
                                        <ul className="mt-4 space-y-2 text-gray-400 text-sm">
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-emerald-400 mr-2" />
                                                Centralized identity management
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-emerald-400 mr-2" />
                                                Analytics
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-emerald-400 mr-2" />
                                                Session management
                                            </li>
                                        </ul>
                                    </CardContent>
                                </Card>
                            </motion.div>

                            <motion.div
                                whileHover={{ y: -5 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm h-full">
                                    <CardContent className="p-8">
                                        <div className="bg-blue-500/10 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-6">
                                            <Lock className="text-blue-400 w-6 h-6" />
                                        </div>
                                        <h3 className="text-xl font-semibold mb-3">OAuth & OIDC</h3>
                                        <p className="text-gray-300">
                                            Full OAuth 2.0 and OpenID Connect implementation with all standard flows.
                                        </p>
                                        <ul className="mt-4 space-y-2 text-gray-400 text-sm">
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-blue-400 mr-2" />
                                                Authorization Code Flow
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-blue-400 mr-2" />
                                                PKCE support
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-blue-400 mr-2" />
                                                Customizable consent screens
                                            </li>
                                        </ul>
                                    </CardContent>
                                </Card>
                            </motion.div>

                            <motion.div
                                whileHover={{ y: -5 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm h-full">
                                    <CardContent className="p-8">
                                        <div className="bg-purple-500/10 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-6">
                                            <Activity className="text-purple-400 w-6 h-6" />
                                        </div>
                                        <h3 className="text-xl font-semibold mb-3">Analytics & Insights</h3>
                                        <p className="text-gray-300">
                                            Real-time monitoring and detailed analytics for your authentication traffic.
                                        </p>
                                        <ul className="mt-4 space-y-2 text-gray-400 text-sm">
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-purple-400 mr-2" />
                                                Login attempt monitoring
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-purple-400 mr-2" />
                                                Geo-location tracking
                                            </li>
                                            <li className="flex items-center">
                                                <CheckCircle className="w-4 h-4 text-purple-400 mr-2" />
                                                Anomaly detection
                                            </li>
                                        </ul>
                                    </CardContent>
                                </Card>
                            </motion.div>
                        </div>
                    </div>
                </section>

                <section className="py-20 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-gray-900 to-gray-950">
                    <div className="max-w-7xl mx-auto">
                        <div className="text-center mb-16">
                            <Badge className="bg-indigo-500/10 text-indigo-400 px-4 py-1.5 text-sm font-medium mb-4">
                                PRIVACY FIRST
                            </Badge>
                            <h2 className="text-3xl md:text-4xl font-bold text-white">
                                Your privacy is important
                            </h2>
                        </div>

                        <div className="grid md:grid-cols-2 gap-8 items-center">
                            <div>
                                <div className="space-y-8">
                                    <div className="flex">
                                        <div className="flex-shrink-0">
                                            <div className="bg-emerald-500/10 p-2 rounded-lg">
                                                <Shield className="w-6 h-6 text-emerald-400" />
                                            </div>
                                        </div>
                                        <div className="ml-4">
                                            <h3 className="text-lg font-medium text-white">Pairwise identifiers</h3>
                                            <p className="mt-1 text-gray-300">
                                                No one can correlate your activity between verified Authos applications
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex">
                                        <div className="flex-shrink-0">
                                            <div className="bg-blue-500/10 p-2 rounded-lg">
                                                <EyeOff className="w-6 h-6 text-blue-400" />
                                            </div>
                                        </div>
                                        <div className="ml-4">
                                            <h3 className="text-lg font-medium text-white">Brute Force Protection</h3>
                                            <p className="mt-1 text-gray-300">
                                                Automated detection and prevention of credential stuffing and brute force attacks.
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex">
                                        <div className="flex-shrink-0">
                                            <div className="bg-purple-500/10 p-2 rounded-lg">
                                                <Key className="w-6 h-6 text-purple-400" />
                                            </div>
                                        </div>
                                        <div className="ml-4">
                                            <h3 className="text-lg font-medium text-white">Passwordless Auth</h3>
                                            <p className="mt-1 text-gray-300">
                                                Support for WebAuthn, magic links, and biometric authentication.
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="relative">
                                <div className="relative bg-gray-800/50 border border-gray-700/50 rounded-xl p-8 backdrop-blur-sm">
                                    <div className="absolute -inset-1 bg-gradient-to-r from-emerald-500 to-blue-500 rounded-xl opacity-20 blur"></div>
                                    <div className="relative">
                                        <h3 className="text-xl font-semibold text-white mb-4">Get Started in Minutes</h3>
                                        <p className="text-gray-300 mb-6">
                                            Integrate with our API or use our pre-built components to add authentication to your apps.
                                        </p>
                                        <Button
                                            className="w-full bg-emerald-600 hover:bg-emerald-500 text-white py-3 text-lg"
                                            onClick={() => navigate("/register")}
                                        >
                                            <UserPlus className="w-5 h-5 mr-2" />
                                            Create Free Account
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Footer */}
                <footer className="border-t border-gray-800 py-12 px-4 sm:px-6 lg:px-8">
                    <div className="max-w-7xl mx-auto">
                        <div className="flex flex-col md:flex-row justify-between items-center">
                            <div className="flex items-center space-x-2 mb-4 md:mb-0">
                                <Key className="w-8 h-8 text-emerald-400" />
                                <span className="text-xl font-bold text-white">AuthOS</span>
                            </div>
                            <div className="flex space-x-6">
                                <a href="#" className="text-gray-400 hover:text-white">
                                    Documentation
                                </a>
                                <a href="#" className="text-gray-400 hover:text-white">
                                    API Reference
                                </a>
                                <a href="#" className="text-gray-400 hover:text-white">
                                    Support
                                </a>
                                <a href="#" className="text-gray-400 hover:text-white">
                                    Privacy
                                </a>
                            </div>
                        </div>
                        <div className="mt-8 pt-8 border-t border-gray-800 text-center text-gray-400 text-sm">
                            &copy; {new Date().getFullYear()} AuthOS. All rights reserved.
                        </div>
                    </div>
                </footer>
            </div>
        </Layout>
    );
};

export default HomePage;