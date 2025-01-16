import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import "./RegisterForm.css";
import {validateConfirmPassword, validatePassword, validateUsername} from "./validators";

const RegisterForm: React.FC = () => {
    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
        confirmPassword: "",
    });

    const [errors, setErrors] = useState<{ username?: string; password?: string; confirmPassword?: string }>({});
    const navigate = useNavigate();

    const validateForm = () => {
        const newErrors: { username?: string; password?: string; confirmPassword?: string } = {};

        // Используем общие функции
        const usernameError = validateUsername(credentials.username);
        if (usernameError) {
            newErrors.username = usernameError;
        }

        const passwordError = validatePassword(credentials.password);
        if (passwordError) {
            newErrors.password = passwordError;
        }

        const confirmPasswordError = validateConfirmPassword(credentials.password, credentials.confirmPassword);
        if (confirmPasswordError) {
            newErrors.confirmPassword = confirmPasswordError;
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

        // Убираем ошибку при исправлении
        setErrors({
            ...errors,
            [name]: "",
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return; // Если форма не прошла валидацию, не отправляем запрос
        }

        try {
            const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/register`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    username: credentials.username,
                    password: credentials.password,
                }),
            });

            if (response.ok) {
                alert("Регистрация успешна! Теперь вы можете войти.");
                navigate("/login"); // Редирект на страницу логина
            } else {
                const errorMessage = await response.text();
                alert(errorMessage || "Ошибка регистрации");
            }
        } catch (err) {
            console.error("Ошибка:", err);
            setErrors({username: "Не удалось зарегистрироваться. Попробуйте позже."});
        }
    };

    return (
        <div className="register-container">
            <div className="header">
                <img
                    className="logo"
                    src="/logo192.png"
                    alt="Логотип"
                />
                <h1 className="register-title">Создайте аккаунт</h1>
            </div>

            <form onSubmit={handleSubmit} className="register-form">
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

                <div className="form-group">
                    <label htmlFor="confirmPassword">Повторите пароль:</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={credentials.confirmPassword}
                        onChange={handleChange}
                        required
                    />
                    {errors.confirmPassword && <p className="error-message">{errors.confirmPassword}</p>}
                </div>

                <button type="submit" className="register-button">Зарегистрироваться</button>
            </form>

            <div className="login-prompt">
                Уже есть аккаунт? <a href="/login" className="login-link">Войти</a>
            </div>
        </div>
    );
};

export default RegisterForm;
