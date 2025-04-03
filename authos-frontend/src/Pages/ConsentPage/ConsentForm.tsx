import React, {useEffect} from "react";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {ShieldCheck, Lock, Info} from "lucide-react";
import {useNavigate} from "react-router-dom";

const ConsentForm: React.FC = () => {
    const navigate = useNavigate();

    const checkQuery = () => {
        const query = new URLSearchParams(location.search)
        const clientId = query.get("client_id")
        const redirectUri = query.get("redirect_uri")
        const state = query.get("state")
        const scope = query.get("scope")

        if (!clientId || !redirectUri || !state || !scope) {
            console.error("missing query params")
            navigate("/")
        }

        return {
            clientId,
            redirectUri,
            state,
            scope
        }

    }

    const handleApprove = async () => {
        const query = checkQuery();
        window.location.href = `http://localhost:9000/oauth/approve?client_id=${query.clientId}&redirect_uri=${query.redirectUri}&state=${query.state}&scope=${query.scope}`

    }

    useEffect(() => {
      checkQuery();
    },[])

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white p-6">
            <Card className="w-full max-w-lg shadow-lg border border-gray-700 bg-gray-800">
                <CardContent className="p-8 flex flex-col items-center">
                    <h1 className="text-3xl font-bold mb-4 text-green-500">Consent Request</h1>
                    <p className="text-gray-300 text-center mb-6">
                        "AppName" is requesting access to your account. Please review the permissions below and approve
                        if you trust this application.
                    </p>

                    <div className="w-full bg-gray-700 p-4 rounded-lg mb-6">
                        <div className="flex items-center mb-2">
                            <ShieldCheck className="text-green-500 w-6 h-6 mr-2"/>
                            <p className="text-gray-300">Read your profile information</p>
                        </div>
                        <div className="flex items-center mb-2">
                            <Lock className="text-green-500 w-6 h-6 mr-2"/>
                            <p className="text-gray-300">Access your email address</p>
                        </div>
                        <div className="flex items-center">
                            <Info className="text-green-500 w-6 h-6 mr-2"/>
                            <p className="text-gray-300">Manage your application settings</p>
                        </div>
                    </div>

                    <div className="flex gap-4">
                        <Button className="bg-green-600 hover:bg-green-500 px-6 py-3" onClick={handleApprove}>
                            Approve
                        </Button>
                        <Button className="bg-red-600 hover:bg-red-500 px-6 py-3" onClick={() => navigate("/")}>
                            Deny
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default ConsentForm;
