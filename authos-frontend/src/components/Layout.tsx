import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/ui/app-sidebar.tsx"
import React from "react";
import Navbar from "@/components/Navbar.tsx";
import {SidebarTriggerWrapper} from "@/components/wrappers/SidebarTriggerWrapper.tsx";
import {Toaster} from "@/components/ui/sonner.tsx";

export default function Layout({ children }: { children: React.ReactNode }) {
    return (
        <SidebarProvider defaultOpen={false}>

            <div className="flex flex-row min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white w-full">
                <AppSidebar />
                <SidebarTriggerWrapper/>
                <main className="flex-1 p-8 overflow-y-auto w-3/4]">

                    <Navbar></Navbar>
                    {children}
                </main>
                <Toaster richColors={true} position={"bottom-right"}/>
            </div>
        </SidebarProvider>
    );
}
