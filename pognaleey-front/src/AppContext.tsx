import React, {createContext, useContext, useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import i18n from "i18next"; // Импортируем i18next

interface AppContextProps {
    user: { username: string | null };
    language: string;
    languages: { code: string; label: string }[];
    handleLogout: () => void;
    handleLanguageChange: (code: string) => void;
    loginUser: (token: string) => void;
}

const AppContext = createContext<AppContextProps | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const navigate = useNavigate();
    const [user, setUser] = useState<{ username: string | null }>({username: null});
    const [language, setLanguage] = useState<string>("ru");
    const [isInitialized, setIsInitialized] = useState<boolean>(false); // Для отслеживания инициализации

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

        setIsInitialized(true); // Контекст инициализирован
    }, []);

    useEffect(() => {
        if (isInitialized && i18n.isInitialized) {
            i18n.changeLanguage(language); // Меняем язык только после полной инициализации
        }
    }, [language, isInitialized]);

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
        navigate("/", {replace: true});
        window.location.reload();
    };

    // Функция для смены языка
    const handleLanguageChange = (code: string) => {
        setLanguage(code); // Обновляем состояние языка
    };

    const loginUser = (token: string) => {
        localStorage.setItem("jwtToken", token);
        const username = getUsernameFromToken(token);
        setUser({username});
    };

    return (
        <AppContext.Provider
            value={{
                user,
                language,
                languages,
                handleLogout,
                handleLanguageChange,
                loginUser,
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
