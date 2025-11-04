import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Mail, User as UserIcon} from "lucide-react";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {PhoneInput} from "@/components/phone-input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {ProfileTabProps, User} from "@/services/types.ts";

type AccountTabProps = {user: User}


export const AccountTab = ({active,user} : ProfileTabProps & AccountTabProps) => {
    return (
        <>
            {active && (
                <Card className="bg-gray-800/50 border border-gray-700/50 backdrop-blur-sm">
                    <CardHeader className="border-b border-gray-700/50">
                        <CardTitle className="flex items-center gap-2">
                            <UserIcon className="w-5 h-5 text-emerald-400"/>
                            Account Information
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="p-6 space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <Label className="text-gray-300 mb-2 block">Name</Label>
                                <Input
                                    defaultValue={user.firstName}
                                    className="bg-gray-700 border-gray-600 text-white"
                                />
                            </div>
                            <div>
                                <Label className="text-gray-300 mb-2 block flex items-center gap-2">
                                    <Mail className="w-4 h-4"/> Email Address
                                </Label>
                                <Input
                                    defaultValue={user.email}
                                    className="bg-gray-700 border-gray-600 text-white"
                                    disabled
                                />
                            </div>

                        </div>
                        <div className="flex justify-end">
                            <Button className="bg-emerald-600 hover:bg-emerald-500">
                                Update Profile
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            )}
        </>
    )
}