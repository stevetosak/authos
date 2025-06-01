import React, {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardFooter, CardHeader} from "@/components/ui/card";
import axios from "axios";
import {Link, useNavigate, useSearchParams} from "react-router-dom";
import {Tabs, TabsContent} from "@/components/ui/tabs.tsx";
import {TabsList, TabsTrigger} from "@radix-ui/react-tabs";
import {InfoIcon, KeyIcon, LockIcon, MailIcon, UserIcon, UserPlusIcon, UsersIcon} from "lucide-react";
import Layout from "@/Pages/components/Layout.tsx";

export default function UserRegistration() {
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        name: "",
        surname: ""
    });

    const nav = useNavigate()
    const [searchParams, setSearchParams] = useSearchParams()
    const [oauthFormData, setOauthFormData] = useState({
        email: "",
        password: ""
    })

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };
    const handleOAuthChange = (e) => {
        setOauthFormData({...oauthFormData, [e.target.name]: e.target.value})
    }
    const handleOAuthSubmit = () => {

    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.password != formData.confirmPassword) {
            return;
        }

        const {confirmPassword, ...payload} = formData

        try {
            await axios.post("http://localhost:9000/register", payload)
            alert("Registered Successfully!.")
            nav("/login")
        } catch (e) {
            alert("Cant register user. Error Occured")
            console.error(e)
        }
        // Handle form submission logic here
    };

    return (
        <Layout>

            <div
                className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-gray-100 p-4 md:p-10 font-sans">
                <Card
                    className="bg-gray-800 border border-gray-700 shadow-xl rounded-xl overflow-hidden max-w-2xl mx-auto">
                    <CardHeader className="border-b border-gray-700 p-6">
                        <div className="flex flex-col items-center space-y-2">
                            <h2 className="text-2xl font-bold text-emerald-500">User Registration</h2>
                            <p className="text-gray-400 text-sm">Create an account to access our services</p>
                        </div>
                    </CardHeader>

                    <CardContent className="p-6">
                        <Tabs defaultValue={searchParams.get('type') || 'native'} className="w-full">
                            <div className="border-b border-gray-700">
                                <TabsList className="grid grid-cols-2 bg-gray-800 rounded-none">
                                    <TabsTrigger
                                        value="native"
                                        className="py-4 data-[state=active]:bg-gray-700 data-[state=active]:text-white data-[state=active]:shadow-[0_-2px_0_0_theme(colors.emerald.500)_inset]"
                                        onClick={() => setSearchParams({type: 'native'})}
                                    >
                                        <UserIcon className="w-4 h-4 mr-2"/>
                                        Developer Account
                                    </TabsTrigger>
                                    <TabsTrigger
                                        value="oauth"
                                        className="py-4 data-[state=active]:bg-gray-700 data-[state=active]:text-white data-[state=active]:shadow-[0_-2px_0_0_theme(colors.emerald.500)_inset]"
                                        onClick={() => setSearchParams({type: 'oauth'})}
                                    >
                                        <KeyIcon className="w-4 h-4 mr-2"/>
                                        OAuth Registration
                                    </TabsTrigger>
                                </TabsList>
                            </div>

                            <TabsContent value="native" className="pt-6">
                                <form onSubmit={handleSubmit} className="space-y-5">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                        <div>
                                            <Label className="text-gray-300 mb-2 block">
                                    <span className="flex items-center gap-2">
                                        <UserIcon className="w-4 h-4"/>
                                        First Name
                                    </span>
                                            </Label>
                                            <Input
                                                name="name"
                                                value={formData.name}
                                                onChange={handleChange}
                                                type="text"
                                                className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                required
                                                placeholder="John"
                                            />
                                        </div>
                                        <div>
                                            <Label className="text-gray-300 mb-2 block">
                                    <span className="flex items-center gap-2">
                                        <UsersIcon className="w-4 h-4"/>
                                        Last Name
                                    </span>
                                            </Label>
                                            <Input
                                                name="surname"
                                                value={formData.surname}
                                                onChange={handleChange}
                                                type="text"
                                                className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                required
                                                placeholder="Doe"
                                            />
                                        </div>
                                    </div>

                                    <div>
                                        <Label className="text-gray-300 mb-2 block">
                                <span className="flex items-center gap-2">
                                    <MailIcon className="w-4 h-4"/>
                                    Email
                                </span>
                                        </Label>
                                        <Input
                                            name="email"
                                            value={formData.email}
                                            onChange={handleChange}
                                            type="email"
                                            className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                            required
                                            placeholder="your@email.com"
                                        />
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                        <div>
                                            <Label className="text-gray-300 mb-2 block">
                                    <span className="flex items-center gap-2">
                                        <LockIcon className="w-4 h-4"/>
                                        Password
                                    </span>
                                            </Label>
                                            <Input
                                                name="password"
                                                value={formData.password}
                                                onChange={handleChange}
                                                type="password"
                                                className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                required
                                                placeholder="••••••••"
                                            />
                                            <p className="mt-1 text-xs text-gray-400">Minimum 8 characters</p>
                                        </div>
                                        <div>
                                            <Label className="text-gray-300 mb-2 block">
                                    <span className="flex items-center gap-2">
                                        <LockIcon className="w-4 h-4"/>
                                        Confirm Password
                                    </span>
                                            </Label>
                                            <Input
                                                name="confirmPassword"
                                                value={formData.confirmPassword}
                                                onChange={handleChange}
                                                type="password"
                                                className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                required
                                                placeholder="••••••••"
                                            />
                                        </div>
                                    </div>

                                    <div className="pt-2">
                                        <Button
                                            type="submit"
                                            className="w-full bg-emerald-600 hover:bg-emerald-500 text-white py-3 rounded-lg font-medium"
                                        >
                                            <UserPlusIcon className="w-5 h-5 mr-2"/>
                                            Register Developer Account
                                        </Button>
                                    </div>
                                </form>
                            </TabsContent>

                            {/* OAuth Registration Tab */}
                            <TabsContent value="oauth" className="pt-6">
                                <form onSubmit={handleOAuthSubmit} className="space-y-5">
                                    {/* OAuth Provider Buttons */}
                                    <div className="space-y-3">
                                        <p className="text-sm text-gray-400 text-center">Sign up with</p>
                                        <div className="grid grid-cols-2 gap-3">
                                            <Button
                                                type="button"
                                                variant="outline"
                                                className="border-gray-600 hover:bg-gray-700/50 h-11"
                                                onClick={() => handleOAuthProvider('google')}
                                            >
                                                <img
                                                    src="https://authjs.dev/img/providers/google.svg"
                                                    alt="Google"
                                                    className="w-5 h-5 mr-2"
                                                />
                                                Google
                                            </Button>
                                            <Button
                                                type="button"
                                                variant="outline"
                                                className="border-gray-600 hover:bg-gray-700/50 h-11"
                                                onClick={() => handleOAuthProvider('github')}
                                            >
                                                <img
                                                    src="https://authjs.dev/img/providers/github.svg"
                                                    alt="GitHub"
                                                    className="w-5 h-5 mr-2 dark:invert"
                                                />
                                                GitHub
                                            </Button>
                                        </div>
                                        <div className="relative my-4">
                                            <div className="absolute inset-0 flex items-center">
                                                <div className="w-full border-t border-gray-700"></div>
                                            </div>
                                            <div className="relative flex justify-center">
                                                <span className="px-2 bg-gray-800 text-sm text-gray-400">Or continue manually</span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Auto-filled when provider selected */}
                                    <div>
                                        <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                            <MailIcon className="w-4 h-4"/>
                                            Email
                                        </Label>
                                        <Input
                                            name="email"
                                            value={oauthFormData.email}
                                            onChange={handleOAuthChange}
                                            type="email"
                                            className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                            required
                                            placeholder="your@email.com"
                                            readOnly={!!oauthFormData.provider} // Lock when provider selected
                                        />
                                    </div>

                                    {/* Password field - always required for your native auth */}
                                    <div>
                                        <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                            <LockIcon className="w-4 h-4"/>
                                            Create Password
                                        </Label>
                                        <Input
                                            name="password"
                                            value={oauthFormData.password}
                                            onChange={handleOAuthChange}
                                            type="password"
                                            className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                            required
                                            placeholder="••••••••"
                                        />
                                        <p className="mt-1 text-xs text-gray-400">Minimum 8 characters</p>
                                    </div>

                                    {/* Name fields - shown only when no provider selected */}
                                    {!oauthFormData.provider && (
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                                    <UserIcon className="w-4 h-4"/>
                                                    First Name
                                                </Label>
                                                <Input
                                                    name="firstName"
                                                    value={oauthFormData.firstName}
                                                    onChange={handleOAuthChange}
                                                    type="text"
                                                    className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                    required
                                                    placeholder="John"
                                                />
                                            </div>
                                            <div>
                                                <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                                    <UsersIcon className="w-4 h-4"/>
                                                    Last Name
                                                </Label>
                                                <Input
                                                    name="lastName"
                                                    value={oauthFormData.lastName}
                                                    onChange={handleOAuthChange}
                                                    type="text"
                                                    className="bg-gray-700 border-gray-600 text-white w-full focus:ring-emerald-500 focus:border-emerald-500"
                                                    required
                                                    placeholder="Doe"
                                                />
                                            </div>
                                        </div>
                                    )}

                                    <div className="bg-gray-700/50 p-4 rounded-lg border border-gray-600">
                                        <h3 className="text-sm font-medium text-gray-300 mb-2 flex items-center gap-2">
                                            <InfoIcon className="w-4 h-4"/>
                                            Quick Registration Notice
                                        </h3>
                                        <p className="text-xs text-gray-400">
                                            {oauthFormData.provider
                                                ? `We'll use your ${oauthFormData.provider} profile to create your account. You'll authenticate natively after registration.`
                                                : 'By creating an account, you agree to share basic profile information with the requesting application.'}
                                        </p>
                                    </div>

                                    <div className="pt-2">
                                        <Button
                                            type="submit"
                                            className="w-full bg-emerald-600 hover:bg-emerald-500 text-white py-3 rounded-lg font-medium"
                                        >
                                            {oauthFormData.provider
                                                ? `Register with ${oauthFormData.provider} Account`
                                                : 'Continue with Manual Registration'}
                                        </Button>
                                    </div>
                                </form>
                            </TabsContent>
                        </Tabs>
                    </CardContent>

                    <CardFooter className="flex justify-center border-t border-gray-700 p-6">
                        <p className="text-sm text-gray-400">
                            Already have an account?{' '}
                            <Link to="/login" className="text-emerald-500 hover:underline">
                                Sign in here
                            </Link>
                        </p>
                    </CardFooter>
                </Card>
            </div>
        </Layout>
    );
}
