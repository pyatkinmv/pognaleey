import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import apiClient from "./apiClient";
import "./Guide.css";
import "./print.css"
import Header from "./Header";
import MainContainer from "./MainContainer";
import {useLikeHandler} from "./useLikeHandler";
import LoginPopup from "./LoginPopup";

interface UserDto {
    id: number;
    username: string;
}

interface TravelGuideInfoDto {
    id: number;
    title: string;
    imageUrl: string;
    totalLikes: number;
    isLiked: boolean;
    owner?: UserDto;
    createdAt: number;
}

interface TravelGuideContentDto {
    contentItems: TravelGuideContentItemDto[];
}

interface TravelGuideContentItemDto {
    id: number;
    guideId: number;
    ordinal: number;
    content?: string;
    status: string;
}

function formatDate(timestamp: number): string {
    const date = new Date(timestamp); // Создаем объект Date из миллисекунд
    return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    }).replace(',', ''); // Убираем запятую между датой и временем
}

const Guide: React.FC = () => {
    const {guideId} = useParams<{ guideId: string }>();
    const [guide, setGuide] = useState<TravelGuideInfoDto | null>(null);
    const [contentItems, setContentItems] = useState<TravelGuideContentItemDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showLoginPopup, setShowLoginPopup] = useState(false);
    const navigate = useNavigate();

    const {handleLike} = useLikeHandler(() => setShowLoginPopup(true));

    useEffect(() => {
        const fetchGuide = async () => {
            if (!guideId) return;

            try {
                setLoading(true);
                const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-guides/${guideId}`);

                if (response.ok) {
                    const data: TravelGuideInfoDto = await response.json();
                    setGuide(data);
                } else {
                    throw new Error("Failed to fetch travel guide.");
                }
            } catch (err) {
                setError((err as Error).message);
            } finally {
                setLoading(false);
            }
        };

        fetchGuide();
    }, [guideId]);

    useEffect(() => {
        if (!guideId) return;

        const fetchContent = async () => {
            try {
                setLoading(true);
                const response = await apiClient(
                    `${process.env.REACT_APP_API_URL}/travel-guides/${guideId}/content`
                );

                if (!response.ok) {
                    throw new Error("Failed to fetch guide content.");
                }

                const data: TravelGuideContentDto = await response.json();
                setContentItems(data.contentItems);
            } catch (err) {
                setError((err as Error).message);
            } finally {
                setLoading(false);
            }
        };

        // Загружаем контент только если еще не был передан
        if (!contentItems.length) {
            fetchContent();
        }
    }, [guideId, contentItems.length]);


    const handleLikeUpdate = (guideId: number, isLiked: boolean, totalLikes: number) => {
        setGuide((prevGuide) =>
            prevGuide?.id === guideId
                ? {...prevGuide, isLiked, totalLikes}
                : prevGuide
        );
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loader"></div>
            </div>
        );
    }

    if (error) {
        return <div className="error">Ошибка: {error}</div>;
    }

    if (!guide) {
        return <div className="not-found">Гид не найден.</div>;
    }

    const handlePdfDownload = async () => {
        window.print();
    };

    return (
        <MainContainer>
            <Header/>
            <div className="guide-header">
                <div className="guide-actions">
                    <div className="tile-likes">
                    <span
                        className={`like-button ${guide.isLiked ? "liked" : ""}`}
                        onClick={() => handleLike(guide.id, guide.isLiked, handleLikeUpdate)}
                    >
                        ❤
                    </span>
                        {guide.totalLikes}
                    </div>
                    <button className="download-pdf-button" onClick={() => handlePdfDownload()}>
                        Скачать PDF 💾
                    </button>
                    <p className="owner">Владелец: {guide.owner?.username || "Неизвестно"}</p>
                    <p className="created-at">Дата создания: {formatDate(guide.createdAt)}</p>
                </div>
            </div>
            <div className="guide-details">
                {contentItems.length > 0 ? (
                    contentItems.map((item) => (
                        <div key={item.id} className="content-item">
                            <ReactMarkdown rehypePlugins={[rehypeRaw]}>
                                {item.content}
                            </ReactMarkdown>
                            {item.status === "IN_PROGRESS" && <div className="loader"/>}
                        </div>
                    ))
                ) : (
                    <div className="loader"/>
                )}
            </div>
            {showLoginPopup && (
                <LoginPopup onClose={() => setShowLoginPopup(false)} onLogin={() => navigate("/login")}/>
            )}
        </MainContainer>
    );

};

export default Guide;
