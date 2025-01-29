import React, {useState} from "react";
import {jwtDecode} from "jwt-decode";
import LoginPopup from "../components/LoginPopup/LoginPopup";
import {useTranslation} from "react-i18next";

interface PrivateRouteProps {
    children: React.ReactNode;
}

interface JwtPayload {
    exp: number; // Время истечения токена в формате UNIX
}

const isTokenExpired = (token: string): boolean => {
    try {
        const decoded = jwtDecode<JwtPayload>(token);
        const currentTime = Math.floor(Date.now() / 1000); // Текущее время в секундах
        return decoded.exp < currentTime; // Если срок истек, возвращаем true
    } catch (error) {
        console.error("Ошибка при проверке токена:", error);
        return true; // Если не удалось декодировать, считаем токен истекшим
    }
};

const PrivateRoute: React.FC<PrivateRouteProps> = ({children}) => {
    const token = localStorage.getItem("jwtToken");
    const [showLoginPopup, setShowLoginPopup] = useState(false);
    const {t} = useTranslation();

    if (token && isTokenExpired(token)) {
        localStorage.removeItem("jwtToken"); // Удаляем истёкший или отсутствующий токен

        if (!showLoginPopup) {
            alert(t("sessionExpired"));
            setShowLoginPopup(true);
        }
    }

    const handleLoginSuccess = () => {
        setShowLoginPopup(false);
    };

    const handlePopupClose = () => {
        setShowLoginPopup(false);
    };

    return (
        <>
            {showLoginPopup && (
                <LoginPopup
                    onClose={handlePopupClose}
                    onLoginSuccess={handleLoginSuccess}
                />
            )}
            {children}
        </>
    );
};

export default PrivateRoute;
