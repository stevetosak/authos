import React, { useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";

export const Sidebartest: React.FC = () => {
    // State to manage the dropdown toggles
    const [isAppsDropdownOpen, setIsAppsDropdownOpen] = useState(false);
    const [isSettingsDropdownOpen, setIsSettingsDropdownOpen] = useState(false);

    const toggleAppsDropdown = () => setIsAppsDropdownOpen(prevState => !prevState);
    const toggleSettingsDropdown = () => setIsSettingsDropdownOpen(prevState => !prevState);

    return (
        <aside className="w-64 h-[90vh] overflow-y-auto shrink-0 bg-gray-800 border border-gray-700 rounded-xl shadow-md p-6">
            <nav className="flex flex-col gap-6">
                {/* Home Button as Card */}
                <div className="bg-gray-700 rounded-xl p-4 shadow-md hover:shadow-lg transition-all">
                    <button className="w-full text-left text-white hover:text-green-500">Home</button>
                </div>

                {/* My Apps Dropdown Card */}
                <div className="bg-gray-700 rounded-xl p-4 shadow-md hover:shadow-lg transition-all">
                    <button
                        onClick={toggleAppsDropdown}
                        className="w-full text-left text-white hover:text-green-500 flex items-center justify-between"
                    >
                        My Apps
                        {isAppsDropdownOpen ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                    </button>

                    {isAppsDropdownOpen && (
                        <div className="flex flex-col mt-2 space-y-2">
                            <div className="bg-gray-800 text-gray-300 p-3 rounded-md shadow-md hover:bg-gray-700">
                                App 1
                            </div>
                            <div className="bg-gray-800 text-gray-300 p-3 rounded-md shadow-md hover:bg-gray-700">
                                App 2
                            </div>
                            <div className="bg-gray-800 text-gray-300 p-3 rounded-md shadow-md hover:bg-gray-700">
                                App 3
                            </div>
                        </div>
                    )}
                </div>

                {/* Settings Dropdown Card */}
                <div className="bg-gray-700 rounded-xl p-4 shadow-md hover:shadow-lg transition-all">
                    <button
                        onClick={toggleSettingsDropdown}
                        className="w-full text-left text-white hover:text-green-500 flex items-center justify-between"
                    >
                        Settings
                        {isSettingsDropdownOpen ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                    </button>

                    {isSettingsDropdownOpen && (
                        <div className="flex flex-col mt-2 space-y-2">
                            <div className="bg-gray-800 text-gray-300 p-3 rounded-md shadow-md hover:bg-gray-700">
                                Profile Settings
                            </div>
                            <div className="bg-gray-800 text-gray-300 p-3 rounded-md shadow-md hover:bg-gray-700">
                                Privacy Settings
                            </div>
                        </div>
                    )}
                </div>

                {/* Show Profile Button */}
                <div className="bg-gray-700 rounded-xl p-4 shadow-md hover:shadow-lg transition-all">
                    <button
                        className="w-full text-left text-green-400 hover:text-green-300"
                        onClick={() => {/* Add toggle functionality for profile visibility */}}
                    >
                        Show Profile
                    </button>
                </div>
            </nav>
        </aside>
    );
};

export default Sidebartest;
