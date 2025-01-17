// LoginPopup.tsx
import React from "react";
import "./LoginPopup.css";

interface LoginPopupProps {
    onClose: () => void;
    onLogin: () => void;
}

const LoginPopup: React.FC<LoginPopupProps> = ({onClose, onLogin}) => {
    return (
        <div className="popup-overlay" onClick={onClose}>
            <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                <h3>Вы не авторизованы</h3>
                <p>Чтобы воспользоваться этой функцией, войдите в аккаунт.</p>
                <button onClick={onLogin} className="login-button-popup">
                    Войти
                </button>
            </div>
        </div>
    );
};

export default LoginPopup;