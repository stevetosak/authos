import React, {ReactElement, useEffect, useState} from "react";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Info, ShieldQuestion, Check, Key, User, Mail, RefreshCw, AlertTriangle} from "lucide-react";
import {useNavigate} from "react-router-dom";
import {motion} from "framer-motion";
import {envConfig} from "@/lib/env.ts";
import {api, apiGetAuthenticated} from "@/services/netconfig.ts";
import {UserInfoResponse} from "@/services/types.ts";

const scopeIcons: Record<string, ReactElement> = {
    'profile': <User className="w-4 h-4 text-primary"/>,
    'email': <Mail className="w-4 h-4 text-primary"/>,
    'offline_access': <RefreshCw className="w-4 h-4 text-primary"/>,
};

const scopeLabels: Record<string, string> = {
    'profile': 'View your profile information',
    'email': 'Access your email address',
    'offline_access': 'Maintain access to your account',
};

const scopeDescriptions: Record<string, string> = {
    'profile': 'Includes your name, profile picture, and basic info',
    'email': 'View your primary email address',
    'offline_access': "Stay signed in and access your data when you're not present",
};

type ConsentQuery = {
    clientId: string,
    redirectUri: string,
    state: string,
    scope: string,
    authzId: string,
}

const ConsentForm: React.FC = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState<ConsentQuery | null>(null);
    const [clientName, setClientName] = useState<string | null>(null);
    const [userEmail, setUserEmail] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const checkQuery = (): ConsentQuery | null => {
        const params = new URLSearchParams(location.search)
        const clientId = params.get("client_id")
        const redirectUri = params.get("redirect_uri")
        const state = params.get("state")
        const scope = params.get("scope")
        const authzId = params.get("authz_id")

        if (!clientId || !redirectUri || !state || !scope || !authzId) {
            console.error("missing query params")
            //TODO tuka trebit da sa pustit request za da sa iscistit temp sesijata so id authz_id
            navigate("/")
            return null
        }

        return {clientId, redirectUri, state, scope, authzId}
    }

    const handleApprove = () => {
        if (!query) return
        window.location.href = `${envConfig.API_URL}/oauth/approve?client_id=${query.clientId}&redirect_uri=${query.redirectUri}&state=${query.state}&scope=${query.scope}&authz_id=${query.authzId}`
    }

    useEffect(() => {
        const q = checkQuery();
        if (!q) return;
        setQuery(q);

        api.get(`/oauth/client-info?client_id=${q.clientId}`)
            .then(resp => setClientName(resp.data.name))
            .catch(() => setError("This application could not be found. The request may be invalid or expired."));

        apiGetAuthenticated<UserInfoResponse>("/verify")
            .then(resp => setUserEmail(resp.data.user.email))
            .catch(() => navigate(`/oauth/login${location.search}`));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const scopeList = (query?.scope ?? "").split(" ").filter(s => s && s !== "openid");
    const ready = query !== null && clientName !== null && userEmail !== null;

    return (
        <div
            className="flex items-center justify-center min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 p-4 sm:p-6">
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{duration: 0.3}}
                className="w-full max-w-md"
            >
                <Card className="bg-gray-800/70 backdrop-blur-sm border border-gray-700/50 shadow-xl overflow-hidden">
                    <CardHeader className="border-b border-gray-700/50 p-6">
                        <div className="flex flex-col items-center">
                            <div className="bg-primary/10 p-3 rounded-full mb-4">
                                {error
                                    ? <AlertTriangle className="w-8 h-8 text-destructive"/>
                                    : <ShieldQuestion className="w-8 h-8 text-primary"/>}
                            </div>
                            <CardTitle className="text-2xl font-bold text-center">
                                {error ? "Authorization Failed" : "Authorization Request"}
                            </CardTitle>
                        </div>
                    </CardHeader>

                    <CardContent className="p-6">
                        {error ? (
                            <p className="text-center text-gray-300">{error}</p>
                        ) : !ready ? (
                            <div className="flex flex-col items-center py-8 text-gray-400">
                                <RefreshCw className="w-6 h-6 mb-3 animate-spin"/>
                                Loading authorization request...
                            </div>
                        ) : (
                            <>
                                <div className="text-center mb-6">
                                    <p className="text-gray-300 mb-2">
                                        <span className="font-medium text-white">{clientName}</span> wants to access
                                        your account
                                    </p>
                                    <p className="text-sm text-gray-400">
                                        This will allow {clientName} to:
                                    </p>
                                </div>

                                <div className="space-y-3 mb-8">
                                    {scopeList.map((scope) => (
                                        <div key={scope}
                                             className="flex items-start bg-gray-700/50 p-3 rounded-lg border border-gray-600/50">
                                            <div className="bg-primary/10 p-1.5 rounded-full mr-3 mt-0.5">
                                                {scopeIcons[scope] || <Key className="w-4 h-4 text-primary"/>}
                                            </div>
                                            <div>
                                                <p className="text-gray-100 font-medium">{scopeLabels[scope] || scope}</p>
                                                <p className="text-xs text-gray-400 mt-1">{scopeDescriptions[scope] || ''}</p>
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <div className="bg-gray-700/30 p-4 rounded-lg border border-gray-600/50 mb-6">
                                    <div className="flex items-start">
                                        <Info className="w-5 h-5 text-blue-400 flex-shrink-0 mt-0.5 mr-2"/>
                                        <p className="text-sm text-gray-300">
                                            You're signing in as <span
                                            className="font-medium text-white">{userEmail}</span>.
                                            Not you? <a href="/logout" className="text-primary hover:underline">Switch
                                            accounts</a>.
                                        </p>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <Button
                                        variant="outline"
                                        className="border-gray-600 text-gray-300 hover:bg-gray-700/50 hover:text-white h-12"
                                        onClick={() => navigate("/")}
                                    >
                                        Cancel
                                    </Button>
                                    <Button
                                        className="h-12 font-medium"
                                        onClick={handleApprove}
                                    >
                                        <Check className="w-5 h-5 mr-2"/>
                                        Allow Access
                                    </Button>
                                </div>
                            </>
                        )}
                    </CardContent>

                    {!error && ready && (
                        <CardFooter className="border-t border-gray-700/50 p-4 bg-gray-800/50">
                            <p className="text-xs text-gray-500 text-center w-full">
                                By approving, you agree to {clientName}'s{' '}
                                <a href="#" className="text-primary hover:underline">Terms of Service</a> and{' '}
                                <a href="#" className="text-primary hover:underline">Privacy Policy</a>.
                            </p>
                        </CardFooter>
                    )}
                </Card>
            </motion.div>
        </div>
    );
};

export default ConsentForm;
