import React, {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import "./LoginForm.css";
import {validatePassword, validateUsername} from "./validators";


const LoginForm: React.FC = () => {
    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
    });

    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const navigate = useNavigate();

    const validateForm = () => {
        const newErrors: { username?: string; password?: string } = {};

        // Используем общие функции
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

        // Убираем ошибку при вводе исправленных данных
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
            const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(credentials),
            });

            if (response.ok) {
                const token = await response.text(); // Получаем токен как текст
                localStorage.setItem("jwtToken", token); // Сохраняем токен в локальное хранилище

                alert("Успешный вход в систему!");
                navigate("/"); // Редирект на главную страницу
            } else {
                alert("Ошибка входа. Проверьте имя пользователя и пароль.");
            }
        } catch (error) {
            console.error("Ошибка:", error);
            alert("Не удалось войти. Попробуйте позже.");
        }
    };

    return (
        <div className="login-container">
            <div className="header">
                <img
                    className="logo"
                    src="/logo-circle192.png"
                    alt="Логотип"
                />
                <h1 className="login-title">Добро пожаловать!</h1>
            </div>

            <form onSubmit={handleSubmit} className="login-form">
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

                <button type="submit" className="login-button">Войти</button>
            </form>

            {/* Текст с ссылкой на регистрацию */}
            <div className="signup-prompt">
                Not a member? <Link to="/register" className="signup-link">Signup now</Link>
            </div>
        </div>
    );
};

export default LoginForm;
