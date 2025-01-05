// LoginForm.tsx
import React, {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import "./LoginForm.css";

const LoginForm: React.FC = () => {
    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
    });

    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setCredentials({
            ...credentials,
            [name]: value,
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

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
