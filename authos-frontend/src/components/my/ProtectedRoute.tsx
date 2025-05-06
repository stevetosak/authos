
import {useAuth} from "@/services/useAuth.ts";
import {Navigate, Outlet,useLocation} from "react-router-dom";

const ProtectedRoute = () => {
    const location = useLocation()
    const {isAuthenticated} = useAuth()

    console.log("AUTH: " + isAuthenticated)

    if(!isAuthenticated){
        return <Navigate to={"/login"} state={{targetPath: location}} replace/>
    }

    return <Outlet/>
}

export default ProtectedRoute