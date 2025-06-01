import Layout from "@/Pages/components/Layout.tsx";
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
import {useState} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Switch} from "@/components/ui/switch.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog.tsx";

export const ProfilePage = () => {
    const [activeTab, setActiveTab] = useState('account');
    const [showMfaSetup, setShowMfaSetup] = useState(false);
    const [recoveryCodes, setRecoveryCodes] = useState([""]);
    const [isMfaEnabled, setIsMfaEnabled] = useState(false);

    // Mock user data
    const user = {
        name: "Alex Johnson",
        email: "alex.johnson@example.com",
        lastLogin: "2023-06-15T14:30:00Z",
        mfaEnabled: false,
        linkedAccounts: [
            { provider: "google", email: "alex@gmail.com" },
            { provider: "github", email: "alex@github.com" }
        ]
    };

    const generateRecoveryCodes = () => {
        const codes = Array.from({ length: 10 }, () =>
            Math.random().toString(36).slice(2, 10).toUpperCase()
        );
        setRecoveryCodes(codes);
    };

    return (
        <Layout>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white p-4 md:p-8">
                <div className="max-w-6xl mx-auto">
                    {/* Profile Header */}
                    <div className="flex flex-col md:flex-row gap-6 mb-8">
                        <div className="flex-shrink-0">
                            <div className="relative">
                                <div className="w-24 h-24 rounded-full bg-gray-800 border-2 border-emerald-400/30 flex items-center justify-center">
                                    <User className="w-12 h-12 text-gray-400" />
                                </div>
                                <div className="absolute bottom-0 right-0 bg-emerald-500 rounded-full p-1.5 border-2 border-gray-900">
                                    <Edit className="w-4 h-4 text-white" />
                                </div>
                            </div>
                        </div>
                        <div className="flex-1">
                            <h1 className="text-2xl md:text-3xl font-bold">{user.name}</h1>
                            <p className="text-gray-400 flex items-center gap-2 mt-1">
                                <Mail className="w-4 h-4" /> {user.email}
                            </p>
                            <p className="text-sm text-gray-500 mt-2">
                                Last login: {new Date(user.lastLogin).toLocaleString()}
                            </p>
                        </div>
                    </div>

                    {/* Main Content */}
                    <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                        {/* Sidebar Navigation */}
                        <div className="lg:col-span-1">
                            <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                <CardContent className="p-4">
                                    <nav className="space-y-1">
                                        {[
                                            { id: 'account', icon: User, label: 'Account' },
                                            { id: 'security', icon: Lock, label: 'Security' },
                                            { id: 'sessions', icon: Shield, label: 'Sessions' },
                                            { id: 'connected', icon: Key, label: 'Connected Apps' }
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
                                                <tab.icon className="w-5 h-5" />
                                                <span>{tab.label}</span>
                                            </button>
                                        ))}
                                    </nav>
                                </CardContent>
                            </Card>
                        </div>

                        {/* Main Content Area */}
                        <div className="lg:col-span-3 space-y-6">
                            {/* Account Tab */}
                            {activeTab === 'account' && (
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                    <CardHeader className="border-b border-gray-700/50">
                                        <CardTitle className="flex items-center gap-2">
                                            <User className="w-5 h-5 text-emerald-400" />
                                            Account Information
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent className="p-6 space-y-6">
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                            <div>
                                                <Label className="text-gray-300 mb-2 block">Full Name</Label>
                                                <Input
                                                    defaultValue={user.name}
                                                    className="bg-gray-700 border-gray-600 text-white"
                                                />
                                            </div>
                                            <div>
                                                <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                                    <Mail className="w-4 h-4" /> Email Address
                                                </Label>
                                                <Input
                                                    defaultValue={user.email}
                                                    className="bg-gray-700 border-gray-600 text-white"
                                                    disabled
                                                />
                                            </div>
                                        </div>
                                        <div className="flex justify-end">
                                            <Button className="bg-emerald-600 hover:bg-emerald-500">
                                                Update Profile
                                            </Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            )}

                            {/* Security Tab */}
                            {activeTab === 'security' && (
                                <div className="space-y-6">
                                    {/* Password Change */}
                                    <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                        <CardHeader className="border-b border-gray-700/50">
                                            <CardTitle className="flex items-center gap-2">
                                                <Lock className="w-5 h-5 text-emerald-400" />
                                                Password
                                            </CardTitle>
                                        </CardHeader>
                                        <CardContent className="p-6 space-y-4">
                                            <div>
                                                <Label className="text-gray-300 mb-2 block">Current Password</Label>
                                                <Input
                                                    type="password"
                                                    className="bg-gray-700 border-gray-600 text-white"
                                                />
                                            </div>
                                            <div>
                                                <Label className="text-gray-300 mb-2 block">New Password</Label>
                                                <Input
                                                    type="password"
                                                    className="bg-gray-700 border-gray-600 text-white"
                                                />
                                            </div>
                                            <div>
                                                <Label className="text-gray-300 mb-2 block">Confirm New Password</Label>
                                                <Input
                                                    type="password"
                                                    className="bg-gray-700 border-gray-600 text-white"
                                                />
                                            </div>
                                            <div className="flex justify-end">
                                                <Button className="bg-emerald-600 hover:bg-emerald-500">
                                                    Change Password
                                                </Button>
                                            </div>
                                        </CardContent>
                                    </Card>

                                    {/* Multi-Factor Authentication */}
                                    <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                        <CardHeader className="border-b border-gray-700/50">
                                            <div className="flex justify-between items-center">
                                                <CardTitle className="flex items-center gap-2">
                                                    <Shield className="w-5 h-5 text-emerald-400" />
                                                    Multi-Factor Authentication
                                                </CardTitle>
                                                <Switch
                                                    checked={isMfaEnabled}
                                                    onCheckedChange={setIsMfaEnabled}
                                                    className="data-[state=checked]:bg-emerald-500"
                                                />
                                            </div>
                                        </CardHeader>
                                        <CardContent className="p-6 space-y-6">
                                            {isMfaEnabled ? (
                                                <div className="space-y-4">
                                                    <div className="flex items-center gap-3 p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
                                                        <Smartphone className="w-5 h-5 text-emerald-400" />
                                                        <div>
                                                            <p className="font-medium">Authenticator App</p>
                                                            <p className="text-sm text-gray-400">Configured with Google Authenticator</p>
                                                        </div>
                                                    </div>

                                                    {recoveryCodes.length > 0 ? (
                                                        <div className="space-y-4">
                                                            <div className="bg-gray-900/50 p-4 rounded-lg border border-red-400/30">
                                                                <h4 className="font-medium text-red-400 flex items-center gap-2">
                                                                    <AlertTriangle className="w-4 h-4" />
                                                                    Save Your Recovery Codes
                                                                </h4>
                                                                <p className="text-sm text-gray-400 mt-2">
                                                                    These codes can be used to access your account if you lose access to your authenticator app.
                                                                </p>
                                                            </div>

                                                            <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
                                                                {recoveryCodes.map((code, i) => (
                                                                    <div key={i} className="font-mono text-sm bg-gray-800 p-2 rounded text-center">
                                                                        {code}
                                                                    </div>
                                                                ))}
                                                            </div>

                                                            <div className="flex gap-3">
                                                                <Button variant="outline" className="border-gray-600">
                                                                    <Download className="w-4 h-4 mr-2" />
                                                                    Download
                                                                </Button>
                                                                <Button variant="outline" className="border-gray-600">
                                                                    <Copy className="w-4 h-4 mr-2" />
                                                                    Copy
                                                                </Button>
                                                                <Button variant="outline" className="border-gray-600">
                                                                    <RotateCw className="w-4 h-4 mr-2" />
                                                                    Regenerate
                                                                </Button>
                                                            </div>
                                                        </div>
                                                    ) : (
                                                        <Button
                                                            onClick={generateRecoveryCodes}
                                                            className="bg-emerald-600 hover:bg-emerald-500"
                                                        >
                                                            Generate Recovery Codes
                                                        </Button>
                                                    )}
                                                </div>
                                            ) : (
                                                <div className="space-y-4">
                                                    <p className="text-gray-400">
                                                        Add an extra layer of security to your account by enabling MFA. When enabled, you'll be required to enter both your password and an authentication code from your mobile device when logging in.
                                                    </p>
                                                    <Button
                                                        onClick={() => setShowMfaSetup(true)}
                                                        className="bg-emerald-600 hover:bg-emerald-500"
                                                    >
                                                        Set Up MFA
                                                    </Button>
                                                </div>
                                            )}
                                        </CardContent>
                                    </Card>

                                    {/* Linked Accounts */}
                                    <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                        <CardHeader className="border-b border-gray-700/50">
                                            <CardTitle className="flex items-center gap-2">
                                                <Key className="w-5 h-5 text-emerald-400" />
                                                Linked Accounts
                                            </CardTitle>
                                        </CardHeader>
                                        <CardContent className="p-6">
                                            <div className="space-y-4">
                                                {user.linkedAccounts.map((account) => (
                                                    <div key={account.provider} className="flex items-center justify-between p-3 bg-gray-700/30 rounded-lg">
                                                        <div className="flex items-center gap-3">
                                                            <img
                                                                src={`https://authjs.dev/img/providers/${account.provider}.svg`}
                                                                alt={account.provider}
                                                                className="w-6 h-6"
                                                            />
                                                            <div>
                                                                <p className="font-medium capitalize">{account.provider}</p>
                                                                <p className="text-sm text-gray-400">{account.email}</p>
                                                            </div>
                                                        </div>
                                                        <Button variant="outline" size="sm" className="border-gray-600 text-red-400 hover:bg-red-400/10">
                                                            Unlink
                                                        </Button>
                                                    </div>
                                                ))}
                                            </div>
                                        </CardContent>
                                    </Card>
                                </div>
                            )}

                            {/* Sessions Tab */}
                            {activeTab === 'sessions' && (
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                    <CardHeader className="border-b border-gray-700/50">
                                        <CardTitle className="flex items-center gap-2">
                                            <Shield className="w-5 h-5 text-emerald-400" />
                                            Active Sessions
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent className="p-6">
                                        <div className="space-y-4">
                                            {[1, 2, 3].map((session) => (
                                                <div key={session} className="flex items-center justify-between p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
                                                    <div>
                                                        <div className="flex items-center gap-2">
                                                            <p className="font-medium">Chrome on Windows</p>
                                                            {session === 1 && (
                                                                <Badge className="bg-emerald-500/10 text-emerald-400 px-2 py-0.5 text-xs">
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

                            {/* Connected Apps Tab */}
                            {activeTab === 'connected' && (
                                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                                    <CardHeader className="border-b border-gray-700/50">
                                        <CardTitle className="flex items-center gap-2">
                                            <Key className="w-5 h-5 text-emerald-400" />
                                            Authorized Applications
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent className="p-6">
                                        <div className="space-y-4">
                                            {[1, 2].map((app) => (
                                                <div key={app} className="flex items-center justify-between p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
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

                {/* MFA Setup Modal */}
                {showMfaSetup && (
                    <Dialog open={showMfaSetup} onOpenChange={setShowMfaSetup}>
                        <DialogContent className="bg-gray-800 border border-gray-700 max-w-md">
                            <DialogHeader>
                                <DialogTitle className="flex items-center gap-2">
                                    <Shield className="w-5 h-5 text-emerald-400" />
                                    Set Up Multi-Factor Authentication
                                </DialogTitle>
                            </DialogHeader>
                            <div className="space-y-6">
                                <div className="text-center">
                                    <p className="text-gray-300 mb-4">
                                        Scan this QR code with your authenticator app
                                    </p>
                                    <div className="flex justify-center p-4 bg-white rounded-lg mb-4">
                                        <QrCode className="w-32 h-32 text-black" />
                                    </div>
                                    <p className="text-sm text-gray-400 mb-4">
                                        Or enter this code manually: <span className="font-mono">JBSWY3DPEHPK3PXP</span>
                                    </p>
                                </div>
                                <div>
                                    <Label className="text-gray-300 mb-2 block">
                                        Verification Code
                                    </Label>
                                    <Input
                                        className="bg-gray-700 border-gray-600 text-white"
                                        placeholder="Enter 6-digit code"
                                    />
                                </div>
                                <div className="flex justify-end gap-3">
                                    <Button
                                        variant="outline"
                                        onClick={() => setShowMfaSetup(false)}
                                        className="border-gray-600"
                                    >
                                        Cancel
                                    </Button>
                                    <Button className="bg-emerald-600 hover:bg-emerald-500">
                                        Verify & Enable
                                    </Button>
                                </div>
                            </div>
                        </DialogContent>
                    </Dialog>
                )}
            </div>
        </Layout>
    );
};