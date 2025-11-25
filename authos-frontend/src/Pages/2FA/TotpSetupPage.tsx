import {Dialog, DialogContent, DialogTitle} from "@radix-ui/react-dialog";
import {DialogHeader} from "@/components/ui/dialog.tsx";
import {Shield} from "lucide-react";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useEffect, useState} from "react";
import {apiGetAuthenticated} from "@/services/netconfig.ts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {useNavigate} from "react-router-dom";

export const TotpSetupPage = () => {
    const [totpQr, setTotpQr] = useState<string>()
    const [secret,setSecret] = useState<string>()
    const nav = useNavigate()

    useEffect(() => {
        let active = true

        const fetchQr = async () => {
            try {
                const resp = await apiGetAuthenticated<{qrData:string,secret:string}>("/generate-totp-qr")
                if (active){
                    setTotpQr(resp.data.qrData)
                    setSecret(resp.data.secret)
                }
            } catch (err) {
                console.error("Failed to load TOTP QR:", err)
                nav("/profile")
            }
        }

        fetchQr()
        return () => {
            active = false
        }
    }, [])


    const displayRawCode = () => {
        if(!secret) return ""
        const parts = []
        let prev = 0
        for(let i = 4; i <= secret.length; i+=4){
            parts.push(secret.substring(prev,i))
            prev = i;
        }
        return parts.join(" ")
    }



    return (
        <div className={'flex items-center justify-center min-h-screen px-4'}>
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Shield className="w-5 h-5 text-emerald-400"/>
                        Set Up Multi-Factor Authentication
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="space-y-6">
                        <div className="text-center">
                            <p className="text-gray-300 mb-4 font-semibold">
                                Scan this QR code with your authenticator app
                            </p>

                            <div className="flex justify-center">
                                <img
                                    src={totpQr}
                                    alt="qr"
                                    className="mx-auto rounded-lg shadow-md"
                                />
                            </div>

                            <p className="text-sm text-start text-gray-400 mb-4 p-2 mt-2">
                                Or enter this code manually: <span className="font-mono">{displayRawCode()}</span>
                            </p>
                        </div>
                        <div className={'flex justify-end'}>
                            <Button className={'btn-primary'} onClick={() => nav("/2fa/totp/enable")}>Continue</Button>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>

    )
}