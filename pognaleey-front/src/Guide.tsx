import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import apiClient from "./apiClient";
import "./Guide.css";
import "./print.css";
import Header from "./Header";
import MainContainer from "./MainContainer";
import {useLikeHandler} from "./useLikeHandler";
import useGuideContent from "./useGuideContent"; // Импортируем наш кастомный хук
import LoginPopup from "./LoginPopup";
import PencilLoader from "./PencilLoader";
import CircleLoader from "./CircleLoader";
import ModalImage from "./ModalImage";
import {ImageDto} from "./ImageDto";

interface UserDto {
    id: number;
    username: string;
}

interface TravelGuideInfoDto {
    id: number;
    title: string;
    image?: ImageDto;
    totalLikes: number;
    isLiked: boolean;
    owner?: UserDto;
    createdAt: number;
}

function formatDate(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toLocaleString("ru-RU", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    }).replace(",", "");
}

const Guide: React.FC = () => {
    const {guideId} = useParams<{ guideId: string }>();
    const [guide, setGuide] = useState<TravelGuideInfoDto | null>(null);
    const [loadingGuide, setLoadingGuide] = useState(true);
    const [errorGuide, setErrorGuide] = useState<string | null>(null);
    const [showLoginPopup, setShowLoginPopup] = useState(false);
    const navigate = useNavigate();

    const {contentItems, isLoading: loadingContent, error: errorContent} = useGuideContent(guideId!); // Используем хук

    const {handleLike} = useLikeHandler(() => setShowLoginPopup(true));

    useEffect(() => {
        const fetchGuide = async () => {
            if (!guideId) return;

            try {
                setLoadingGuide(true);
                const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-guides/${guideId}`);

                if (response.ok) {
                    const data: TravelGuideInfoDto = await response.json();
                    setGuide(data);
                } else {
                    throw new Error("Failed to fetch travel guide.");
                }
            } catch (err) {
                setErrorGuide((err as Error).message);
            } finally {
                setLoadingGuide(false);
            }
        };

        fetchGuide();
    }, [guideId]);

    const handleLikeUpdate = (guideId: number, isLiked: boolean, totalLikes: number) => {
        setGuide((prevGuide) =>
            prevGuide?.id === guideId ? {...prevGuide, isLiked, totalLikes} : prevGuide
        );
    };
    const [selectedImage, setSelectedImage] = useState<ImageDto | null>(null);

    const handleImageClick = (image: ImageDto) => {
        setSelectedImage(image);
    };

    const closeModal = () => {
        setSelectedImage(null);
    };


    if (loadingGuide) {
        return <CircleLoader/>;
    }

    if (errorGuide) {
        return <div className="error">Ошибка: {errorGuide}</div>;
    }

    if (!guide) {
        return <div className="not-found">Гид не найден.</div>;
    }

    const handlePdfDownload = async () => {
        window.print();
    };

    const renderImage = (image: ImageDto) => (
        <div className="image-guide-container">
            <img
                className="image"
                src={image.url}
                alt={image.title}
                style={{maxWidth: "80%", margin: "0 auto", display: "block"}}
                onClick={() =>
                    handleImageClick(image)
                }
            />
            <div className="image-caption">
                {image.ownerName && image.ownerUrl && (
                    <span>
                        Автор: <a href={image.ownerUrl}>{image.ownerName}</a>
                    </span>
                )}
                {image.licenceUrl && (
                    <>
                        {image.ownerName && image.ownerUrl && "; "}
                        <a href={image.licenceUrl}>Лицензия</a>
                    </>
                )}
            </div>
        </div>
    );

    return (
        <MainContainer>
            <Header/>

            <div className="guide-header">
                {!loadingContent && !errorContent && (
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
                    </div>)}
            </div>

            <div className="guide-details">
                {errorContent && <div className="error">Ошибка: {errorContent}</div>}
                {contentItems.map((item) =>
                    <div key={item.id} className="content-item">
                        {item.type === "MARKDOWN" && (
                            <ReactMarkdown rehypePlugins={[rehypeRaw]}>
                                {item.content || ""}
                            </ReactMarkdown>
                        )}
                        {item.type === "IMAGE" && renderImage(JSON.parse(item.content))}
                        {item.status === "IN_PROGRESS" && <PencilLoader/>}

                    </div>
                )}
            </div>
            {
                showLoginPopup && (
                    <LoginPopup onClose={() => setShowLoginPopup(false)} onLogin={() => navigate("/login")}/>
                )
            }

            {/* Модальное окно */}
            <ModalImage image={selectedImage} onClose={closeModal}/>
        </MainContainer>
    )
        ;
};

export default Guide;
