import React, {createContext, useContext, useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";

interface AppContextProps {
    user: { username: string | null };
    language: string;
    languages: { code: string; label: string }[];
    handleLogout: () => void;
    handleLanguageChange: (code: string) => void;
}

const AppContext = createContext<AppContextProps | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const navigate = useNavigate();
    const [user, setUser] = useState<{ username: string | null }>({username: null});
    const [language, setLanguage] = useState<string>("ru");

    const languages = [
        {code: "ru", label: "Русский"},
        {code: "en", label: "English"},
    ];

    useEffect(() => {
        const token = localStorage.getItem("jwtToken");
        if (token) {
            const username = getUsernameFromToken(token);
            setUser({username});
        }
    }, []);

    const getUsernameFromToken = (token: string) => {
        try {
            const payloadBase64 = token.split(".")[1];
            const decodedPayload = JSON.parse(atob(payloadBase64));
            return decodedPayload.name || decodedPayload.sub || null;
        } catch (error) {
            console.error(error);
            return null;
        }
    };

    const handleLogout = () => {
        localStorage.removeItem("jwtToken");
        setUser({username: null});
        navigate("/");
    };

    const handleLanguageChange = (code: string) => setLanguage(code);

    return (
        <AppContext.Provider
            value={{
                user,
                language,
                languages,
                handleLogout,
                handleLanguageChange,
            }}
        >
            {children}
        </AppContext.Provider>
    );
};

export const useAppContext = () => {
    const context = useContext(AppContext);
    if (!context) {
        throw new Error("useAppContext must be used within an AppProvider");
    }
    return context;
};