import {Link, useNavigate} from "react-router-dom";
import React, {useState} from "react";
import {LoginForm} from "@/Pages/components/login-form.tsx";
import {api} from "@/services/config.ts";
import {LoginResponse} from "@/services/types.ts";
import {validateResponse} from "@/services/jwtService.ts";

export const OAuthLogin: React.FC = () => {

    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [error, setError] = useState<string | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [loading, setLoading] = useState<boolean>(false);
    const nav = useNavigate()

    //TODO PRI USPESEN LOGIN TREBIT DA SA CUVAT SUB OD USER VO COOKIE, toj cookiee do duster go prakjas
    // csrf

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        const params = new URLSearchParams(window.location.search)
        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);
        formData.append('client_id', params.get("client_id") || '');
        formData.append('redirect_uri', params.get("redirect_uri") || '');
        formData.append('state', params.get("state") || '');
        formData.append('scope', params.get("scope") || '')


        const isValidRequest = Array.from(formData.values()).every(val => val !== null && val !== '');
        if(!isValidRequest) {
            nav("/?error=invalid_oauth_parameters")
            return
        }
        formData.append("duster_uid",params.get("duster_uid") || '')

        try{
            const resp = await api.post<LoginResponse>("/oauth-login", formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                withCredentials: true
            });
            if(resp.data.signature == null || resp.data.redirectUri == null) {
                nav("/error")
                return;
            }
            const valid = await validateResponse(resp.data.signature)
            console.warn("VALID:",valid)
            window.location.href = valid ? resp.data.redirectUri : "http://localhost:5173/error"
        } catch (err){
            console.error("ERROR oauth login: " + err)
            nav("/error")
        }


    };



    return (
        <div className="flex min-h-screen w-full items-center justify-center">
            <div className="w-full max-w-3xl">
                <LoginForm className={""} handleSubmit={handleSubmit} setEmail={setEmail} setPassword={setPassword}/>
            </div>
        </div>
    );
}

