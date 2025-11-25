import {
    Lock,
    Mail,
    User,
    Shield,
    Key,
    Smartphone,
    Download,
    Copy,
    QrCode,
    RotateCw,
    Edit,
    AlertTriangle
} from "lucide-react";
import {useEffect, useState} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {AccountTab} from "@/Pages/UserProfilePage/AccountTab.tsx";
import {SecurityTab} from "@/Pages/UserProfilePage/SecurityTab.tsx";

export const ProfilePage = () => {
    const {user} = useAuth()
    const [activeTab, setActiveTab] = useState('account');

    return (
        <div className="min-h-screen text-white p-4 md:p-8">
            <div className="max-w-6xl mx-auto">
                <div className="flex flex-col md:flex-row gap-6 mb-8">
                    <div className="flex-shrink-0">
                        <div className="relative">
                            <div
                                className="w-24 h-24 rounded-full bg-gray-800 border-2 border-emerald-400/30 flex items-center justify-center">
                                <User className="w-12 h-12 text-gray-400"/>
                            </div>
                            <div
                                className="absolute bottom-0 right-0 bg-emerald-500 rounded-full p-1.5 border-2 border-gray-900">
                                <Edit className="w-4 h-4 text-white"/>
                            </div>
                        </div>
                    </div>
                    <div className="flex-1">
                        <h1 className="text-2xl md:text-3xl font-bold">{user.firstName}</h1>
                        <p className="text-gray-400 flex items-center gap-2 mt-1">
                            <Mail className="w-4 h-4"/> {user.email}
                        </p>
                        <p className="text-sm text-gray-500 mt-2">
                            Last login: {new Date(user.lastLoginAt).toLocaleString()}
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                    <div className="lg:col-span-1">
                        <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                            <CardContent className="p-4">
                                <nav className="space-y-1">
                                    {[
                                        {id: 'account', icon: User, label: 'Account'},
                                        {id: 'security', icon: Lock, label: 'Security'},
                                        {id: 'sessions', icon: Shield, label: 'Sessions'},
                                        {id: 'connected', icon: Key, label: 'Connected Apps'}
                                    ].map((tab) => (
                                        <button
                                            key={tab.id}
                                            onClick={() => setActiveTab(tab.id)}
                                            className={`w-full flex items-center gap-3 px-4 py-3 rounded-md text-left transition-colors ${
                                                activeTab === tab.id
                                                    ? 'bg-gray-700/50 text-emerald-400'
                                                    : 'text-gray-300 hover:bg-gray-700/30'
                                            }`}
                                        >
                                            <tab.icon className="w-5 h-5"/>
                                            <span>{tab.label}</span>
                                        </button>
                                    ))}
                                </nav>
                            </CardContent>
                        </Card>
                    </div>

                    <div className="lg:col-span-3 space-y-6">

                        <AccountTab active={activeTab === "account"} user={user}/>
                        <SecurityTab active={activeTab === "security"} user={user}/>

                        {activeTab === 'sessions' && (
                            <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                <CardHeader className="border-b border-gray-700/50">
                                    <CardTitle className="flex items-center gap-2">
                                        <Shield className="w-5 h-5 text-emerald-400"/>
                                        Active Sessions
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-6">
                                    <div className="space-y-4">
                                        {[1, 2, 3].map((session) => (
                                            <div key={session}
                                                 className="flex items-center justify-between p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
                                                <div>
                                                    <div className="flex items-center gap-2">
                                                        <p className="font-medium">Chrome on Windows</p>
                                                        {session === 1 && (
                                                            <Badge
                                                                className="bg-emerald-500/10 text-emerald-400 px-2 py-0.5 text-xs">
                                                                Current
                                                            </Badge>
                                                        )}
                                                    </div>
                                                    <p className="text-sm text-gray-400">
                                                        Last active: {new Date().toLocaleString()}
                                                    </p>
                                                    <p className="text-sm text-gray-400">
                                                        IP: 192.168.1.{session}
                                                    </p>
                                                </div>
                                                {session !== 1 && (
                                                    <Button variant="outline" size="sm" className="border-gray-600">
                                                        Revoke
                                                    </Button>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                    <div className="mt-6">
                                        <Button variant="outline" className="border-gray-600 w-full">
                                            Revoke All Other Sessions
                                        </Button>
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        {activeTab === 'connected' && (
                            <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                <CardHeader className="border-b border-gray-700/50">
                                    <CardTitle className="flex items-center gap-2">
                                        <Key className="w-5 h-5 text-emerald-400"/>
                                        Authorized Applications
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-6">
                                    <div className="space-y-4">
                                        {[1, 2].map((app) => (
                                            <div key={app}
                                                 className="flex items-center justify-between p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
                                                <div>
                                                    <p className="font-medium">Application {app}</p>
                                                    <p className="text-sm text-gray-400">
                                                        Last accessed: {new Date().toLocaleDateString()}
                                                    </p>
                                                    <div className="flex gap-2 mt-2">
                                                        <Badge variant="outline" className="border-gray-600 text-xs">
                                                            Read: Profile
                                                        </Badge>
                                                        <Badge variant="outline" className="border-gray-600 text-xs">
                                                            Read: Email
                                                        </Badge>
                                                    </div>
                                                </div>
                                                <Button variant="outline" size="sm" className="border-gray-600">
                                                    Revoke
                                                </Button>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        )}
                    </div>
                </div>
            </div>

        </div>
    );
};