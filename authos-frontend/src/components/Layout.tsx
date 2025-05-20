import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/ui/app-sidebar.tsx"
import React from "react";

export default function Layout({ children }: { children: React.ReactNode }) {
    return (
        <SidebarProvider>
            <div className="flex min-h-screen bg-gray-900 text-white w-full">
                <AppSidebar />
                <main className="flex-1 p-8 overflow-y-auto w-3/4]">
                    <SidebarTrigger />
                    {children}
                </main>
            </div>
        </SidebarProvider>
    );
}
