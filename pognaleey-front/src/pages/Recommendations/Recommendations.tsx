import React, {useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import styles from "./Recommendations.module.css"; // Импортируем локальные стили
import Header from "../../components/Header/Header";
import MainContainer from "../../components/MainContainer/MainContainer";
import useRecommendations from "../../hooks/useRecommendation";
import apiClient from "../../services/apiClient";
import PencilLoader from "../../components/loaders/PencilLoader/PencilLoader";
import CircleLoader from "../../components/loaders/CircleLoader/CircleLoader";
import ModalImage from "../../components/ModalImage/ModalImage";
import {ImageDto} from "../../types/ImageDto";
import {useTranslation} from "react-i18next";

const Recommendations: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const {t} = useTranslation();
    const searchParams = new URLSearchParams(location.search);
    const inquiryId = searchParams.get("inquiryId");
    const {recommendations, isLoading, error} = useRecommendations(inquiryId);
    const [selectedImage, setSelectedImage] = useState<ImageDto | null>(null);

    const handleImageClick = (image: ImageDto) => {
        setSelectedImage(image);
    };

    const closeModal = () => {
        setSelectedImage(null);
    };

    const handleGenerateGuide = async (recommendationId: number) => {
        try {
            const response = await apiClient(
                `${process.env.REACT_APP_API_URL}/travel-guides?recommendationId=${recommendationId}`,
                {method: "POST"}
            );
            if (!response.ok) {
                throw new Error(`Failed to generate guide: ${response.status}`);
            }
            // Получаем данные о гайде (без details)
            const guide = await response.json();
            // Передаем данные в navigate через state
            navigate(`/travel-guides/${guide.id}`, {state: {guideInfo: guide}});
        } catch (error) {
            console.error("Error generating guide:", error);
            alert("Не удалось сгенерировать гайд.");
        }
    };

    return (
        <MainContainer>
            <Header/>
            {!isLoading && error && (
                <div className={styles.errorMessage}>{error}</div>
            )}
            <div className={styles.recommendationsList}>
                {recommendations.map((recommendation) => (
                    <div className={styles.recommendationCard} key={recommendation.id}>
                        <div className={styles.recommendationImageWrapper}>
                            {recommendation.image ? (
                                <img
                                    className={styles.recommendationImage}
                                    src={recommendation.image.thumbnailUrl}
                                    alt={recommendation.title}
                                    onClick={() => handleImageClick(recommendation.image!)}
                                />
                            ) : recommendation.status === "IN_PROGRESS" ? (
                                <CircleLoader/>
                            ) : (
                                <img
                                    className={styles.recommendationImage}
                                    src="/assets/images/not-found512.png"
                                    alt="Not Found"
                                />
                            )}
                        </div>
                        <div className={styles.recommendationContent}>
                            <h2 className={styles.recommendationTitle}>
                                {recommendation.title}
                            </h2>
                            {recommendation.details ? (
                                <>
                                    <p>
                                        <strong>{t("whyItFits")}</strong>{" "}
                                        {recommendation.details.reasoning}
                                    </p>
                                    <p>
                                        <strong>{t("description")}</strong>{" "}
                                        {recommendation.details.description}
                                    </p>
                                </>
                            ) : (
                                <PencilLoader/>
                            )}
                            {recommendation.status === "READY" && (
                                <button
                                    className={styles.generateGuideButton}
                                    onClick={() =>
                                        recommendation.guideId
                                            ? navigate(`/travel-guides/${recommendation.guideId}`)
                                            : handleGenerateGuide(recommendation.id)
                                    }
                                >
                                    {recommendation.guideId
                                        ? t("goToGuide")
                                        : t("generateGuide")}
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
            {/* Модальное окно */}
            <ModalImage image={selectedImage} onClose={closeModal}/>
        </MainContainer>
    );
};

export default Recommendations;