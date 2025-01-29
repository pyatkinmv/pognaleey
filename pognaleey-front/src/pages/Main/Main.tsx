import React, {useEffect, useRef, useState} from "react";
import "./Main.css";
import {useNavigate} from "react-router-dom";
import Header from "../../components/Header/Header";
import FilterButtons from "../../components/FilterButtons/FilterButtons";
import TileGrid from "../../components/TileGrid/TileGrid";
import LoginPopup from "../../components/LoginPopup/LoginPopup";
import apiClient from "../../services/apiClient";
import MainContainer from "../../components/MainContainer/MainContainer";
import {useLikeHandler} from "../../hooks/useLikeHandler";
import {useTranslation} from "react-i18next";

const Main: React.FC = () => {
    const navigate = useNavigate();
    const [selectedFilter, setSelectedFilter] = useState<string>("feed");
    const [tiles, setTiles] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<number>(0);
    const [hasMore, setHasMore] = useState<boolean>(true);
    const [showLoginPopup, setShowLoginPopup] = useState<boolean>(false);
    const {t} = useTranslation();

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
                throw new Error(t("loadingError"));
            }

            const data = await response.json();
            setTiles((prevTiles) => (reset ? data.content : [...prevTiles, ...data.content]));
            setPage((prevPage) => (reset ? 1 : prevPage + 1));
            setHasMore(!data.last);
        } catch (err: any) {
            setError(err.message || t("unknownError"));
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
            <Header onLanguageChange={() => loadTiles(selectedFilter, true)}/>
            <div className="image-container">
                <img src="/assets/images/main.webp" alt="Main Banner" className="banner-image"/>
                <div className="banner-text">{t('bannerText1')}<br/>{t('bannerText2')}</div>
                <button className="action-button" onClick={() => navigate("/travel-inquiries")}>{t("goButton")}</button>
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
