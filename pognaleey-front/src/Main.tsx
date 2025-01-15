import React from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";

const Main: React.FC = () => {
    const navigate = useNavigate();

    const handleButtonClick = () => {
        navigate(`/travel-inquiries`);
    };

    // Пример данных для плитки
    const tiles = [
        {
            title: "Путешествие в Париж",
            imageUrl: "/logo192.png", // Замените на путь к вашей картинке
            text: "Посетите Эйфелеву башню и наслаждайтесь французской кухней.",
            likes: 120,
        },
        {
            title: "Горы Саян",
            imageUrl: "/logo192.png",
            text: "Погрузитесь в атмосферу сибирской природы.",
            likes: 95,
        },
        {
            title: "Мальдивы",
            imageUrl: "/logo192.png",
            text: "Райские пляжи и бирюзовые воды.",
            likes: 200,
        },
        {
            title: "Амстердам",
            imageUrl: "/logo192.png",
            text: "Каналы, музеи и уютные улочки.",
            likes: 180,
        },
        {
            title: "Япония",
            imageUrl: "/logo192.png",
            text: "Культура и природа Киото",
            likes: 201,
        }
    ];

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
                        src="/main.webp"
                        alt="Main Banner"
                        className="banner-image"
                    />
                    <div className="banner-text">
                        Каждое путешествие<br/>начинается с идеи!
                    </div>
                    <button className="action-button" onClick={handleButtonClick}>Погнали!</button>
                </div>
                {/* Плитка */}
                <div className="tile-container">
                    {tiles.map((tile, index) => (
                        <div className="tile" key={index}>
                            <img src={tile.imageUrl} alt={tile.title} className="tile-image"/>
                            <h3 className="tile-title">{tile.title}</h3>
                            <p className="tile-text">{tile.text}</p>
                            <div className="tile-likes">❤️ {tile.likes}</div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Main;
