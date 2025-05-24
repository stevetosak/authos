import React, {ReactNode, useState} from "react";
import {Info, Plus, Users} from "lucide-react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Checkbox} from "@/components/ui/checkbox.tsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.tsx";
import {api} from "@/components/config.ts";
import {AppGroup, CreateAppGroupDTO, MFAPolicyValue, SSOPolicyValue} from "@/services/interfaces.ts";
import {useAuth} from "@/services/useAuth.ts";
import {AxiosResponse} from "axios";
import {toast} from "sonner";

interface TooltipWrapperProps {
    content: ReactNode;
    children?: ReactNode;
    side?: "top" | "right" | "bottom" | "left";
}

const TooltipWrapper = ({content, children, side = "top"}: TooltipWrapperProps) => {
    return (
        <Tooltip>
            <TooltipTrigger asChild>
                {children || <Info className="w-4 h-4 text-gray-400 hover:text-gray-300"/>}
            </TooltipTrigger>
            <TooltipContent side={side} className="bg-gray-700 border-gray-600 text-gray-100 max-w-[300px]">
                {content}
            </TooltipContent>
        </Tooltip>
    );
};

export function AddGroupModal() {
    const [open, setOpen] = useState(false);
    const [groupName, setGroupName] = useState("");
    const [isDefault, setIsDefault] = useState(false);
    const [mfaPolicy, setMfaPolicy] = useState<MFAPolicyValue>("Disabled")
    const [ssoPolicy, setSsoPolicy] = useState<SSOPolicyValue>("Partial")
    const {user,setGroups} = useAuth();

    const resetData = () => {
        setGroupName("");
        setIsDefault(false);
        setMfaPolicy("Disabled");
        setSsoPolicy("Partial");
        setOpen(false)
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const group: CreateAppGroupDTO = {
            name: groupName,
            isDefault: isDefault,
            mfaPolicy: mfaPolicy,
            ssoPolicy: ssoPolicy
        }

        console.log("Creating group:", JSON.stringify(group));

        api.post("/group/add", group, {
            withCredentials: true
        }).then((resp: AxiosResponse<AppGroup>) => {
            setGroups((prevGroups) => [...prevGroups, resp.data]);
            toast.success("Successfully created app group");
            setTimeout(resetData, 300);
        });

    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button
                    variant="outline"
                    size="icon"
                    className="h-8 w-8 border-gray-600 bg-transparent hover:border-green-500 hover:bg-gray-700/50 hover:scale-105 transition-all duration-200 group"
                >
                    <Plus className="w-4 h-4 text-gray-400 group-hover:text-green-400 transition-colors"/>
                </Button>
            </DialogTrigger>
            <DialogContent className="bg-gray-800/90 backdrop-blur-sm border border-gray-700/50 rounded-lg max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-lg text-white">
                        <Users className="w-5 h-5 text-green-400"/>
                        Create New Group
                    </DialogTitle>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-6 mt-4">
                    <div className="space-y-3">
                        <div className="flex items-center gap-2">
                            <Label htmlFor="group-name" className="text-gray-300">
                                Group Name
                            </Label>
                            <TooltipWrapper
                                content="Give your group a descriptive name (e.g., 'Production Apps', 'Internal Tools')"/>
                        </div>
                        <Input
                            id="group-name"
                            type="text"
                            value={groupName}
                            onChange={(e) => setGroupName(e.target.value)}
                            className="bg-gray-700 border-gray-600 text-white focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
                            required
                            placeholder="e.g., Production Apps"
                        />
                    </div>
                    <div className={"flex justify-between"}>


                        <div className="flex items-center space-x-3">
                            <Checkbox
                                id="default-group"
                                checked={isDefault}
                                onCheckedChange={(checked) => setIsDefault(checked as boolean)}
                                className="border-gray-600 data-[state=checked]:bg-emerald-500 data-[state=checked]:border-emerald-500"
                            />
                            <div className="flex items-center gap-2">
                                <Label htmlFor="default-group" className="text-gray-300">
                                    Set as default group
                                </Label>
                                <TooltipWrapper
                                    content="New applications will be added automatically to default group"/>
                            </div>
                        </div>


                        <div className="flex items-center space-x-3">
                            <Select onValueChange={(val) => setSsoPolicy(val as SSOPolicyValue)} value={ssoPolicy}>
                                <SelectTrigger className="w-[180px]">
                                    <SelectValue placeholder="SSO Policy"/>
                                </SelectTrigger>
                                <SelectContent className="bg-gray-700">
                                    <SelectItem value="Full">Full</SelectItem>
                                    <SelectItem value="Partial">Partial</SelectItem>
                                    <SelectItem value="Same-Domain">Same-Domain</SelectItem>
                                    <SelectItem value="Disabled">Disabled</SelectItem>
                                </SelectContent>
                            </Select>

                            <TooltipWrapper
                                content={
                                    <div className="space-y-2 italic">
                                        <p><strong>Full:</strong> Inherit all sessions in this group</p>
                                        <p><strong>Partial:</strong> Require fresh user consent</p>
                                        <p><strong>Same Domain:</strong> SSO enabled across same domain (no consent
                                            required)</p>
                                        <p><strong>Disabled:</strong> SSO completely disabled</p>
                                    </div>
                                }
                            >
                                <button
                                    type="button"
                                    className="text-gray-400 hover:text-gray-300 focus:outline-none"
                                    aria-label="SSO Policy Help"
                                >
                                    <Info className="h-5 w-5"/>
                                </button>
                            </TooltipWrapper>
                        </div>
                    </div>
                    <div>
                        <div className="flex items-center space-x-3">
                            <Select onValueChange={(val) => setMfaPolicy(val as MFAPolicyValue)} value={mfaPolicy}>
                                <SelectTrigger className="w-[180px]">
                                    <SelectValue placeholder="MFA Policy"/>
                                </SelectTrigger>
                                <SelectContent className="bg-gray-700">
                                    <SelectItem value="Email">Email</SelectItem>
                                    <SelectItem value="Phone">Phone</SelectItem>
                                    <SelectItem value="Disabled">Disabled</SelectItem>
                                </SelectContent>
                            </Select>

                            <TooltipWrapper
                                content={
                                    <div className="space-y-2 italic">
                                        <p><strong>Email:</strong> Email confirmation </p>
                                        <p><strong>Phone:</strong> SMS confirmation</p>
                                    </div>
                                }
                            >
                                <button
                                    type="button"
                                    className="text-gray-400 hover:text-gray-300 focus:outline-none"
                                    aria-label="SSO Policy Help"
                                >
                                    <Info className="h-5 w-5"/>
                                </button>
                            </TooltipWrapper>
                        </div>
                    </div>

                    <div className="flex justify-end gap-3 pt-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => resetData()}
                            className="text-gray-300 border-gray-600 hover:bg-gray-700/50"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="bg-emerald-600 hover:bg-emerald-500 text-white"
                        >
                            Create Group
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
}