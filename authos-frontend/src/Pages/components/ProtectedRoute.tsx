
import {useAuth} from "@/services/useAuth.ts";
import {Navigate, Outlet,useLocation} from "react-router-dom";
import {HashLoader} from "react-spinners"

const ProtectedRoute = () => {
    const location = useLocation()
    const {isAuthenticated,loading} = useAuth()

    console.log("AUTH: " + isAuthenticated)
    console.log("LOADING: " + loading)
    if (loading) {
        return (
            <div className="fixed inset-0 flex items-center justify-center z-50">
                <HashLoader loading={true} color="#02ab79" />
            </div>
        );
    }


    if(!isAuthenticated){
        return <Navigate to={"/login"} state={{targetPath: location}} replace/>
    }

    return <Outlet/>
}

export default ProtectedRoute