import React, {useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import "./Recommendations.css";
import Header from "./Header";
import MainContainer from "./MainContainer";
import useRecommendations from "./useRecommendation";
import apiClient from "./apiClient";
import PencilLoader from "./PencilLoader";
import ModalImage from "./ModalImage";
import {ImageDto} from "./ImageDto";

// TODO: Исправить прыгающий размер и элементы
const Recommendations: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

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
                <div className="error-message">{error}</div>
            )}
            <div className="recommendations-list">
                {recommendations.map((recommendation) => (
                    <div className="recommendation-card" key={recommendation.id}>
                        <div className="recommendation-image-wrapper">
                            {recommendation.image ? (
                                <img
                                    className="recommendation-image"
                                    src={recommendation.image.thumbnailUrl}
                                    alt={recommendation.title}
                                    onClick={() =>
                                        handleImageClick(recommendation.image!)
                                    }
                                />
                            ) : recommendation.status === "IN_PROGRESS" ? (
                                <div className="circle-loader"/>
                            ) : (
                                <img
                                    className="recommendation-image"
                                    src="/not-found512.png"
                                    alt="Not Found"
                                />
                            )}
                        </div>

                        <div className="recommendation-content">
                            <h2 className="recommendation-title">{recommendation.title}</h2>
                            {recommendation.details ? (
                                <>
                                    <p>
                                        <strong>Почему подходит:</strong>{" "}
                                        {recommendation.details.reasoning}
                                    </p>
                                    <p>
                                        <strong>Описание:</strong>{" "}
                                        {recommendation.details.description}
                                    </p>
                                </>
                            ) : <PencilLoader/>}
                            {recommendation.status === "READY" && (<button
                                className="generate-guide-button"
                                onClick={() =>
                                    recommendation.guideId
                                        ? navigate(`/travel-guides/${recommendation.guideId}`)
                                        : handleGenerateGuide(recommendation.id)
                                }
                            >
                                {recommendation.guideId
                                    ? "Перейти на гайд"
                                    : "Сгенерировать гайд"}
                            </button>)}
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
