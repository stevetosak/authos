import {Link, useLocation, useNavigate} from "react-router-dom";
import {
    Grid,
    Key,
    LayoutDashboard,
    LogInIcon,
    LogOut,
    Plus,
    Settings,
    User,
    Users, Wind
} from "lucide-react";
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
import RegisterAppPage from "@/Pages/ClientRegistrationPage/RegisterAppPage.tsx";
import {useAuth} from "@/services/useAuth.ts";
import  {apiGetAuthenticated} from "@/services/config.ts";
import {toast} from "sonner";
import {defaultUser} from "@/services/types.ts";
import {motion} from "framer-motion";
import axios from "axios";

const Navbar = () => {
    const location = useLocation();
    const {user, isAuthenticated, setUser, setIsAuthenticated} = useAuth();
    const nav = useNavigate()

    const logOut = () => {
        console.log("LOGOUT")
        apiGetAuthenticated("/logout")
            .then(() => {
                toast.warning("Logging Out...")
                setTimeout(() => {
                    setUser(defaultUser)
                    setIsAuthenticated(false)
                }, 1000)
            }).catch(err => {
            console.error(err)
        })
    }

    function silentAuth(){
        axios.get("http://localhost:8785/duster/api/v1/oauth/start?client_id=33e16ab8cdb2c9d01de2400475db0472a1922949c34a3c987750e6abc2b6516f&mode=fresh",{
            withCredentials: true
        }).then(resp => {
            console.log("DUSTER RESPONSE: " + resp)
        })
    }

    return (
        <nav className="sticky top-0 z-50 border-b border-gray-700/50 bg-gray-800/80 backdrop-blur-md rounded-2xl">

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center space-x-4">
                        <Link to="/" className="flex items-center space-x-2">
                            <Key className="w-6 h-6 text-green-400"/>
                            <span
                                className="text-xl font-bold bg-gradient-to-r from-green-400 to-green-600 bg-clip-text text-transparent">
                Authos
              </span>
                        </Link>

                        <div className="hidden md:flex items-center space-x-1">
                            <NavLink
                                to="/dashboard"
                                isActive={location.pathname === '/dashboard'}
                                icon={<LayoutDashboard className="w-5 h-5"/>}
                            >
                                Dashboard
                            </NavLink>
                            <NavLink
                                to="/duster"
                                isActive={location.pathname.startsWith('/duster')}
                                icon={<Wind className="w-5 h-5"/>}
                            >
                                Duster
                            </NavLink>
                            <NavLink
                                to="/users"
                                isActive={location.pathname.startsWith('/users')}
                                icon={<Users className="w-5 h-5"/>}
                            >
                                Users
                            </NavLink>
                        </div>
                    </div>

                    {isAuthenticated && <div className="flex items-center space-x-3">


                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <motion.div
                                    initial={{opacity: 0, y: 10}}
                                    animate={{opacity: 1, y: 0}}
                                    transition={{duration: 0.2}}
                                    whileHover={{y: -3}}>


                                    <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                                        <Avatar className="h-8 w-8 border border-gray-600 hover:border-green-600">
                                            <AvatarImage src={user?.email}/>
                                            <AvatarFallback className="bg-gray-700">
                                                {user.firstName.charAt(0) || "U"}
                                            </AvatarFallback>
                                        </Avatar>
                                    </Button>
                                </motion.div>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent
                                className="w-56 bg-gray-800 border border-gray-700 text-white"
                                align="end"
                                forceMount
                            >
                                <DropdownMenuLabel className="font-normal">
                                    <div className="flex flex-col space-y-1">
                                        <p className="text-sm font-medium leading-none">{user?.firstName}</p>
                                        <p className="text-xs leading-none text-gray-400">{user?.email}</p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator className="bg-gray-700"/>
                                <Link to={"/profile"}>
                                    <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700">
                                        <User className="mr-2 h-4 w-4"/>
                                        Profile
                                    </DropdownMenuItem>
                                </Link>
                                <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700">
                                    <Settings className="mr-2 h-4 w-4"/>
                                    Settings
                                </DropdownMenuItem>
                                <DropdownMenuSeparator className="bg-gray-700"/>
                                <DropdownMenuItem className="hover:bg-gray-700 focus:bg-gray-700 text-red-400">
                                    <Button onClick={logOut}>
                                        <LogOut className="mr-2 h-4 w-4"/>
                                        Log out
                                    </Button>
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>}

                    {!isAuthenticated &&
                        <div className={'flex items-center space-x-5'}>
                            <NavLink
                                to="/login"
                                isActive={location.pathname.startsWith('/login')}
                                icon={<LogInIcon className="w-5 h-5"/>}
                            >
                                Log In
                            </NavLink>
                            <NavLink
                                to="/register"
                                isActive={location.pathname.startsWith('/register')}
                                icon={<LogInIcon className="w-5 h-5"/>}
                            >
                                Sign Up
                            </NavLink>

                        </div>
                    }
                </div>
            </div>

            <div className="md:hidden border-t border-gray-700/50 px-4 py-2">
                <div className="flex items-center justify-around space-x-1">
                    <MobileNavLink
                        to="/dashboard"
                        isActive={location.pathname === '/dashboard'}
                        icon={<LayoutDashboard className="w-5 h-5"/>}
                    />
                    <MobileNavLink
                        to="/applications"
                        isActive={location.pathname.startsWith('/applications')}
                        icon={<Grid className="w-5 h-5"/>}
                    />
                    <Dialog>
                        <DialogTrigger asChild>
                            <Button
                                size="icon"
                                variant="ghost"
                                className="text-gray-400 hover:text-green-400"
                            >
                                <Plus className="w-5 h-5"/>
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-4xl bg-gray-800 border border-gray-700 p-0 overflow-hidden">
                            <RegisterAppPage/>
                        </DialogContent>
                    </Dialog>
                    <MobileNavLink
                        to="/users"
                        isActive={location.pathname.startsWith('/users')}
                        icon={<Users className="w-5 h-5"/>}
                    />
                </div>
            </div>
        </nav>
    );
};

const NavLink = ({to, children, isActive, icon} : {to: string,children: React.ReactNode,isActive: boolean,icon: React.ReactElement}) => (
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

const MobileNavLink = ({to, isActive, icon}: {to: string, isActive: boolean, icon: React.ReactElement}) => (
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