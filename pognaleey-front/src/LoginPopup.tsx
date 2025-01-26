import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import "./LoginPopup.css";
import {validatePassword, validateUsername} from "./validators";
import {useAppContext} from "./AppContext";

interface LoginPopupProps {
    onClose: () => void;
    onLoginSuccess: () => void; // Callback для успешного входа
}

const LoginPopup: React.FC<LoginPopupProps> = ({onClose, onLoginSuccess}) => {
    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
    });
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const {loginUser} = useAppContext(); // Используем loginUser из контекста

    const validateForm = () => {
        const newErrors: { username?: string; password?: string } = {};

        const usernameError = validateUsername(credentials.username);
        if (usernameError) {
            newErrors.username = usernameError;
        }

        const passwordError = validatePassword(credentials.password);
        if (passwordError) {
            newErrors.password = passwordError;
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setCredentials({
            ...credentials,
            [name]: value,
        });

        setErrors({
            ...errors,
            [name]: "",
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(credentials),
            });

            if (response.ok) {
                const token = await response.text();
                loginUser(token); // Обновляем контекст пользователя
                localStorage.setItem("jwtToken", token); // Сохранение токена в localStorage
                onLoginSuccess(); // Вызываем callback успешного входа
                navigate("/");
            } else {
                alert("Ошибка входа. Проверьте имя пользователя и пароль.");
            }
        } catch (error) {
            console.error("Ошибка при входе:", error);
            alert("Не удалось войти. Попробуйте позже.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="popup-overlay" onClick={onClose}>
            <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                <h3>Вход в аккаунт</h3>
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
                        {errors.username && <p className="error-message">{errors.username}</p>}
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
                        {errors.password && <p className="error-message">{errors.password}</p>}
                    </div>

                    <button type="submit" className="login-button-popup" disabled={isSubmitting}>
                        {isSubmitting ? "Вход..." : "Войти"}
                    </button>
                </form>
                {/* Текст с ссылкой на регистрацию */}
                <div className="signup-prompt">
                    {/*TODO: Add logic*/}
                </div>
            </div>

        </div>
    );
};

export default LoginPopup;
