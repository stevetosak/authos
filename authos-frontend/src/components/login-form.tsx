import {cn} from "@/lib/utils";
import {Button} from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import React, {useState} from "react";
import {useLocation} from "react-router";

export function LoginForm({className, ...props}: React.ComponentProps<"div">) {
    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [error, setError] = useState<string | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [loading, setLoading] = useState<boolean>(false);

    const nav = useLocation();


    const handleLogin = async (email: string, password: string) => {

        const params = new URLSearchParams(window.location.search)

        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);
        formData.append('client_id', params.get("client_id") || '');
        formData.append('redirect_uri', params.get("redirect_uri") || '');
        formData.append('state', params.get("state") || '');
        formData.append('scope', params.get("scope") || '')


        return await fetch("http://localhost:9000/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: formData.toString(),
        });


    }

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);

        if (!email || !password) {
            setError("Email and password are required.");
            return;
        }

        setLoading(true);
        handleLogin(email, password)
            .then(resp => {
                window.location.href = resp.headers.get("location") || ""
                setLoading(false);
            }).catch(err => {
            console.error(err)
        })

    };

    return (
        <div
            className={cn("flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white", className)} {...props}>
            <Card className="bg-gray-800 shadow-md w-full max-w-md">
                <CardHeader>
                    <CardTitle className="text-white text-center text-2xl">Login to Your Account</CardTitle>
                    <CardDescription className="text-gray-400 text-center">
                        Enter your email below to log in to your account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit}>
                        <div className="flex flex-col gap-6">
                            <div className="grid gap-3">
                                <Label htmlFor="email" className="text-gray-300">Email</Label>
                                <Input id="email" type="email" placeholder="m@example.com" required
                                       className="bg-gray-700 border-gray-600 text-white"
                                       onChange={(e) => setEmail(e.target.value)}/>
                            </div>
                            <div className="grid gap-3">
                                <div className="flex items-center">
                                    <Label htmlFor="password" className="text-gray-300">Password</Label>
                                    <a href="#" className="ml-auto text-sm text-green-500 hover:underline">
                                        Forgot your password?
                                    </a>
                                </div>
                                <Input id="password" type="password" required
                                       className="bg-gray-700 border-gray-600 text-white"
                                       onChange={(e) => setPassword(e.target.value)}/>
                            </div>
                            <div className="flex flex-col gap-3">
                                <Button type="submit" className="w-full bg-green-600 hover:bg-green-500 text-white">
                                    Login
                                </Button>
                                <Button variant="outline"
                                        className="w-full border-gray-600 text-white hover:bg-gray-700">
                                    Login with Google
                                </Button>
                            </div>
                        </div>
                        <div className="mt-4 text-center text-sm text-gray-400">
                            Don&apos;t have an account? <a href="#" className="text-green-500 hover:underline">Sign
                            up</a>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}