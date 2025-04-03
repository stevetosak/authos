import React, {useEffect} from "react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import {errorCodes} from "@/Pages/ErrorPage/errorCodes.tsx";

const ErrorPage: React.FC = () => {
    const navigate = useNavigate();

    const showErrorMsg = (): string => {

        const code = getErrorCode();
        return code === "unknown" ? "An unknown error occurred" : errorCodes[code]
    }

    const getErrorCode = () : string => {
        const query = new URLSearchParams(location.search);
        const err_code = query.get("err_code")
        return err_code ? err_code : "unknown"
    }

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white">
            {/* Header */}
            <header className="w-full flex justify-between items-center p-6 bg-gray-800 text-white shadow-md">
                <h1 className="text-2xl font-bold">Authos</h1>
            </header>

            {/* Error Section */}
            <section className="flex flex-col items-center justify-center my-12 text-center">
                <h1 className="text-6xl font-extrabold text-red-600">Error</h1>
                <p className="mt-2 text-4xl font-semibold text-gray-400">
                    Gabagool!
                </p>

                <div className="mt-4 p-6 bg-gray-800 rounded-lg shadow-lg w-full max-w-md">
                    <div className="flex items-center space-x-2">
                        <div className="text-xl font-semibold text-red-500">Status: {getErrorCode()}</div>
                        <div className="text-xl font-bold text-red-600">{}</div>
                    </div>
                    <p className="mt-2 text-gray-300">
                        {showErrorMsg()}
                    </p>
                </div>

                <Button
                    className="mt-8 bg-green-600 hover:bg-green-500 text-white px-6 py-3 text-lg"
                    onClick={() => navigate("/")}
                >
                    Go Back Home
                </Button>
            </section>

            {/* Footer */}
            <footer className="w-full p-4 text-center text-gray-400 mt-12">
                &copy; {new Date().getFullYear()} Authos. All rights reserved.
            </footer>
        </div>
    );
};

export default ErrorPage;
