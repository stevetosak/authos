import * as React from "react";
import { GalleryVerticalEnd, Minus, Plus, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";
import { SearchForm } from "@/components/ui/search-form.tsx";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible.tsx";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  SidebarRail,
} from "@/components/ui/sidebar.tsx";

const data = {
  navMain: [
    {
      title: "Introduction",
      items: [
        { title: "What is Authos?", url: "/docs/introduction/what-is-authos" },
        { title: "Key Features", url: "/docs/introduction/features" },
        { title: "Getting Started", url: "/docs/introduction/getting-started" },
      ],
    },
    {
      title: "Setup & Configuration",
      url: "/docs/setup",
      items: [
        { title: "Installation", url: "/docs/setup/installation" },
        { title: "Project Structure", url: "/docs/setup/structure" },
        { title: "Configuration Options", url: "/docs/setup/configuration" },
      ],
    },
    {
      title: "Core Concepts",
      url: "/docs/core-concepts",
      items: [
        { title: "Applications", url: "/docs/core-concepts/applications" },
        { title: "Groups & SSO", url: "/docs/core-concepts/groups" },
        { title: "Users & Identifiers", url: "/docs/core-concepts/users" },
        { title: "OIDC Flows", url: "/docs/core-concepts/oidc" },
        { title: "PKCE Support", url: "/docs/core-concepts/pkce" },
      ],
    },
    {
      title: "Security",
      url: "/docs/security",
      items: [
        { title: "Token Signing", url: "/docs/security/tokens" },
        { title: "Subject Identifiers", url: "/docs/security/subjects" },
        { title: "Security Best Practices", url: "/docs/security/best-practices" },
      ],
    },
    {
      title: "Client Libraries",
      url: "/docs/clients",
      items: [
        { title: "Using Authos.js", url: "/docs/clients/authos-js" },
        { title: "React Integration", url: "/docs/clients/react" },
        { title: "Vue Integration", url: "/docs/clients/vue" },
        { title: "Angular Integration", url: "/docs/clients/angular" },
      ],
    },
    {
      title: "Analytics",
      url: "/docs/analytics",
      items: [
        { title: "App Usage Metrics", url: "/docs/analytics/usage" },
        { title: "User Data Requests", url: "/docs/analytics/data-requests" },
      ],
    },
    {
      title: "CLI Reference",
      url: "/docs/cli",
      items: [
        { title: "Installation", url: "/docs/cli/installation" },
        { title: "Commands Overview", url: "/docs/cli/commands" },
      ],
    },
    {
      title: "Advanced Topics",
      url: "/docs/advanced",
      items: [
        { title: "Edge Runtime", url: "/docs/advanced/edge" },
        { title: "Configuration Options", url: "/docs/advanced/config" },
        { title: "File Conventions", url: "/docs/advanced/files" },
        { title: "Supported Browsers", url: "/docs/advanced/browsers" },
      ],
    },
  ],
};

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
      <Sidebar {...props} className="bg-gradient-dark backdrop-blur-sm">
        <SidebarHeader className="p-4 border-b bg-gradient">
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton
                  size="lg"
                  asChild
                  className="hover:bg-gray-700/50 transition-colors"
              >
                <a href="/docs" className="flex items-center gap-3">
                  <div className="bg-emerald-500/10 text-emerald-400 flex aspect-square size-8 items-center justify-center rounded-lg">
                    <GalleryVerticalEnd className="size-4" />
                  </div>
                  <div className="flex flex-col gap-0.5 leading-none">
                    <span className="font-medium text-white">Documentation</span>
                    <span className="text-xs text-gray-400">v1.0.0</span>
                  </div>
                </a>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
          <SearchForm
              className="mt-4"
              // inputClassName="bg-gray-700 border-gray-600 text-white focus:ring-emerald-500"
          />
        </SidebarHeader>

        <SidebarContent className="p-2 bg-sidebar">
          <SidebarGroup>
            <SidebarMenu>
              {data.navMain.map((item, index) => (
                  <Collapsible
                      key={item.title}
                      defaultOpen={index === 0}
                      className="group/collapsible"
                  >
                    <SidebarMenuItem>
                      <CollapsibleTrigger asChild>
                        <SidebarMenuButton
                            className="w-full hover:bg-gray-700/50 text-gray-300 hover:text-white transition-colors"
                            asChild
                        >
                          <a href={item.url} className="flex items-center justify-between">
                            <span>{item.title}</span>
                            <div className="flex items-center">
                              <Plus className="size-4 group-data-[state=open]/collapsible:hidden" />
                              <Minus className="size-4 group-data-[state=closed]/collapsible:hidden" />
                            </div>
                          </a>
                        </SidebarMenuButton>
                      </CollapsibleTrigger>

                      {item.items?.length ? (
                          <CollapsibleContent>
                            <SidebarMenuSub className="ml-4 border-l border-gray-700/50 pl-2">
                              {item.items.map((subItem) => (
                                  <SidebarMenuSubItem key={subItem.title}>
                                    <SidebarMenuSubButton
                                        asChild
                                        className="text-gray-400 hover:text-emerald-400 hover:bg-gray-700/30"
                                    >
                                      <a href={subItem.url} className="flex items-center gap-2">
                                        <ChevronRight className="size-3 text-emerald-400/50" />
                                        {subItem.title}
                                      </a>
                                    </SidebarMenuSubButton>
                                  </SidebarMenuSubItem>
                              ))}
                            </SidebarMenuSub>
                          </CollapsibleContent>
                      ) : null}
                    </SidebarMenuItem>
                  </Collapsible>
              ))}
            </SidebarMenu>
          </SidebarGroup>
        </SidebarContent>

        <SidebarRail className="" />
      </Sidebar>
  );
}