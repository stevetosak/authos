import axios from "axios";
import Cookies from 'js-cookie';

export const api = axios.create({
    // baseURL: import.meta.env.VITE_API_BASE_URL,
    baseURL: "https://api.authos.imaps.mk",
    timeout: 10000,
});


export const apiGetAuthenticated = async <T> (uri : string) => {
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
