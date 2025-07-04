import React, {useState} from "react";
import {LoginForm} from "@/Pages/components/login-form.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {api} from "@/services/config.ts";
import {LoginResponse} from "@/services/types.ts";
import {useNavigate} from "react-router-dom";


const NativeLogin: React.FC = () => {

    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [error, setError] = useState<string | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [loading, setLoading] = useState<boolean>(false);
    const {setContext, setIsAuthenticated} = useAuth()
    const nav = useNavigate()

    const handleSubmit = async (e: React.FormEvent<Element>) => {
        e.preventDefault();
        setError(null);

        if (!email || !password) {
            setError("Email and password are required.");
            return;
        }


        setLoading(true);

        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);


        await api.post<LoginResponse>("native-login", formData, {
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            withCredentials: true
        }).then(resp => {
            setContext(resp.data)
            setIsAuthenticated(true)
            setLoading(false);
            nav("/dashboard")
        }).catch(err => {
            console.error(err)
        })

        setLoading(false);


    };


    return (
        <div className="flex min-h-screen w-full items-center justify-center">
            <div className="w-full max-w-3xl">
                <LoginForm className={""} handleSubmit={handleSubmit} setEmail={setEmail} setPassword={setPassword}/>
            </div>
        </div>
    );
};

export default NativeLogin;
