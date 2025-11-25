import React, {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardFooter, CardHeader} from "@/components/ui/card";
import axios from "axios";
import {Link, useNavigate,}  from "react-router-dom";
import {InfoIcon, KeyIcon, LockIcon, MailIcon, UserIcon, UserPlusIcon, UsersIcon} from "lucide-react";
import {api} from "@/services/netconfig.ts";

export default function UserRegistration() {
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        name: "",
        surname: ""
    });

    const nav = useNavigate()

    const handleChange = (e : React.ChangeEvent<HTMLInputElement>) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.password != formData.confirmPassword) {
            return;
        }

        const {confirmPassword, ...payload} = formData

        try {
            await api.post("/register", payload)
            alert("Registered Successfully!.")
            nav("/register/confirm")
        } catch (e) {
            alert("Cant register user. Error Occured")
            console.error(e)
        }
    };

    return (

            <div className="min-h-screen text-gray-100 p-4 md:p-10 ">
                <Card
                    className="border border-gray-700 shadow-xl rounded-xl overflow-hidden max-w-2xl mx-auto">
                    <CardHeader className="border-b border-gray-700 p-6">
                        <div className="flex flex-col items-center space-y-2">
                            <h2 className="text-2xl font-bold text-primary">Register</h2>
                        </div>
                    </CardHeader>

                    <CardContent className="p-6">
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
                                            className="w-full  py-3 rounded-lg font-medium"
                                        >
                                            <UserPlusIcon className="w-5 h-5 mr-2"/>
                                            Create Account
                                        </Button>
                                    </div>
                                </form>
                    </CardContent>

                    <CardFooter className="flex justify-center border-t border-gray-700 p-6">
                        <p className="text-sm text-gray-400">
                            Already have an account?{' '}
                            <Link to="/login" className="text-primary hover:underline">
                                Sign in here
                            </Link>
                        </p>
                    </CardFooter>
                </Card>
            </div>
    );
}
