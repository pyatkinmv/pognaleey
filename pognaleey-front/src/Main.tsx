// Main.tsx
import React from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom"; // Для перехода между страницами


const Main: React.FC = () => {
    const navigate = useNavigate(); // Для редиректа

    const handleButtonClick = () => {
        navigate(`/travel-inquiries`)
    };

    return (
        <div className="main-container">
            <div className="content-container">
                <header className="header">
                    <nav className="navbar">
                        <a href="/" className="nav-link">Главная</a>
                        <a href="/contacts" className="nav-link">Контакты</a>
                        <a href="/language" className="nav-link">Язык</a>
                        <a href="/login" className="nav-link">Войти</a>
                    </nav>
                </header>
                <div className="image-container">
                    <img
                        src="/main.webp" // Замените на путь к вашей картинке
                        alt="Main Banner"
                        className="banner-image"
                    />
                    <div className="banner-text">
                        Каждое путешествие<br/>начинается с идеи!
                    </div>
                    <button className="action-button" onClick={handleButtonClick}>Погнали!</button>
                </div>
            </div>
        </div>
    );
};

export default Main;
