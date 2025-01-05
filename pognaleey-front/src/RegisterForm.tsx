import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import "./RegisterForm.css";

const RegisterForm: React.FC = () => {
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        confirmPassword: "",
    });
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData({
            ...formData,
            [name]: value,
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError("Пароли не совпадают.");
            return;
        }

        try {
            const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/register`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    username: formData.username,
                    password: formData.password,
                }),
            });

            if (response.ok) {
                alert("Регистрация успешна! Теперь вы можете войти.");
                navigate("/login"); // Редирект на страницу логина
            } else {
                const errorMessage = await response.text();
                setError(errorMessage || "Ошибка регистрации.");
            }
        } catch (err) {
            console.error("Ошибка:", err);
            setError("Не удалось зарегистрироваться. Попробуйте позже.");
        }
    };

    return (
        <div className="register-container">
            <div className="header">
                <img
                    className="logo"
                    src="/logo-circle192.png"
                    alt="Логотип"
                />
                <h1 className="register-title">Создайте аккаунт</h1>
            </div>

            <form onSubmit={handleSubmit} className="register-form">
                {error && <div className="error-message">{error}</div>}

                <div className="form-group">
                    <label htmlFor="username">Имя пользователя:</label>
                    <input
                        type="text"
                        id="username"
                        name="username"
                        value={formData.username}
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
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="confirmPassword">Повторите пароль:</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        required
                    />
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
