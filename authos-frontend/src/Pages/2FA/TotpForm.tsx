import {InputOTP, InputOTPGroup, InputOTPSlot} from "@/components/ui/input-otp.tsx";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useSidebar} from "@/components/ui/sidebar.tsx";
import {useEffect, useState} from "react";
import {motion} from "framer-motion";
import {REGEXP_ONLY_DIGITS} from "input-otp";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/netconfig.ts";
import {useAuth} from "@/services/useAuth.ts";
import {useNavigate} from "react-router-dom";

export const TotpForm = ({action}: {action: "/verify-totp-setup" | "/verify-totp" | "/disable-totp"}) => {
    const { toggleSidebar } = useSidebar();
    const {refreshAuth} = useAuth()
    const [otpValue,setOtpValue] = useState<string>("")
    const nav = useNavigate()

    const handleSubmit = async () => {
        if (otpValue.trim() === "" || otpValue.length !== 6 || isNaN(Number(otpValue.trim()))) return
        const resp = await apiPostAuthenticated(`${action}?otp=${otpValue}`)
        if (resp.status !== 200) console.log("error verifying otp", resp.statusText)

        console.log("success")

        refreshAuth()
        nav("/profile")


        //TODO
    }


    useEffect(() => {
        toggleSidebar();
    }, []);

    return (
        <div className="flex items-center justify-center min-h-screen px-4">
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
            >

                <Card className="w-full max-w-md backdrop-blur-xl shadow-2xl rounded-2xl">
                    <CardHeader className="text-center space-y-1 pb-2">
                        <CardTitle className="text-2xl font-semibold text-white">
                            Verify Your Code
                        </CardTitle>
                        <CardDescription className="text-gray-300">
                            Enter the 6-digit code from your authenticator app.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="flex flex-col items-center">
                        <InputOTP maxLength={6}
                                  pattern={REGEXP_ONLY_DIGITS} value={otpValue}
                                  onChange={(value) => setOtpValue(value)}
                                  className="p-4">
                            <InputOTPGroup className="gap-3">
                                {[0,1,2,3,4,5].map(i => (
                                    <InputOTPSlot
                                        key={i}
                                        index={i}
                                        className="w-14 h-14 text-2xl text-white border-2 border-white/30 rounded-xl bg-white/10 focus:bg-white/20 focus:border-blue-400 transition-all"
                                    />
                                ))}
                            </InputOTPGroup>
                        </InputOTP>

                        <Button
                            variant="default"
                            className="mt-6 w-full py-6 text-base btn-primary transition-all shadow-lg rounded-xl"
                            onClick={handleSubmit}
                        >
                            Verify
                        </Button>
                    </CardContent>
                </Card>
            </motion.div>
        </div>
    );
};
