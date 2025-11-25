import {TotpModalProps} from "@/services/types.ts";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/netconfig.ts";
import {TotpModal} from "@/Pages/components/totp/TotpModal.tsx";
import {useEffect, useState} from "react";
import { Dialog, DialogContent, DialogTitle } from "@radix-ui/react-dialog";
import { DialogHeader } from "@/components/ui/dialog";
import { Shield } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export const TotpQrCodeDisplay = ({dialogOpen, onOpenChange,user}: Omit<TotpModalProps, "onSubmit">) => {

    const sendEnable2FARequest = (otp: string) => {
        return apiPostAuthenticated<void>(`/enable-totp?otp=${otp}`)
    }


    const [totpInput, setTotpInput] = useState<string>("")



    const handleSubmit = async () => {
        if (totpInput.trim() === "" || totpInput.length !== 6 || isNaN(Number(totpInput.trim()))) return
        const resp = await onSubmit(totpInput)
        if (resp.status !== 200) console.log("error otp", resp.statusText)

        console.log("success")
        onSuccess()
        //TODO
    }



    return (
        <Dialog open={dialogOpen} onOpenChange={onOpenChange}>
            <DialogContent className="bg-gray-800 border border-gray-700 max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <Shield className="w-5 h-5 text-emerald-400"/>
                        Set Up Multi-Factor Authentication
                    </DialogTitle>
                </DialogHeader>
                <div className="space-y-6">
                    <div className="text-center">
                        <p className="text-gray-300 mb-4">
                            Scan this QR code with your authenticator app
                        </p>

                        <div className="flex justify-center">
                            <img
                                src={totpQr}
                                alt="qr"
                                className="mx-auto rounded-lg shadow-md"
                            />
                        </div>

                        <p className="text-sm text-gray-400 mb-4">
                            Or enter this code manually: <span className="font-mono">Todo</span>
                        </p>
                    </div>

                    <div>
                        <Label className="text-gray-300 mb-2 block">
                            Verification Code
                        </Label>
                        <Input
                            className="bg-gray-700 border-gray-600 text-white"
                            placeholder="Enter 6-digit code"
                            value={totpInput}
                            onChange={(e) => setTotpInput(() => e.target.value)}
                        />
                    </div>
                    <div className="flex justify-end gap-3">
                        <Button
                            variant="outline"
                            onClick={() => () => onOpenChange(false)}
                            className="border-gray-600"
                        >
                            Cancel
                        </Button>
                        <Button className="bg-emerald-600 hover:bg-emerald-500" onClick={handleSubmit}>
                            Verify & Enable
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    )
}
