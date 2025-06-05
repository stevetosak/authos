// router.tsx
import {
    createBrowserRouter,
    createRoutesFromElements,
    Route,
} from "react-router-dom";
import HomePage from "@/Pages/HomePage/HomePage.tsx";
import Dashboard from "@/Pages/Dashboard/Dashboard.tsx";
import ConsentForm from "@/Pages/ConsentPage/ConsentForm.tsx";
import ErrorPage from "@/Pages/ErrorPage/ErrorPage.tsx";
import UserRegistrationPage from "@/Pages/UserRegistrationPage/UserRegistrationPage.tsx";
import ProtectedRoute from "@/Pages/components/ProtectedRoute.tsx";
import AppDetailsPage from "@/Pages/AppDetailsPage/AppDetails.tsx";
import RegisterAppPage from "@/Pages/ClientRegistrationPage/RegisterAppPage.tsx";
import {ProfilePage} from "@/Pages/UserProfilePage/UserProfilePage.tsx";
import {WhatIsAuthos} from "@/Pages/Documentation/WhatIsAuthos.tsx";
import LoginPage from "@/Pages/LoginPage/LoginPage.tsx";
import {Layout} from "@/Pages/components/Layout.tsx";

export const router = createBrowserRouter(
    createRoutesFromElements(
        <Route path="/" element={<Layout/>}>
            <Route index element={<HomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<UserRegistrationPage />} />
            <Route path="oauth/user-consent" element={<ConsentForm />} />
            <Route path="error" element={<ErrorPage />} />

            <Route element={<ProtectedRoute />}>
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="dashboard/:appId" element={<AppDetailsPage />} />
                <Route path="connect/register" element={<RegisterAppPage />} />
                <Route path="profile" element={<ProfilePage />} />
            </Route>

            <Route path="docs">
                <Route path="introduction/what-is-authos" element={<WhatIsAuthos />} />
            </Route>
        </Route>
    )
);
