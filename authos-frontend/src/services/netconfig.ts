import axios from "axios";
import Cookies from 'js-cookie';
import {envConfig} from "@/lib/env.ts";

export const api = axios.create({
    baseURL: envConfig.API_URL,
    timeout: 10000,
});


export const apiGetAuthenticated = async <T> (uri : string) => {
    console.log(envConfig.API_URL)
    console.log(envConfig.BASE_URL)
    const xsrfCookie = Cookies.get("XSRF-TOKEN")
    return await api.get<T>(uri,{
        withCredentials: true,
        withXSRFToken: true,
        headers: {
            "X-XSRF-TOKEN": xsrfCookie
        }
    })
}
export const apiPostAuthenticated = async <T> (uri : string,data?: any,contentType?: string) => {
    const xsrfCookie = Cookies.get("XSRF-TOKEN")
    return await api.post<T>(uri,data,{
        withCredentials: true,
        withXSRFToken: true,
        headers: {
            "X-XSRF-TOKEN": xsrfCookie,
            "Content-Type": contentType
        }
    })
}
