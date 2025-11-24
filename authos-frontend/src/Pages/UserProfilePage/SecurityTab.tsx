import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {AlertTriangle, Copy, Download, Lock, RotateCw, Shield, Smartphone} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {ProfileTabProps, User} from "@/services/types.ts";
import {useEffect, useState} from "react";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input.tsx";
import {apiPostAuthenticated} from "@/services/netconfig";
import {ConfirmDisable2FAModal} from "@/Pages/components/totp/ConfirmDisable2FAModal.tsx";
import {useNavigate} from "react-router-dom";

type SecurityTabProps = { user: User }

export const SecurityTab = ({active, user}: ProfileTabProps & SecurityTabProps) => {

    const nav = useNavigate()
    const [recoveryCodes, setRecoveryCodes] = useState(["1", "2"]);
    const [isMfaEnabled, setIsMfaEnabled] = useState(user.mfaEnabled);
    const [showConfirmationModal, setShowConfirmationModal] = useState(false)


    const generateRecoveryCodes = () => {

    }

    const enableMfa = async () => {
        const resp = await apiPostAuthenticated("/enable-totp")
        if(resp.status === 200){
            nav("/2fa/totp/setup")
        }
    }

    const onConfirmDisable2FA = () => {
        setShowConfirmationModal(false)
        nav("/2fa/totp/disable")
    }

    return (
        <>
            {active && (
                <>
                   <ConfirmDisable2FAModal showConfirmationModal={showConfirmationModal} onOpenChange={(open) => setShowConfirmationModal(open)} onSubmit={onConfirmDisable2FA}/>

                    <div className="space-y-6">
                        <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                            <CardHeader className="border-b border-gray-700/50">
                                <CardTitle className="flex items-center gap-2">
                                    <Lock className="w-5 h-5 text-emerald-400"/>
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

                        <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                            <CardHeader className="border-b border-gray-700/50">
                                <div className="flex justify-between items-center">
                                    <CardTitle className="flex items-center gap-2">
                                        <Shield className="w-5 h-5 text-emerald-400"/>
                                        Multi-Factor Authentication
                                    </CardTitle>
                                </div>
                            </CardHeader>
                            {/*mfa popup*/}
                            <CardContent className="p-6 space-y-6">
                                {isMfaEnabled ? (
                                    <div className="space-y-4">
                                        <div className="flex items-center gap-3 p-4 bg-gray-700/30 rounded-lg border border-gray-600/50">
                                            <Smartphone className="w-10 h-10 text-emerald-400"/>
                                            <div className={"w-full"}>
                                                <p className="font-medium">Time-based One Time Password </p>
                                                <p className="text-sm text-gray-400">Authenticator App</p>
                                            </div>
                                            <div className={"flex w-full justify-end"}>
                                                <Button className={'bg-red-500 hover:bg-red-700'}
                                                        onClick={() => setShowConfirmationModal(true)}>Disable 2FA</Button>
                                            </div>
                                        </div>

                                        {recoveryCodes.length > 0 ? (
                                            <div className="space-y-4">
                                                <div
                                                    className="bg-gray-900/50 p-4 rounded-lg border border-red-400/30">
                                                    <h4 className="font-medium text-red-400 flex items-center gap-2">
                                                        <AlertTriangle className="w-4 h-4"/>
                                                        Save Your Recovery Codes
                                                    </h4>
                                                    <p className="text-sm text-gray-400 mt-2">
                                                        These codes can be used to access your account if you
                                                        lose access to your authenticator app.
                                                    </p>
                                                </div>

                                                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
                                                    {recoveryCodes.map((code, i) => (
                                                        <div key={i}
                                                             className="font-mono text-sm bg-gray-800 p-2 rounded text-center">
                                                            {code}
                                                        </div>
                                                    ))}
                                                </div>

                                                <div className="flex gap-3">
                                                    <Button variant="outline" className="border-gray-600">
                                                        <Download className="w-4 h-4 mr-2"/>
                                                        Download
                                                    </Button>
                                                    <Button variant="outline" className="border-gray-600">
                                                        <Copy className="w-4 h-4 mr-2"/>
                                                        Copy
                                                    </Button>
                                                    <Button variant="outline" className="border-gray-600">
                                                        <RotateCw className="w-4 h-4 mr-2"/>
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
                                            Add an extra layer of security to your account by enabling MFA. When
                                            enabled, you'll be required to enter both your password and an
                                            authentication code from your mobile device when logging in.
                                        </p>
                                        <Button
                                            onClick={enableMfa}
                                            className="bg-emerald-600 hover:bg-emerald-500"
                                        >
                                            Enable MFA
                                        </Button>
                                    </div>
                                )}
                            </CardContent>
                        </Card>

                    </div>

                </>

            )}
        </>
    )
}