import {TotpModal} from "@/Pages/components/totp/TotpModal.tsx";
import {TotpModalProps} from "@/services/types.ts";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/netconfig.ts";

export const Disable2FATotpModal = ({dialogOpen,onOpenChange,user} : Omit<TotpModalProps, "onSubmit">) => {

    const onSubmit = (otp: string) => {
        return apiGetAuthenticated(`/verify-totp?otp=${otp}`)
    }
    const onSuccess = () => {
        apiPostAuthenticated('/disable-totp')
    }
    return (
        <TotpModal dialogOpen={dialogOpen} onOpenChange={onOpenChange} user={user} onSubmit={onSubmit} onSuccess={onSuccess} key={`${dialogOpen ? 'open' : 'closed'}`}/>
    )

}