import {Avatar} from "@/components/ui/avatar.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {Button} from "@/components/ui/button.tsx";

export const UserProfile = () => {
    const {user} = useAuth()

    return user.id != -1 && (
        <div className="w-full bg-gray-700 text-white shadow-md px-6 py-4 flex justify-between items-center m-2 rounded-xl">
            <div className="text-lg font-semibold">
               <span className="text-green-400">{user.firstName}</span>
            </div>

            <Button className="bg-gray-700 hover:bg-red-500 text-white font-medium px-4 py-2 rounded-lg">
                Log Out
            </Button>
        </div>

    )
}