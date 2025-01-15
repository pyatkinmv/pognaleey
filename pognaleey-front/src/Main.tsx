import React, {useState} from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";

const Main: React.FC = () => {
    const navigate = useNavigate();

    const [selectedFilter, setSelectedFilter] = useState<string>("best"); // "Недавнее" выбрано по умолчанию

    const handleButtonClick = () => {
        navigate(`/travel-inquiries`);
    };

    const handleFilterChange = (value: string) => {
        setSelectedFilter(value);
        console.log("Выбран фильтр:", value); // Обработчик фильтрации (можно добавить логику)
    };

    // Пример данных для плитки
    const tiles = [
        {
            title: "Посетите Эйфелеву башню и наслаждайтесь французской кухней",
            imageUrl: "https://avatars.dzeninfra.ru/get-zen_doc/5298771/pub_63f50f153bf62367f23b0695_63f51059cdd48e720ff93c7b/scale_1200", // Замените на путь к вашей картинке
            text: "Посетите Эйфелеву башню и наслаждайтесь французской кухней.",
            likes: 120,
        },
        {
            title: "Погрузитесь в атмосферу сибирской природы",
            imageUrl: "https://baldezh.top/uploads/posts/2022-08/1660780372_36-funart-pro-p-vodorosli-baikala-priroda-krasivo-foto-39.jpg",
            text: "Погрузитесь в атмосферу сибирской природы.",
            likes: 95,
        },
        {
            title: "Райские пляжи и бирюзовые воды",
            imageUrl: "https://cff2.earth.com/uploads/2021/12/17122657/Extreme-weather-scaled.jpg",
            text: "Райские пляжи и бирюзовые воды.",
            likes: 200,
        },
        {
            title: "Каналы, музеи и уютные улочки",
            imageUrl: "http://img.goodfon.ru/original/2560x1600/0/7f/vesna-reka-tsvetenie-buildings-canal-netherlands-amsterdam-b.jpg",
            text: "Каналы, музеи и уютные улочки.",
            likes: 180,
        },
        {
            title: "Культура и природа Киото",
            imageUrl: "https://i.ytimg.com/vi/TWaNuGeXv4E/maxresdefault.jpg",
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
                {/* Радиокнопки */}
                <div className="radio-buttons-container">

                    <label className={`radio-button ${selectedFilter === "best" ? "active" : ""}`}>
                        <input
                            type="radio"
                            name="filter"
                            value="best"
                            checked={selectedFilter === "best"}
                            onChange={() => handleFilterChange("best")}
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

                    <label className={`radio-button ${selectedFilter === "mine" ? "active" : ""}`}>
                        <input
                            type="radio"
                            name="filter"
                            value="mine"
                            checked={selectedFilter === "mine"}
                            onChange={() => handleFilterChange("mine")}
                        />
                        Моё
                    </label>
                </div>
                {/* Плитка */}
                <div className="tile-container">
                    {tiles.map((tile, index) => (
                        <div className="tile" key={index}>
                            <div className="tile-image-wrapper">
                                <img src={tile.imageUrl} alt={tile.title} className="tile-image"/>
                            </div>
                            <div className="tile-title">{tile.title}</div>
                            <div className="tile-likes">❤️ {tile.likes}</div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Main;
