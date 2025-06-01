import {DocCode, DocCollapse, DocNote, DocSection, DocumentationPage} from "@/Pages/Documentation/components/Docs.tsx";

export const WhatIsAuthos = () => {
    return (
        <DocumentationPage title="Introduction">
            <DocSection title="What is Authos?" level={2}>
                <p className="mb-4 leading-relaxed text-gray-100">
                    Authos is an OpenID compliant Identity Provider designed to streamline secure authentication with OAuth flows.
                    It’s built to be secure and easy to use, coming with a dedicated Authos Client. Developers don’t have to worry about the safety of their authentication flows — Authos takes care of that.
                </p>
                <DocSection title={"Benefits of using an IDP"} level={3}>
                    <DocSection title={"For Developers"} level={3}>
                        Using an IDP (Identity Provider) centralizes authentication and makes it more secure by having a single source of truth.
                        The authentication mechanisms are offloaded from your application to Authos, meaning that your application should only
                        care about its business logic, Authos cares about authenticating your app,
                        authenticating the user and providing the desired information about said user.
                    </DocSection>
                    <DocSection title={"For Users"} level={3}>
                        Authentication is easier - you create an account at the IDP and you can use that account in any applications that supports it.
                        Security - You do not enter credentials on every site you visit meaning there is less chance of getting your credentials stolen
                    </DocSection>

                    <DocNote type={"info"}>
                    </DocNote>
                </DocSection>
                <DocSection title={"Why authos?"} level={2}>
                    You would be right to ask this question because there are a lot of other IDPs like Google, Okta...
                    Authos is made with passion for developers and more importantly for users.
                    In this day and age where user information is bought, sold and used all the time, its important to have
                    transparency and consent, and while this can't be prevented <b>you should have insights as to who is using your data and when</b>.
                </DocSection>

                <p className="mb-4 leading-relaxed text-gray-100">
                    Analytics are also a key part of Authos and are bi-directional:
                </p>

                <ul className="list-disc list-inside mb-6 text-gray-200">
                    <li>
                        <strong>Developers:</strong> Metrics for your applications and groups — requests, flows, login failures, and more.
                    </li>
                    <li>
                        <strong>Users:</strong> Track how your data is being used, by whom and when.
                    </li>
                </ul>
            </DocSection>
        </DocumentationPage>

    )


//     return (
//         <DocumentationPage title="My Documentation">
//             <DocSection title="Getting Started" level={2}>
//                 <p>Welcome to the documentation...</p>
//             </DocSection>
//
//             <DocSection title="Configuration" level={3}>
//                 <p>Here's how to configure...</p>
//                 <DocCode language="javascript" text={"const config = {\n  clientId: 'YOUR_ID',\n  authUrl: 'https://..."}>
//                     {`const config = {
//   clientId: 'YOUR_ID',
//   authUrl: 'https://...'
// }`}
//                 </DocCode>
//             </DocSection>
//         </DocumentationPage>
//     )
}