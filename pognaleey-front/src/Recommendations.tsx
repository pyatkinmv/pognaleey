import React, {useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import "./Recommendations.css";
import Header from "./Header";
import MainContainer from "./MainContainer";
import useRecommendations from "./useRecommendation";
import apiClient from "./apiClient";

// TODO: Исправить прыгающий размер и элементы
const Recommendations: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const searchParams = new URLSearchParams(location.search);
    const inquiryId = searchParams.get("inquiryId");

    const {recommendations, isLoading, error} = useRecommendations(inquiryId);

    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const handleImageClick = (imageUrl: string) => {
        setSelectedImage(imageUrl);
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

            const guide = await response.json();
            navigate(`/travel-guides/${guide.id}`);
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
                                        handleImageClick(recommendation.image?.imageUrl || "")
                                    }
                                />
                            ) : (
                                <div className="loader"/>
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
                            ) : (
                                <div className="loader"/>
                            )}
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

            {selectedImage && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <img className="modal-image" src={selectedImage} alt="Enlarged"/>
                        <button className="modal-close" onClick={closeModal}>
                            &times;
                        </button>
                    </div>
                </div>
            )}
        </MainContainer>
    );
};

export default Recommendations;
