import {ChevronDown, FileText, Code, AlertTriangle, Info, ArrowRight, Check, Copy} from "lucide-react";
import React, {JSX, useState} from "react";
import Layout from "@/Pages/components/Layout.tsx";

interface DocSectionProps {
    title: string,
    level: number,
    children?: React.ReactNode
}

export const DocSection = ({title, level = 2, children}: DocSectionProps) => {
    const headingMap: { [key: number]: keyof JSX.IntrinsicElements } = {
        2: 'h2',
        3: 'h3',
        4: 'h4',
        5: 'h5',
        6: 'h6'
    };

    const HeadingTag = headingMap[Math.min(6, Math.max(2, level))] || 'h2';
    const padding = `pl-${2 * (level - 1)}`;

    return (
        <div className={"mb-8 " + padding}>
            <HeadingTag className={`flex items-center gap-2 text-white mb-4 ${
                level === 2 ? "text-2xl font-bold border-b border-gray-700 pb-2" :
                    level === 3 ? "text-xl font-semibold mt-6" :
                        "text-lg font-medium mt-4"
            }`}>
                {level === 2 && <FileText className="w-5 h-5 text-emerald-400"/>}
                {level > 2 && <ArrowRight className="w-4 h-4 text-emerald-400"/>}
                {title}
            </HeadingTag>
            <div className="prose prose-invert max-w-none text-gray-300">
                {children}
            </div>
        </div>
    );
};


interface DocCodeProps {
    language: string,
    children?: React.ReactNode,
    text: string
}

export const DocCode = ({language = "javascript", children, text}: DocCodeProps) => {
    const [copied, setCopied] = useState(false);

    const copyToClipboard = () => {
        navigator.clipboard.writeText(text);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="relative my-4">
      <pre className="bg-gray-800/80 border border-gray-700 rounded-lg p-4 overflow-x-auto">
        <code className={`language-${language}`}>{children}</code>
      </pre>
            <button
                onClick={copyToClipboard}
                className="absolute top-2 right-2 p-2 text-gray-400 hover:text-white bg-gray-700/50 rounded"
                title="Copy to clipboard"
            >
                {copied ? <Check className="w-4 h-4"/> : <Copy className="w-4 h-4"/>}
            </button>
        </div>
    );
};

interface DocNoteProps {
    type: string, // ova poseben type spored bgmap
    children?: React.ReactNode
}

export const DocNote = ({type = "info", children}: DocNoteProps) => {
    const iconMap: { [key: string]: React.ReactElement } = {
        info: <Info className="w-5 h-5 text-blue-400"/>,
        warning: <AlertTriangle className="w-5 h-5 text-yellow-400"/>,
        danger: <AlertTriangle className="w-5 h-5 text-red-400"/>,
    };

    const bgMap: { [key: string]: string } = {
        info: "bg-blue-900/20",
        warning: "bg-yellow-900/20",
        danger: "bg-red-900/20",
    };

    const borderMap: { [key: string]: string } = {
        info: "border-blue-800/50",
        warning: "border-yellow-800/50",
        danger: "border-red-800/50",
    };

    return (
        <div className={`${bgMap[type]} ${borderMap[type]} border-l-4 p-4 my-4 rounded-r-lg flex gap-3`}>
            <div className="flex-shrink-0 pt-0.5">{iconMap[type]}</div>
            <div className="text-gray-300">{children}</div>
        </div>
    );
};

interface DocCollapseProps {
    title: string,
    children?: React.ReactNode
}

export const DocCollapse = ({title, children}: DocCollapseProps) => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <div className="mb-4 border border-gray-700/50 rounded-lg overflow-hidden bg-gray-800/30">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex items-center justify-between p-3 hover:bg-gray-700/30 transition-colors"
            >
                <div className="flex items-center gap-2">
                    <ChevronDown
                        className={`w-4 h-4 text-emerald-400 transition-transform ${isOpen ? "" : "-rotate-90"}`}/>
                    <span className="font-medium text-white">{title}</span>
                </div>
            </button>
            {isOpen && <div className="p-4 pt-2">{children}</div>}
        </div>
    );
};

interface DocumentationPageProps {
    title: string,
    children?: React.ReactNode
}

export const DocumentationPage = ({title, children}: DocumentationPageProps) => {
    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-950 text-white p-4 md:p-8">
            <div className="max-w-4xl mx-auto">
                <div className="mb-8">
                    <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">{title}</h1>
                    <div className="h-1 w-20 bg-emerald-400 rounded-full"></div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">

                    {/* Main Content */}
                    <div className="lg:col-span-4">
                        <div
                            className="bg-gray-800/50 border border-gray-700/50 rounded-lg p-6 md:p-8 backdrop-blur-sm">
                            {children}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

// const ExampleDocumentation = () => (
//     <DocumentationPage title="API Authentication Guide">
//         <DocSection title="Introduction" level={2}>
//             <p>
//                 This guide explains how to authenticate with our API using OAuth 2.0 and OpenID Connect.
//                 You'll need your client credentials to get started.
//             </p>
//         </DocSection>
//
//         <DocSection title="Getting Started" level={3}>
//             <p>First, register your application to obtain client credentials:</p>
//
//             <DocCollapse title="Step-by-step registration">
//                 <ol className="list-decimal pl-5 space-y-2">
//                     <li>Navigate to the Developer Portal</li>
//                     <li>Click "New Application"</li>
//                     <li>Fill in your application details</li>
//                     <li>Copy your Client ID and Secret</li>
//                 </ol>
//             </DocCollapse>
//
//             <DocNote type="warning">
//                 Keep your client secret secure. Never expose it in client-side code.
//             </DocNote>
//         </DocSection>
//
//         <DocSection title="Authentication Flow" level={3}>
//             <p>Use the authorization code flow for most web applications:</p>
//
//             <DocCode language="bash">
//                 {`curl -X POST https://api.yourdomain.com/oauth/token \\
//   -d "client_id=YOUR_CLIENT_ID" \\
//   -d "client_secret=YOUR_CLIENT_SECRET" \\
//   -d "grant_type=authorization_code" \\
//   -d "code=AUTHORIZATION_CODE" \\
//   -d "redirect_uri=YOUR_REDIRECT_URI"`}
//             </DocCode>
//
//             <DocNote type="info">
//                 For single-page apps, consider using PKCE for additional security.
//             </DocNote>
//         </DocSection>
//     </DocumentationPage>
// );