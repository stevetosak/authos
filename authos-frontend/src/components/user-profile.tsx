import {Avatar} from "@/components/ui/avatar.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {Button} from "@/components/ui/button.tsx";

const UserProfile = () => {
    const {user} = useAuth()

    return (
        <>
            <p>${user.firstName}</p>
            <Button>Log Out</Button>
        </>
    )
}