import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx"
import { Button } from "@/components/ui/button.tsx"
import { LogOut, Settings, User as LucideUser } from "lucide-react"
import {User} from "@/services/interfaces.ts";

export const UserSidebar = ({ user } : { user : User}) => {
    return user.id != -1 && (
        <div className="flex justify-end">
            <Popover>
                <PopoverTrigger asChild>
                    <Button
                        variant="ghost"
                        className="flex items-center gap-2 text-white hover:bg-gray-700"
                    >
                        <LucideUser className="w-5 h-5"/>
                        <span>{user.firstName}</span>
                    </Button>
                </PopoverTrigger>

                <PopoverContent
                    className="w-48 bg-gray-800 text-white border border-gray-700 shadow-md p-2 rounded-xl"
                    align="end"
                >
                    <div className="flex flex-col gap-2">
                        <Button variant="ghost" className="justify-start hover:bg-gray-700">
                            <LucideUser className="w-4 h-4 mr-2" /> Profile
                        </Button>
                        <Button variant="ghost" className="justify-start hover:bg-gray-700">
                            <Settings className="w-4 h-4 mr-2" /> Settings
                        </Button>
                        <Button variant="ghost" className="justify-start hover:bg-gray-700 text-red-400">
                            <LogOut className="w-4 h-4 mr-2" /> Log Out
                        </Button>
                    </div>
                </PopoverContent>
            </Popover>
        </div>
    )
}
