import {cn} from "@/lib/utils.ts";
import {Button} from "@/components/ui/button.tsx";
import {Card, CardContent, CardDescription, CardHeader, CardTitle,} from "@/components/ui/card.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Label} from "@/components/ui/label.tsx";
import React, {useState} from "react";
import axios from "axios";
import {LoginResponse} from "@/services/interfaces.ts";
import {useAuth} from "@/services/useAuth.ts";
import {Link, useNavigate} from "react-router-dom";
import {Chrome, LockIcon, LogInIcon, MailIcon} from "lucide-react";
import {validateResponse} from "@/services/jwtService.ts";
import {api} from "@/services/config.ts";

export function LoginForm({className, ...props}: React.ComponentProps<"div">) {
    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [error, setError] = useState<string | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [loading, setLoading] = useState<boolean>(false);
    const {setContext, setIsAuthenticated} = useAuth()

    const nav = useNavigate()


    // todo security checks on frontend redirecs


    const handleNativeLogin = async (formData: URLSearchParams) => {


        return await api.post<LoginResponse>("native-login", formData, {
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            withCredentials: true
        });


    }

    const handleOauthLogin = async (formData: URLSearchParams) => {
        return await api.post<LoginResponse>("/oauth-login", formData, {
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            withCredentials: true
        });
    }

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);

        if (!email || !password) {
            setError("Email and password are required.");
            return;
        }

        const params = new URLSearchParams(window.location.search)
        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);
        formData.append('client_id', params.get("client_id") || '');
        formData.append('redirect_uri', params.get("redirect_uri") || '');
        formData.append('state', params.get("state") || '');
        formData.append('scope', params.get("scope") || '')


        const oauthRequest = Array.from(formData.values()).every(val => val !== null && val !== '');

        setLoading(true);

        if (oauthRequest) {
            const resp = await handleOauthLogin(formData)
            if(resp.data.signature == null || resp.data.redirectUri == null) throw Error("No signature")
            const valid = await validateResponse(resp.data.signature)
            console.warn("VALID:",valid)
            window.location.href = valid ? resp.data.redirectUri : ""

        } else {
            handleNativeLogin(formData)
                .then(resp => {
                    setContext(resp.data)
                    setIsAuthenticated(true)
                    setLoading(false);
                    nav("/dashboard")
                }).catch(err => {
                console.error(err)
            })
        }
        setLoading(false);


    };

    return (
        <div className={cn("flex items-center justify-center min-h-[calc(100vh-5rem)] w-full", className)} {...props}>
            <Card
                className="bg-gray-800 border border-gray-700 shadow-xl rounded-xl w-full max-w-4xl min-h-[500px] flex flex-col">
                <CardHeader className="border-b border-gray-700 p-8">
                    <div className="flex flex-col items-center space-y-2">
                        <LockIcon className="w-10 h-10 text-emerald-500"/>
                        <CardTitle className="text-white text-2xl font-bold">Welcome Back</CardTitle>
                        <CardDescription className="text-gray-400">
                            Sign in to access your account
                        </CardDescription>
                    </div>
                </CardHeader>

                <CardContent className="p-8 flex-1 flex flex-col">
                    <form onSubmit={handleSubmit} className="flex-1 flex flex-col">
                        <div className="space-y-6 flex-1">
                            <div className="space-y-3">
                                <Label htmlFor="email" className="text-gray-300 flex items-center gap-2">
                                    <MailIcon className="w-4 h-4"/>
                                    Email Address
                                </Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="your@email.com"
                                    required
                                    className="bg-gray-700 border-gray-600 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 h-12"
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                            </div>

                            <div className="space-y-3">
                                <div className="flex items-center justify-between">
                                    <Label htmlFor="password" className="text-gray-300 flex items-center gap-2">
                                        <LockIcon className="w-4 h-4"/>
                                        Password
                                    </Label>
                                    <a
                                        href="#"
                                        className="text-sm text-emerald-500 hover:text-emerald-400 hover:underline"
                                    >
                                        Forgot password?
                                    </a>
                                </div>
                                <Input
                                    id="password"
                                    type="password"
                                    required
                                    className="bg-gray-700 border-gray-600 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 h-12"
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                            </div>

                            <div className="pt-4">
                                <Button
                                    type="submit"
                                    className="w-full bg-emerald-600 hover:bg-emerald-500 text-white h-12 text-md font-medium"
                                >
                                    <LogInIcon className="w-5 h-5 mr-2"/>
                                    Sign In
                                </Button>
                            </div>

                            <div className="relative my-6">
                                <div className="absolute inset-0 flex items-center">
                                    <div className="w-full border-t border-gray-700"></div>
                                </div>
                                <div className="relative flex justify-center text-sm">
                                    <span className="px-2 bg-gray-800 text-gray-400">Or continue with</span>
                                </div>
                            </div>

                            <Button
                                variant="outline"
                                className="w-full border-gray-600 text-white hover:bg-gray-700 h-12"
                                type="button"
                            >
                                <Chrome className="w-5 h-5 mr-2"/>
                                Sign in with Google
                            </Button>
                        </div>

                        <div className="mt-8 text-center text-sm text-gray-400">
                            Don't have an account?{' '}
                            <Link to={"/register-user"}
                                  className="text-emerald-500 hover:text-emerald-400 hover:underline">
                                Create one now
                            </Link>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}