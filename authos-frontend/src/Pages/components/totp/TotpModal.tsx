import {DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog.tsx";
import {Shield} from "lucide-react";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Dialog} from "@radix-ui/react-dialog";
import {useEffect, useState} from "react";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/netconfig.ts";
import {TotpModalProps} from "@/services/types.ts";


export const TotpModal = ({dialogOpen, onOpenChange, user, onSubmit,onSuccess = () => {}}: TotpModalProps) => {


}