import React from "react";
import {Navigate} from "react-router-dom";
import {jwtDecode} from "jwt-decode";

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


const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const token = localStorage.getItem("jwtToken");

    if (!token || isTokenExpired(token)) {
        return <Navigate to="/login" replace/>;
    }

    return <>{children}</>;
};

export default PrivateRoute;
