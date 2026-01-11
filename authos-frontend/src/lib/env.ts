type EnvConfig = {
    API_URL: string;
    BASE_URL: string;
};

declare global {
    interface Window {
        __CONFIG__?: EnvConfig;
    }
}

const mode = import.meta.env.MODE;
const loadEnvConfig = (): EnvConfig => {
    if (mode === "development") {
        const { VITE_API_BASE_URL, VITE_BASE_URL } = import.meta.env;

        if (!VITE_API_BASE_URL || !VITE_BASE_URL) {
            throw new Error("Missing Vite env variables for development");
        }

        return {
            API_URL: VITE_API_BASE_URL,
            BASE_URL: VITE_BASE_URL,
        };
    }

    if (mode === "production") {
        const config = window.__CONFIG__;

        if (!config) {
            throw new Error("Production runtime config not loaded");
        }

        return config;
    }

    throw new Error(`Unsupported mode: ${mode}`);
}

export const envConfig = loadEnvConfig();
