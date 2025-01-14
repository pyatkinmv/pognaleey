// Main.tsx
import React from "react";
import "./Main.css";

const Main: React.FC = () => {
    return (
        <div className="main-container">
            <div className="content-container">
                <header className="header">
                    <nav className="navbar">
                        <a href="/" className="nav-link">Главная</a>
                        <a href="/contacts" className="nav-link">Контакты</a>
                        <a href="/login" className="nav-link">Войти</a>
                    </nav>
                </header>
                <img
                    src="/main.webp" // Замените на путь к вашей картинке
                    alt="Main Banner"
                    className="banner-image"
                />
            </div>
        </div>
    );
};

export default Main;
