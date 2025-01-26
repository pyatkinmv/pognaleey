import React, {useEffect, useRef, useState} from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";
import Header from "./Header";
import FilterButtons from "./FilterButtons";
import TileGrid from "./TileGrid";
import LoginPopup from "./LoginPopup";
import apiClient from "./apiClient";
import MainContainer from "./MainContainer";
import {useLikeHandler} from "./useLikeHandler";

const Main: React.FC = () => {
    const navigate = useNavigate();
    const [selectedFilter, setSelectedFilter] = useState<string>("feed");
    const [tiles, setTiles] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<number>(0);
    const [hasMore, setHasMore] = useState<boolean>(true);
    const [showLoginPopup, setShowLoginPopup] = useState<boolean>(false);

    const observer = useRef<IntersectionObserver | null>(null);
    const lastTileRef = useRef<HTMLDivElement | null>(null);

    const {handleLike} = useLikeHandler(() => setShowLoginPopup(true));

    const handleFilterChange = (value: string) => {
        if ((value === "liked" || value === "my") && !localStorage.getItem("jwtToken")) {
            setShowLoginPopup(true);
            return;
        }

        setSelectedFilter(value);
        setPage(0);
        setTiles([]);
        setHasMore(true);
        loadTiles(value, true);
    };

    const loadTiles = async (filter: string, reset: boolean = false) => {
        if (isLoading || (!reset && !hasMore)) return;

        setIsLoading(true);
        setError(null);

        const url = `${process.env.REACT_APP_API_URL}/travel-guides/${filter}?page=${reset ? 0 : page}&size=12`;

        try {
            if ((filter === "liked" || filter === "my") && !localStorage.getItem("jwtToken")) {
                setShowLoginPopup(true);
                return;
            }

            const response = await apiClient(url);

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

    const updateTileLikes = (guideId: number, isLiked: boolean, totalLikes: number) => {
        setTiles((prevTiles) =>
            prevTiles.map((tile) =>
                tile.id === guideId ? {...tile, isLiked, totalLikes} : tile
            )
        );
    };

    useEffect(() => {
        loadTiles("feed", true);
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
        <MainContainer>
            <Header/>
            <div className="image-container">
                <img src="/main.webp" alt="Main Banner" className="banner-image"/>
                <div className="banner-text">Каждое путешествие<br/>начинается с идеи!</div>
                <button className="action-button" onClick={() => navigate("/travel-inquiries")}>Погнали!</button>
            </div>

            <FilterButtons selectedFilter={selectedFilter} onFilterChange={handleFilterChange}/>

            <TileGrid
                tiles={tiles}
                onLike={(id, isLiked) => handleLike(id, isLiked, updateTileLikes)}
                lastTileRef={lastTileRef}
                isLoading={isLoading}
                error={error}
            />

            {
                showLoginPopup && (
                    <LoginPopup
                        onClose={() => setShowLoginPopup(false)}
                        onLoginSuccess={() => setShowLoginPopup(false)}
                    />
                )
            }
        </MainContainer>
    );
};

export default Main;
