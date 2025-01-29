import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import apiClient from "../../services/apiClient";
import "./Guide.css";
import "../../styles/print.css";
import Header from "../../components/Header/Header";
import MainContainer from "../../components/MainContainer/MainContainer";
import {useLikeHandler} from "../../hooks/useLikeHandler";
import useGuideContent from "../../hooks/useGuideContent"; // Импортируем наш кастомный хук
import LoginPopup from "../../components/LoginPopup/LoginPopup";
import PencilLoader from "../../components/loaders/PencilLoader/PencilLoader";
import CircleLoader from "../../components/loaders/CircleLoader/CircleLoader";
import ModalImage from "../../components/ModalImage/ModalImage";
import {ImageDto} from "../../types/ImageDto";
import ImageCaption from "../../components/ImageCaption/ImageCaption";
import {useTranslation} from "react-i18next";

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
    const {t} = useTranslation();

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
        return <div className="error">{t("error")} {errorGuide}</div>;
    }

    if (!guide) {
        return <div className="not-found">{t("guideNotFound")}</div>;
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
            <div className="image-wrapper">
                <ImageCaption
                    aiGenerated={image.aiGenerated}
                    authorName={image.authorName}
                    authorUrl={image.authorUrl}
                    licenceUrl={image.licenceUrl}
                    className={"image-caption"}
                />
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
                            {t("downloadPdf")}
                        </button>
                        {guide.owner && <p className="owner"> {t("owner")} {guide.owner?.username || t("unknown")}</p>}
                        <p className="created-at">{t("createdAt")}: {formatDate(guide.createdAt)}</p>
                    </div>)}
            </div>

            <div className="guide-details">
                {errorContent && <div className="error">{t("error")} {errorContent}</div>}
                {contentItems.map((item) =>
                    <div key={item.id} className="content-item">
                        {item.type === "MARKDOWN" && (
                            <ReactMarkdown rehypePlugins={[rehypeRaw]}>
                                {item.content || ""}
                            </ReactMarkdown>
                        )}
                        {item.type === "IMAGE" && item.content && renderImage(JSON.parse(item.content))}
                        {item.status === "IN_PROGRESS" && <PencilLoader/>}

                    </div>
                )}
            </div>
            <footer className="guide-footer">
                <img
                    src="/assets/images/ai-256x384.png"
                    alt="AI Logo"
                    className="footer-ai-logo"
                />
                <p>{t("aiGeneratedText")}</p>
            </footer>
            {
                showLoginPopup && (
                    <LoginPopup
                        onClose={() => setShowLoginPopup(false)}
                        onLoginSuccess={() => setShowLoginPopup(false)}
                    />
                )
            }
            {/* Модальное окно */}
            <ModalImage image={selectedImage} onClose={closeModal}/>
        </MainContainer>
    )
};

export default Guide;
