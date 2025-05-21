import './App.css'
import {Route, BrowserRouter as Router} from "react-router-dom";
import {Routes} from "react-router";
import LoginPage from "./Pages/LoginPage/LoginPage.tsx";
import HomePage from "@/Pages/HomePage/HomePage.tsx";
import Dashboard from "@/Pages/Dashboard/Dashboard.tsx";
import ConsentForm from "@/Pages/ConsentPage/ConsentForm.tsx";
import ErrorPage from "@/Pages/ErrorPage/ErrorPage.tsx";
import UserRegistrationPage from "@/Pages/UserRegistrationPage/UserRegistrationPage.tsx";
import {AuthProvider} from "@/components/context/AuthProvider.tsx";
import ProtectedRoute from "@/components/my/ProtectedRoute.tsx";
import AppDetailsPage from "@/Pages/AppDetailsPage/AppDetails.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";



function App() {


    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route element={<ProtectedRoute/>}>
                        <Route path={"/dashboard"} element={<Dashboard/>}/>
                        <Route path={"/dashboard/:appId"} element={<AppDetailsPage/>}></Route>
                        <Route path={"/connect/register"} element={<ClientRegistration/>}></Route>

                    </Route>
                    <Route path={"/login"} element={<LoginPage/>}/>
                    <Route path={"/"} element={<HomePage/>}/>
                    <Route path="/oauth/user-consent" element={<ConsentForm/>}/>
                    <Route path="/error" element={<ErrorPage/>}/>
                    <Route path={"/register-user"} element={<UserRegistrationPage/>}></Route>
                </Routes>
            </Router>
        </AuthProvider>
    )
}

export default App
