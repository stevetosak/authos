
import {useAuth} from "@/services/useAuth.ts";
import {Navigate, Outlet,useLocation} from "react-router-dom";
import {HashLoader} from "react-spinners"

const ProtectedRoute = () => {
    const location = useLocation()
    const {isAuthenticated,authLoading} = useAuth()


    console.log("AUTH: " + isAuthenticated)
    console.log("LOADING: " + authLoading)


    if(!isAuthenticated){
        return <Navigate to={"/login"} state={{targetPath: location}} replace/>
    }

    return <Outlet/>
}

export default ProtectedRoute