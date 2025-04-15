import './App.css'
import {Route, BrowserRouter as Router} from "react-router-dom";
import {Routes} from "react-router";
import LoginPage from "./Pages/LoginPage/LoginPage.tsx";
import HomePage from "@/Pages/HomePage/HomePage.tsx";
import Dashboard from "@/Pages/Dashboard/Dashboard.tsx";
import ConsentForm from "@/Pages/ConsentPage/ConsentForm.tsx";
import ErrorPage from "@/Pages/ErrorPage/ErrorPage.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";
import UserRegistrationPage from "@/Pages/UserRegistrationPage/UserRegistrationPage.tsx";
import {AuthProvider} from "@/Pages/AuthContext.tsx";


function App() {


    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path={"/login"} element={<LoginPage/>}/>
                    <Route path={"/"} element={<HomePage/>}/>
                    <Route path={"/dashboard"} element={<Dashboard/>}/>
                    <Route path="/oauth/user-consent" element={<ConsentForm/>}/>
                    <Route path="/error" element={<ErrorPage/>}/>
                    <Route path={"/register-app"} element={<ClientRegistration/>}></Route>
                    <Route path={"/register-user"} element={<UserRegistrationPage/>}></Route>
                </Routes>
            </Router>
        </AuthProvider>
    )
}

export default App
