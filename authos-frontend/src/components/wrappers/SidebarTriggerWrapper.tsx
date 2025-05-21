// update path as needed

import {SidebarTrigger, useSidebar} from "@/components/ui/sidebar.tsx";

export function SidebarTriggerWrapper() {
    const { open } = useSidebar();

    return (
        <div
            className={`fixed top-1/2 -translate-y-1/2 z-50 transition-all duration-300`}
            style={{ left: open ? '16rem' : '0' }}
        >
            <SidebarTrigger />
        </div>
    );
}
