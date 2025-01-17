import React, {useEffect, useRef, useState} from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";
import apiClient from "./apiClient"; // Импортируем API клиент

const Main: React.FC = () => {
    const navigate = useNavigate();
    const [selectedFilter, setSelectedFilter] = useState<string>("feed");
    const [tiles, setTiles] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<number>(0);
    const [hasMore, setHasMore] = useState<boolean>(true);
    const [showLoginPopup, setShowLoginPopup] = useState<boolean>(false); // Видимость попапа
    const [user, setUser] = useState<{ username: string | null }>({username: null}); // Информация о пользователе

    const observer = useRef<IntersectionObserver | null>(null);
    const lastTileRef = useRef<HTMLDivElement | null>(null);

    // Загрузка информации о пользователе
    useEffect(() => {
        const token = localStorage.getItem("jwtToken");
        if (token) {
            // Замените это на реальный запрос к API для получения информации о пользователе
            const username = token ? getUsernameFromToken(token) : null;
            setUser({username: username});
        }
    }, []);

    const getUsernameFromToken = (token: string) => {
        try {
            const payloadBase64 = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payloadBase64));
            return decodedPayload.name || decodedPayload.sub || null;
        } catch (error) {
            console.error(error);
            return null;
        }
    };

    const [isDropdownVisible, setIsDropdownVisible] = useState<boolean>(false); // Состояние для показа меню


    const handleLogout = () => {
        localStorage.removeItem("jwtToken");
        setUser({username: null});
        navigate("/");
    };

    const handleButtonClick = () => {
        navigate(`/travel-inquiries`);
    };

    const loadTiles = async (filter: string = selectedFilter, reset: boolean = false) => {
        if (isLoading || (!reset && !hasMore)) return;

        setIsLoading(true);
        setError(null);

        const url = `${process.env.REACT_APP_API_URL}/travel-guides/${filter}?page=${reset ? 0 : page}&size=8`;

        try {
            let response;

            if (filter === "liked" || filter === "my") {
                const token = localStorage.getItem("jwtToken");
                if (!token) {
                    setShowLoginPopup(true); // Показываем попап
                    return;
                }
            }

            response = await apiClient(url);

            if (!response.ok) {
                throw new Error("Ошибка загрузки данных");
            }

            const data = await response.json();
            setTiles((prevTiles) => (reset ? data.content : [...prevTiles, ...data.content]));
            setPage((prevPage) => (reset ? 1 : prevPage + 1));
            setHasMore(!data.last);
        } catch (err: any) {
            setError(err.message || "Неизвестная ошибка");
        } finally {
            setIsLoading(false);
        }
    };

    const handleFilterChange = (value: string) => {
        if ((value === "liked" || value === "my") && !localStorage.getItem("jwtToken")) {
            setShowLoginPopup(true); // Показываем попап, если пользователь не авторизован
            return;
        }

        setSelectedFilter(value);
        setPage(0);
        setTiles([]);
        setHasMore(true);
        loadTiles(value, true).catch((err) => {
            console.error("Ошибка загрузки плиток:", err);
        });
    };

    const handleLike = async (id: number, isCurrentlyLiked: boolean) => {
        try {
            const token = localStorage.getItem("jwtToken");
            if (!token) {
                setShowLoginPopup(true); // Показываем попап
                return;
            }

            const endpoint = isCurrentlyLiked
                ? `${process.env.REACT_APP_API_URL}/travel-guides/${id}/unlike`
                : `${process.env.REACT_APP_API_URL}/travel-guides/${id}/like`;

            const response = await apiClient(endpoint, {
                method: isCurrentlyLiked ? "DELETE" : "PUT",
            });

            if (!response.ok) {
                throw new Error("Ошибка при обработке лайка");
            }

            const {guideId, isLiked, totalLikes} = await response.json();

            // Обновляем состояние плиток
            setTiles((prevTiles) =>
                prevTiles.map((tile) =>
                    tile.id === guideId
                        ? {...tile, isLiked, totalLikes} // Обновляем лайк и количество лайков
                        : tile
                )
            );
        } catch (error) {
            console.error("Ошибка обработки лайка:", error);
        }
    };


    useEffect(() => {
        loadTiles("feed", true).catch((err) => {
            console.error("Ошибка загрузки плиток:", err);
        }); // Загружаем начальные данные при монтировании
    }, []);

    useEffect(() => {
        if (observer.current) observer.current.disconnect();

        observer.current = new IntersectionObserver(
            (entries) => {
                if (entries[0].isIntersecting && hasMore && !isLoading) {
                    loadTiles(selectedFilter, false);
                }
            },
            {threshold: 1.0}
        );

        if (lastTileRef.current) {
            observer.current.observe(lastTileRef.current);
        }

        return () => observer.current?.disconnect();
    }, [lastTileRef, hasMore, isLoading]);

    return (
        <div className="main-container">
            <div className="content-container">
                <header className="header">
                    <img src="/logo-circle192.png" alt="Логотип" className="logo"/>
                    <nav className="navbar">
                        <a href="/" className="nav-link">Главная</a>
                        <a href="/contacts" className="nav-link">Контакты</a>
                        <a href="/language" className="nav-link">🌐 Язык</a>
                        <div
                            className="user-menu"
                            onMouseEnter={() => setIsDropdownVisible(true)}
                            onMouseLeave={() => setIsDropdownVisible(false)}
                        >
                            {user.username ? (
                                <>
                                    <span className="user-name">✨ {user.username}</span>
                                    {isDropdownVisible && (
                                        <div className="dropdown-menu">
                                            <button onClick={handleLogout}>Выйти</button>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <a href="/login" className="nav-link">🔒 Войти</a>
                            )}
                        </div>
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
                    {error && <div className="error">{error}</div>} {/* Сообщение об ошибке */}
                    {tiles.map((tile, index) => (
                        <div
                            className="tile"
                            key={tile.id}
                            ref={index === tiles.length - 1 ? lastTileRef : null} /* Отслеживаем последний элемент */
                            onClick={() => navigate(`/travel-guides/${tile.id}`)} // Добавляем переход
                        >
                            <div className="tile-image-wrapper">
                                <img src={tile.imageUrl} alt={tile.title} className="tile-image"/>
                            </div>
                            <div className="tile-title">{tile.title}</div>
                            <div className="tile-likes">
                                <span
                                    className={`like-button ${tile.isLiked ? "liked" : ""}`}
                                    onClick={(e) => {
                                        e.stopPropagation(); // Предотвращаем клик по плитке
                                        handleLike(tile.id, tile.isLiked);
                                    }}
                                >
                                    ❤
                                </span>
                                {tile.totalLikes}
                            </div>
                        </div>

                    ))}
                    {isLoading &&
                        <div className="loading">Загрузка...</div>}
                </div>

                {showLoginPopup && (
                    <div className="popup-overlay" onClick={() => setShowLoginPopup(false)}>
                        <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                            <h3>Вы не авторизованы</h3>
                            <p>Чтобы воспользоваться этой функцией, войдите в аккаунт.</p>
                            <button onClick={() => navigate("/login")} className="login-button-popup">
                                Войти
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Main;
