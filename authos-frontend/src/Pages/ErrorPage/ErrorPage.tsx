import React from "react";
import {Button} from "@/components/ui/button";
import {useNavigate} from "react-router-dom";
import {errorCodes} from "@/Pages/ErrorPage/errorCodes.tsx";
import {AlertTriangle, Home, RefreshCw, ShieldAlert} from "lucide-react";

const ErrorPage: React.FC = () => {
    const navigate = useNavigate();
    const query = new URLSearchParams(location.search);


    const getErrorCode = (): string => {
        const err_code = query.get("error");
        return err_code ? err_code : "unknown";
    };
    const getErrorDescription = (): string => {
        return query.get("error_description") ?? ""
    }



    const errorCode = getErrorCode();
    const errorDescription = getErrorDescription()

    return (
        <div className="min-h-screen w-full text-white p-4 md:p-8 flex flex-col items-center justify-center">
            <div className=" w-full text-center">
                <div className="relative mb-8">
                    <div className="absolute -inset-4 bg-red-500/10 rounded-full blur-lg opacity-75"></div>
                    <div className="relative bg-gray-800/70 border border-red-500/30 rounded-full p-6 inline-flex items-center justify-center">
                        <ShieldAlert className="h-16 w-16 text-red-400" strokeWidth={1.5} />
                    </div>
                </div>

                <h1 className="text-5xl md:text-6xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-red-400 to-red-600 mb-4">
                    {errorCode === "404" ? "Page Not Found" : "Error Occurred"}
                </h1>

                <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6 mb-8 shadow-lg">
                    <div className="flex items-center justify-center gap-3 mb-4">
                        <AlertTriangle className="h-6 w-6 text-red-400" />
                        <span className="text-xl font-semibold text-red-400">Error Code: {errorCode}</span>
                    </div>
                    <p className="text-gray-200 text-2xl">
                        {errorDescription}
                    </p>
                </div>

                <div className="flex flex-col sm:flex-row justify-center gap-4">
                    <Button
                        variant={"default"}
                        onClick={() => navigate("/")}
                        className="text-lg font-medium transition-all"
                    >
                        <Home className="w-5 h-5 mr-2" />
                        Return Home
                    </Button>
                </div>
            </div>

            <footer className="w-full p-4 text-center text-gray-500 mt-16 border-t border-gray-800/50">
                <p className="text-sm">
                    &copy; {new Date().getFullYear()} Authos. All rights reserved.
                    <span className="block text-xs mt-1">Error Reference: {errorCode}</span>
                </p>
            </footer>
        </div>
    );
};

export default ErrorPage;