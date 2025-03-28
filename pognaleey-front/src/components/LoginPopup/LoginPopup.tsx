// LoginPopup.tsx
import React, {useState} from "react";
import styles from "./LoginPopup.module.css";
import {validatePassword, validateUsername} from "../../utils/validators";
import {useAppContext} from "../../context/AppContext";
import {useTranslation} from "react-i18next";

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
    const {t} = useTranslation();

    const [messages, setMessages] = useState<{
        global?: Message;
        username?: Message;
        password?: Message;
    }>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const {loginUser} = useAppContext(); // Используем loginUser из контекста

    const validateForm = () => {
        const newMessages: typeof messages = {};
        const usernameError = validateUsername(credentials.username, t);
        if (usernameError) {
            newMessages.username = {type: "error", text: usernameError};
        }
        const passwordError = validatePassword(credentials.password, t);
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
                        global: {type: "success", text: t("registrationSuccess")},
                    });
                }
            } else {
                setMessages({
                    global: {type: "error", text: isLoginMode ? t("loginError") : t("registerError")},
                });
            }
        } catch (error) {
            console.error(t("error"), error);
            setMessages({
                global: {type: "error", text: t("generalError")},
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className={styles.popupOverlay} onClick={onClose}>
            <div className={styles.popupContent} onClick={(e) => e.stopPropagation()}>
                <h3>{isLoginMode ? t("loginToAccount") : t("registerTitle")}</h3>
                {/* Глобальное сообщение */}
                {messages.global && (
                    <p className={`${styles.globalMessage} ${styles[`globalMessage.${messages.global.type}`]}`}>
                        {messages.global.text}
                    </p>
                )}
                <form onSubmit={handleSubmit}>
                    <div className={styles.formGroup}>
                        <label htmlFor="username">{t("username")}</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={credentials.username}
                            onChange={handleChange}
                            required
                        />
                        {messages.username && (
                            <p className={`${styles.fieldMessage} ${styles[`fieldMessage.${messages.username.type}`]}`}>
                                {messages.username.text}
                            </p>
                        )}
                    </div>
                    <div className={styles.formGroup}>
                        <label htmlFor="password">{t("password")}</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={credentials.password}
                            onChange={handleChange}
                            required
                        />
                        {messages.password && (
                            <p className={`${styles.fieldMessage} ${styles[`fieldMessage.${messages.password.type}`]}`}>
                                {messages.password.text}
                            </p>
                        )}
                    </div>
                    <button type="submit" className={styles.loginButtonPopup} disabled={isSubmitting}>
                        {isSubmitting ? t("loading") : isLoginMode ? t("login") : t("register")}
                    </button>
                </form>
                <div className={styles.signupPrompt}>
                    {isLoginMode ? (
                        <>
                            {t("noAccount")}{" "}
                            <span
                                className={styles.signupLink}
                                onClick={() => setIsLoginMode(false)}
                            >
                                {t("register")}
                            </span>
                        </>
                    ) : (
                        <>
                            {t("alreadyHaveAccount")}{" "}
                            <span
                                className={styles.signupLink}
                                onClick={() => setIsLoginMode(true)}
                            >
                                {t("login")}
                            </span>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LoginPopup;