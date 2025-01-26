import React, {useState} from "react";
import {Navigate} from "react-router-dom";
import {jwtDecode} from "jwt-decode";
import LoginPopup from "./LoginPopup";

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

    if (!token || isTokenExpired(token)) {
        if (!showLoginPopup) {
            setShowLoginPopup(true); // Показываем попап только один раз
        }
        localStorage.removeItem("jwtToken"); // Удаляем истёкший или отсутствующий токен
    }

    const handleLoginSuccess = () => {
        setShowLoginPopup(false); // Закрываем попап после успешного входа
    };

    const handlePopupClose = () => {
        setShowLoginPopup(false); // Закрываем попап, если пользователь решил его закрыть
        return <Navigate to="/" replace/>; // При желании, можно перенаправить на главную
    };

    return (
        <>
            {showLoginPopup && (
                <LoginPopup
                    onClose={handlePopupClose}
                    onLoginSuccess={handleLoginSuccess}
                />
            )}
            {!showLoginPopup && token && !isTokenExpired(token) && children}
        </>
    );
};

export default PrivateRoute;
