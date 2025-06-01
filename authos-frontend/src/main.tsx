import {createRoot} from 'react-dom/client'
import './index.css'

import React from 'react';
import {AuthProvider} from "@/Pages/components/context/AuthProvider.tsx";
import {RouterProvider} from "react-router-dom";
import {router} from "@/services/Router/router.tsx";

createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AuthProvider>
            <RouterProvider router={router} />
        </AuthProvider>
    </React.StrictMode>
);

