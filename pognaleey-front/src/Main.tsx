// Main.tsx
import React, {useEffect, useState} from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";
import apiClient from "./apiClient";

const Main: React.FC = () => {
    const navigate = useNavigate();

    const [selectedFilter, setSelectedFilter] = useState<string>("best");
    const [tiles, setTiles] = useState<any[]>([]); // Данные для плиток
    const [isLoading, setIsLoading] = useState<boolean>(false); // Индикатор загрузки
    const [error, setError] = useState<string | null>(null); // Сообщение об ошибке

    const handleButtonClick = () => {
        navigate(`/travel-inquiries`);
    };

    const handleFilterChange = async (value: string) => {
        setSelectedFilter(value);
        setIsLoading(true); // Показываем прелоадер
        setError(null); // Сбрасываем ошибки

        try {
            const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-guides/${value}?page=0&size=20`);

            if (!response.ok) {
                throw new Error("Ошибка загрузки данных");
            }

            const data = await response.json();
            setTiles(data.content); // Обновляем плитки
        } catch (err: any) {
            setError(err.message || "Неизвестная ошибка");
        } finally {
            setIsLoading(false); // Скрываем прелоадер
        }
    };

    useEffect(() => {
        handleFilterChange("feed"); // Загружаем данные по умолчанию ("Лучшее")
    }, []);

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

                {/* Радиокнопки */}
                <div className="radio-buttons-container">
                    <label className={`radio-button ${selectedFilter === "feed" ? "active" : ""}`}>
                        <input
                            type="radio"
                            name="filter"
                            value="feed"
                            checked={selectedFilter === "feed"}
                            onChange={() => handleFilterChange("feed")}
                        />
                        Лучшее
                    </label>
                    <label className={`radio-button ${selectedFilter === "liked" ? "active" : ""}`}>
                        <input
                            type="radio"
                            name="filter"
                            value="liked"
                            checked={selectedFilter === "liked"}
                            onChange={() => handleFilterChange("liked")}
                        />
                        Понравилось
                    </label>
                    <label className={`radio-button ${selectedFilter === "my" ? "active" : ""}`}>
                        <input
                            type="radio"
                            name="filter"
                            value="my"
                            checked={selectedFilter === "my"}
                            onChange={() => handleFilterChange("my")}
                        />
                        Моё
                    </label>
                </div>

                {/* Плитка */}
                <div className="tile-container">
                    {isLoading && <div className="loading">Загрузка...</div>} {/* Прелоадер */}
                    {error && <div className="error">{error}</div>} {/* Сообщение об ошибке */}
                    {!isLoading && !error && tiles.map((tile, index) => (
                        <div className="tile" key={index}>
                            <div className="tile-image-wrapper">
                                <img src={tile.imageUrl} alt={tile.title} className="tile-image"/>
                            </div>
                            <div className="tile-title">{tile.title}</div>
                            <div className="tile-likes">❤️ {tile.totalLikes}</div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Main;
