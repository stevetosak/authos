import {defaultAppGroup} from "@/services/types.ts";
import {motion} from "framer-motion";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Avatar, AvatarFallback} from "@/components/ui/avatar.tsx";
import {Button} from "@/components/ui/button.tsx";
import {FolderOpen, Settings} from "lucide-react";
import {useAuth} from "@/services/useAuth.ts";
import {useGroupManagement} from "@/Pages/components/hooks/useGroupManagment.ts";
import {useNavigate} from "react-router-dom";
import {useContextGuarded} from "@/services/useContextGuarded.ts";
import {DashboardContext, DashboardContextType} from "@/Pages/Dashboard/components/context/DashboardContext.ts";

export const AppsList = () => {
    const {apps} = useAuth()
    const {selectedGroup} = useContextGuarded<DashboardContextType>(DashboardContext)

    const nav = useNavigate()

    const handleAppClick = (appId:number) => {
        nav(`/dashboard/${appId}`)
    }

    return (
        <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {(selectedGroup !== defaultAppGroup
                        ? apps.filter((app) => app.group === selectedGroup.id)
                        : apps
                )?.map((app) => (
                    <motion.div
                        key={app.id}
                        initial={{opacity: 0, y: 10}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.2}}
                        whileHover={{y: -3}}
                    >
                        <Card
                            className="h-full border border-gray-700/50 hover:border-teal-300/30 transition-colors group overflow-hidden">
                            <CardHeader className="pb-3">
                                <div className="flex items-start justify-between">
                                    <div>
                                        <CardTitle className="flex items-center gap-2">
                                            <div className="w-3 h-3 rounded-full bg-primary flex-shrink-0"/>
                                            {app.name}
                                        </CardTitle>
                                        <CardDescription className="text-gray-400 mt-1 line-clamp-2">
                                            {app.shortDescription || "No description provided"}
                                        </CardDescription>
                                    </div>
                                    <Avatar className="h-10 w-10 border border-gray-600">
                                        <AvatarFallback
                                            className="bg-gray-700/50 group-hover:bg-green-400/10 transition-colors">
                                            {app.name.charAt(0)}
                                        </AvatarFallback>
                                    </Avatar>
                                </div>
                            </CardHeader>
                            <CardContent className="pb-4">
                                {/*<div className="flex gap-2 flex-wrap">*/}
                                {/*    <Badge variant="outline"*/}
                                {/*           className="text-xs border-gray-600 text-gray-300">*/}
                                {/*        {app.type || "OIDC"}*/}
                                {/*    </Badge>*/}
                                {/*    <Badge variant="outline"*/}
                                {/*           className="text-xs border-blue-500/30 text-blue-400">*/}
                                {/*        {app.status || "Active"}*/}
                                {/*    </Badge>*/}
                                {/*</div>*/}
                            </CardContent>
                            <CardFooter className="border-t border-gray-700/50 pt-3">
                                <Button
                                    variant="default"
                                    className="w-full"
                                    onClick={() => handleAppClick(app.id)}
                                >
                                    <Settings
                                        className="w-4 h-4 mr-2 opacity-70 group-hover:opacity-100 transition-opacity"/>
                                    Manage
                                </Button>
                            </CardFooter>
                        </Card>
                    </motion.div>
                ))}
            </div>
            {!(apps.filter(app => app.group == selectedGroup.id).length > 0) && selectedGroup !== defaultAppGroup && (
                <motion.div
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    className="flex flex-col items-center justify-center py-16 text-center"
                >
                    <div className="bg-gray-800/50 border border-gray-700/50 rounded-full p-6 mb-6">
                        <FolderOpen className="w-10 h-10 text-gray-500"/>
                    </div>
                    <h3 className="text-xl font-medium mb-2">No applications found</h3>
                    <p className="text-gray-400 max-w-md mb-6">
                        Get started by adding your first application to this group
                    </p>
                </motion.div>
            )}
        </>
    )
}