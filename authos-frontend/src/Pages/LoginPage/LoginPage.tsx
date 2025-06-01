import React from "react";
import {LoginForm} from "@/Pages/components/login-form.tsx";
import Layout from "@/Pages/components/Layout.tsx";


const LoginPage: React.FC = () => {


    return (
        <Layout>
            <div className="flex min-h-screen w-full items-center justify-center">
                <div className="w-full max-w-3xl">
                    <LoginForm/>
                </div>
            </div>
        </Layout>
    );
};

export default LoginPage;
