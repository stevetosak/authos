import React from "react";
import {LoginForm} from "@/components/my/login-form.tsx";
import Layout from "@/components/Layout.tsx";


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
