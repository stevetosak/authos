import { Link, useLocation } from "react-router-dom";
import {Grid, Key, LayoutDashboard, LogOut, Plus, Settings, User, Users} from "lucide-react";
import React from "react";
import {Dialog, DialogContent, DialogTrigger} from "@/components/ui/dialog.tsx";
import {Button} from "@/components/ui/button.tsx";
import {
    DropdownMenu,
    DropdownMenuContent, DropdownMenuItem,
    DropdownMenuLabel, DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu.tsx";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";
import {useAuth} from "@/services/useAuth.ts";
import {SidebarTrigger} from "@/components/ui/sidebar.tsx";

const Navbar = () => {
    const location = useLocation();
    const {user} = useAuth();

    return (
        <nav className="sticky top-0 z-50 border-b border-gray-700/50 bg-gray-800/80 backdrop-blur-md rounded-t-2xl">

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Left side - Logo + Main nav */}
                    <div className="flex items-center space-x-4">
                        {/* Logo/Brand */}
                        <Link to="/" className="flex items-center space-x-2">
                            <Key className="w-6 h-6 text-green-400" />
                            <span className="text-xl font-bold bg-gradient-to-r from-green-400 to-green-600 bg-clip-text text-transparent">
                AuthOS
              </span>
                        </Link>

                        {/* Main navigation */}
                        <div className="hidden md:flex items-center space-x-1">
                            <NavLink
                                to="/dashboard"
                                isActive={location.pathname === '/dashboard'}
                                icon={<LayoutDashboard className="w-5 h-5" />}
                            >
                                Dashboard
                            </NavLink>
                            <NavLink
                                to="/applications"
                                isActive={location.pathname.startsWith('/applications')}
                                icon={<Grid className="w-5 h-5" />}
                            >
                                Applications
                            </NavLink>
                            <NavLink
                                to="/users"
                                isActive={location.pathname.startsWith('/users')}
                                icon={<Users className="w-5 h-5" />}
                            >
                                Users
                            </NavLink>
                        </div>
                    </div>

                    {/* Right side - Actions + User */}
                    <div className="flex items-center space-x-3">
                        {/* App Registration Button (highlighted) */}
                        <Button
                            variant="outline"
                            className="hidden md:flex items-center gap-2 border-green-400/30 text-green-400 hover:bg-green-400/10 hover:text-green-300"
                        >
                            <Plus className="w-4 h-4" />
                            <span>New Application</span>
                        </Button>

                        {/* User dropdown */}
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                                    <Avatar className="h-8 w-8 border border-gray-600">
                                        <AvatarImage src={user?.email} />
                                        <AvatarFallback className="bg-gray-700">
                                            {user.firstName.charAt(0) || "U"}
                                        </AvatarFallback>
                                    </Avatar>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent
                                className="w-56 bg-gray-800 border border-gray-700 text-white"
                                align="end"
                                forceMount
                            >
                                <DropdownMenuLabel className="font-normal">
                                    <div className="flex flex-col space-y-1">
                                        <p className="text-sm font-medium leading-none">{user?.name}</p>
                                        <p className="text-xs leading-none text-gray-400">{user?.email}</p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator className="bg-gray-700" />
                                <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700">
                                    <User className="mr-2 h-4 w-4" />
                                    Profile
                                </DropdownMenuItem>
                                <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700">
                                    <Settings className="mr-2 h-4 w-4" />
                                    Settings
                                </DropdownMenuItem>
                                <DropdownMenuSeparator className="bg-gray-700" />
                                <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700 text-red-400">
                                    <LogOut className="mr-2 h-4 w-4" />
                                    Log out
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            </div>

            {/* Mobile menu (simplified) */}
            <div className="md:hidden border-t border-gray-700/50 px-4 py-2">
                <div className="flex items-center justify-around space-x-1">
                    <MobileNavLink
                        to="/dashboard"
                        isActive={location.pathname === '/dashboard'}
                        icon={<LayoutDashboard className="w-5 h-5" />}
                    />
                    <MobileNavLink
                        to="/applications"
                        isActive={location.pathname.startsWith('/applications')}
                        icon={<Grid className="w-5 h-5" />}
                    />
                    <Dialog>
                        <DialogTrigger asChild>
                            <Button
                                size="icon"
                                variant="ghost"
                                className="text-gray-400 hover:text-green-400"
                            >
                                <Plus className="w-5 h-5" />
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-4xl bg-gray-800 border border-gray-700 p-0 overflow-hidden">
                            <ClientRegistration />
                        </DialogContent>
                    </Dialog>
                    <MobileNavLink
                        to="/users"
                        isActive={location.pathname.startsWith('/users')}
                        icon={<Users className="w-5 h-5" />}
                    />
                </div>
            </div>
        </nav>
    );
};

// Reusable nav link component
const NavLink = ({ to, children, isActive, icon }) => (
    <Link
        to={to}
        className={`flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors ${
            isActive
                ? 'bg-gray-700/50 text-green-400'
                : 'text-gray-300 hover:bg-gray-700/30 hover:text-white'
        }`}
    >
        <span className="mr-2">{icon}</span>
        {children}
    </Link>
);

// Mobile version (icon only)
const MobileNavLink = ({ to, isActive, icon }) => (
    <Link
        to={to}
        className={`flex-1 flex justify-center py-2 px-1 rounded-md transition-colors ${
            isActive
                ? 'text-green-400'
                : 'text-gray-400 hover:text-white'
        }`}
    >
        {icon}
    </Link>
);

export default Navbar;