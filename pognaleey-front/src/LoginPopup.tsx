import React, {useState} from "react";
import "./LoginPopup.css";
import {validatePassword, validateUsername} from "./validators";
import {useAppContext} from "./AppContext";

interface LoginPopupProps {
    onClose: () => void;
    onLoginSuccess: () => void; // Callback для успешного входа
}

type MessageType = "error" | "success";

interface Message {
    type: MessageType;
    text: string;
}

const LoginPopup: React.FC<LoginPopupProps> = ({onClose, onLoginSuccess}) => {
    const [isLoginMode, setIsLoginMode] = useState(true); // Режим (вход или регистрация)
    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
    });

    const [messages, setMessages] = useState<{
        global?: Message;
        username?: Message;
        password?: Message;
    }>({});

    const [isSubmitting, setIsSubmitting] = useState(false);
    const {loginUser} = useAppContext(); // Используем loginUser из контекста

    const validateForm = () => {
        const newMessages: typeof messages = {};
        const usernameError = validateUsername(credentials.username);
        if (usernameError) {
            newMessages.username = {type: "error", text: usernameError};
        }

        const passwordError = validatePassword(credentials.password);
        if (passwordError) {
            newMessages.password = {type: "error", text: passwordError};
        }

        setMessages(newMessages);
        return Object.keys(newMessages).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setCredentials({...credentials, [name]: value});

        // Сбрасываем сообщение для конкретного поля
        setMessages({...messages, [name]: undefined});
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        setIsSubmitting(true);

        try {
            const endpoint = isLoginMode
                ? `${process.env.REACT_APP_API_URL}/auth/login`
                : `${process.env.REACT_APP_API_URL}/auth/register`;

            const response = await fetch(endpoint, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(credentials),
            });

            if (response.ok) {
                if (isLoginMode) {
                    const token = await response.text();
                    loginUser(token);
                    localStorage.setItem("jwtToken", token);
                    onLoginSuccess();
                } else {
                    setMessages({
                        global: {type: "success", text: "Регистрация успешна! Теперь войдите в аккаунт."},
                    });
                    setIsLoginMode(true); // Переключаемся на вход
                }
            } else {
                setMessages({
                    global: {type: "error", text: isLoginMode ? "Ошибка входа." : "Ошибка регистрации."},
                });
            }
        } catch (error) {
            console.error("Ошибка:", error);
            setMessages({
                global: {type: "error", text: "Произошла ошибка. Попробуйте позже."},
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="popup-overlay" onClick={onClose}>
            <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                <h3>{isLoginMode ? "Вход в аккаунт" : "Регистрация"}</h3>

                {/* Глобальное сообщение */}
                {messages.global && (
                    <p className={`global-message ${messages.global.type}`}>
                        {messages.global.text}
                    </p>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username">Имя пользователя:</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={credentials.username}
                            onChange={handleChange}
                            required
                        />
                        {messages.username && (
                            <p className={`field-message ${messages.username.type}`}>
                                {messages.username.text}
                            </p>
                        )}
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Пароль:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={credentials.password}
                            onChange={handleChange}
                            required
                        />
                        {messages.password && (
                            <p className={`field-message ${messages.password.type}`}>
                                {messages.password.text}
                            </p>
                        )}
                    </div>

                    <button type="submit" className="login-button-popup" disabled={isSubmitting}>
                        {isSubmitting ? "Загрузка..." : isLoginMode ? "Войти" : "Зарегистрироваться"}
                    </button>
                </form>

                <div className="signup-prompt">
                    {isLoginMode ? (
                        <>
                            Ещё нет аккаунта?{" "}
                            <span
                                className="signup-link"
                                onClick={() => setIsLoginMode(false)}
                            >
                                Зарегистрироваться
                            </span>
                        </>
                    ) : (
                        <>
                            Уже есть аккаунт?{" "}
                            <span
                                className="signup-link"
                                onClick={() => setIsLoginMode(true)}
                            >
                                Войти
                            </span>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LoginPopup;
