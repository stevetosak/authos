import { Mail, RefreshCw, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import React from "react";

export const AccountConfirmationPage: React.FC = () => {
    const handleResendEmail = () => {
        console.log("Resending confirmation email...");
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-gray-800/50 border border-gray-700/50 rounded-xl p-8 backdrop-blur-sm shadow-lg">
                <div className="flex flex-col items-center text-center">
                    <div className="bg-emerald-500/10 p-4 rounded-full mb-6">
                        <Mail className="w-10 h-10 text-emerald-400" strokeWidth={1.5} />
                    </div>


                    <h1 className="text-2xl font-bold text-white mb-2">
                        Confirm Your Email
                    </h1>


                    <p className="w-full text-gray-400 mb-6">
                         A confirmation link has been sent to <span className="font-medium text-white">your@email.com</span>.
                        Please check your inbox and click the link to activate your account.
                    </p>


                    <div className="flex flex-col sm:flex-row gap-3 w-full">
                        <Button
                            onClick={handleResendEmail}
                            variant="outline"
                            className="border-gray-600 text-gray-300 hover:bg-gray-700/50 w-full"
                        >
                            <RefreshCw className="w-4 h-4 mr-2" />
                            Resend Email
                        </Button>

                    </div>


                    <p className="text-xs text-gray-500 mt-6">
                        Didn't receive the email? Check your spam folder or
                        <button
                            onClick={handleResendEmail}
                            className="text-emerald-400 hover:underline ml-1"
                        >
                            resend it
                        </button>.
                    </p>
                </div>
            </div>
        </div>
    );
};