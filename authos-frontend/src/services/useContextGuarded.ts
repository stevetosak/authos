import {Context, useContext} from "react";

export const useContextGuarded = <T>(context: Context<T | null>) : T => {
    if(context == null) throw new Error("Invalid use of context")

    return useContext<T>(context as Context<T>)

}