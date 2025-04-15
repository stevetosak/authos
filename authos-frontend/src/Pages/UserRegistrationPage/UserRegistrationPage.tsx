import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import axios from "axios";
import {useNavigate} from "react-router-dom";

export default function UserRegistration() {
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        name: "",
        surname: ""
    });

    const nav = useNavigate()

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async  (e) => {
        e.preventDefault();
        if(formData.password != formData.confirmPassword) {
            return;
        }

        const {confirmPassword, ...payload} =  formData

        try {
            await axios.post("http://localhost:9000/register",payload)
            alert("Registered Successfully!.")
            nav("/login")
        } catch (e) {
            alert("Cant register user. Error Occured")
            console.error(e)
        }
        // Handle form submission logic here
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-[#111827] text-white p-6">
            <Card className="w-full max-w-3xl bg-[#1f2937] rounded-xl shadow-lg p-6">
                <CardContent className="space-y-6">
                    <h2 className="text-2xl font-bold text-[#10b981] text-center">User Registration</h2>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Email */}
                        <div>
                            <Label className="flex items-center gap-2 p-2">Email
                            </Label>
                            <Input
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                type="email"
                                className="bg-[#111827] border-[#d1d5db] text-white w-full"
                                required
                            />
                        </div>
                        <div>
                            <Label className="flex items-center gap-2 p-2">Name
                            </Label>
                            <Input
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                type="text"
                                className="bg-[#111827] border-[#d1d5db] text-white w-full"
                                required={true}
                            />
                        </div>
                        <div>
                            <Label className="flex items-center gap-2 p-2">Surname
                            </Label>
                            <Input
                                name="surname"
                                value={formData.surname}
                                onChange={handleChange}
                                type="text"
                                className="bg-[#111827] border-[#d1d5db] text-white w-full"
                                required={true}
                            />
                        </div>

                        {/* Password */}
                        <div>
                            <Label className="flex items-center gap-2 p-2">Password
                            </Label>
                            <Input
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                type="password"
                                className="bg-[#111827] border-[#d1d5db] text-white w-full"
                                required={true}
                            />
                        </div>

                        {/* Confirm Password */}
                        <div>
                            <Label className="flex items-center gap-2 p-2">Confirm Password
                            </Label>
                            <Input
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                type="password"
                                className="bg-[#111827] border-[#d1d5db] text-white w-full"
                                required={true}
                            />
                        </div>


                        {/* Submit Button */}
                        <Button
                            className="w-full bg-[#10b981] hover:bg-[#065e44] text-white py-3 rounded-lg text-lg mt-4">
                            Register
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
