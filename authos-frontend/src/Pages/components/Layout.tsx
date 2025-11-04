import { SidebarProvider} from "@/components/ui/sidebar.tsx"
import { AppSidebar } from "@/components/ui/app-sidebar.tsx"
import React, {useEffect} from "react";
import Navbar from "@/Pages/components/Navbar.tsx";
import {SidebarTriggerWrapper} from "@/Pages/components/wrappers/SidebarTriggerWrapper.tsx";
import {Toaster} from "@/components/ui/sonner.tsx";
import {Outlet, useNavigation} from "react-router-dom";
import {useAuth} from "@/services/useAuth.ts";
import {Loader} from "@/Pages/components/Loader.tsx";

export function Layout() {
    const { pageLoading,setPageLoading,authLoading } = useAuth();
    const navigation = useNavigation();

    useEffect(() => {
        setTimeout(() => {
            setPageLoading(navigation.state === "loading");
        },100)
    }, [navigation.state, setPageLoading]);

    if(authLoading || pageLoading) return  <Loader/>
    return (
            <SidebarProvider>
                <div className="flex flex-row min-h-screen bg-gradient-main text-white w-full">
                    <AppSidebar />
                    <SidebarTriggerWrapper/>
                    <main className="flex-1 py-8 overflow-y-auto w-3/4]">
                        <Navbar></Navbar>
                        <div className={"px-8 flex flex-col justify-center items-center"}>
                            <Outlet/>
                        </div>

                    </main>
                    <Toaster richColors={true} position={"bottom-right"}/>
                </div>
            </SidebarProvider>
    );
}
