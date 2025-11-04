import React, {useState} from "react";
import {LoginForm} from "@/Pages/components/login-form.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {api} from "@/services/netconfig.ts";
import {LoginResponse, UserInfoResponse} from "@/services/types.ts";
import {useNavigate} from "react-router-dom";
import {validateResponse} from "@/services/jwtService.ts";


const NativeLogin: React.FC = () => {

    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [error, setError] = useState<string | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const {refreshAuth} = useAuth();
    const nav = useNavigate()





    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!email || !password) {
            setError("Email and password are required.");
            return;
        }


        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);


        try {
            const resp = await api.post<LoginResponse>("native-login", formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                withCredentials: true
            })
            console.log(resp.data)
            if (resp.data.status === "SUCCESS") {
                await refreshAuth();
                nav("/profile")
            }
            if (resp.data.status === "MFA_REQUIRED") {
                console.log("vleze mfa f")
                nav("/2fa/totp/verify")
            }
            // nav("/dashboard")
        } catch (err) {
            console.error("Auth error occurred")
            console.log(err)
        }



    };


    return (
        <div className="flex min-h-screen w-full items-center justify-center">
            <div className="w-full max-w-3xl">
                <LoginForm className={""} handleSubmit={handleSubmit} setEmail={setEmail}
                           setPassword={setPassword}/>
            </div>
        </div>
    );
};

export default NativeLogin;
