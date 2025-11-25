import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog.tsx";
import {Button} from "@/components/ui/button.tsx";

type ConfirmDisable2FAModalProps = {showConfirmationModal:boolean, onOpenChange: (open: boolean) => void,onSubmit: () => void}
export const ConfirmDisable2FAModal = ({showConfirmationModal,onOpenChange,onSubmit}: ConfirmDisable2FAModalProps) => {


    return (
        <Dialog open={showConfirmationModal} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        Are you sure you want to disable 2FA?
                    </DialogTitle>
                    <DialogDescription>
                        2FA offers great and reliable security for your account, it is NOT recommended to disable it.
                    </DialogDescription>
                </DialogHeader>
                <DialogDescription>
                    <div className={'flex flex-row justify-end gap-2'}>
                        <Button variant={'destructive'} onClick={onSubmit}>Disable</Button>
                        <Button variant={'secondary'} onClick={() => onOpenChange(false)}>Cancel</Button>
                    </div>
                </DialogDescription>

            </DialogContent>
        </Dialog>

    )
}