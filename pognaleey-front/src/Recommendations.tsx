import React, {useEffect, useState} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import "./Recommendations.css";
import apiClient from "./apiClient";
import Header from "./Header";
import MainContainer from "./MainContainer";

interface QuickRecommendation {
    id: number;
    title: string;
}

interface DetailedRecommendation {
    id: number;
    title: string;
    reasoning: string;
    description: string;
    imageUrl?: string;
    guideId?: number;
}

const Recommendations: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const params = useParams<{ inquiryId: string }>();

    const {quickRecommendations} = location.state || {quickRecommendations: []};

    const [recommendations, setRecommendations] = useState<(QuickRecommendation | DetailedRecommendation)[]>(quickRecommendations || []);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [detailedLoading, setDetailedLoading] = useState<boolean>(false);

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

    useEffect(() => {
        if (!params.inquiryId) {
            console.error("Missing inquiryId in URL");
            return;
        }

        const fetchDetailedRecommendations = async () => {
            setDetailedLoading(true);

            try {
                const response = await apiClient(
                    `${process.env.REACT_APP_API_URL}/travel-inquiries/${params.inquiryId}/recommendations`
                );

                if (!response.ok) {
                    throw new Error(`Failed to fetch recommendations: ${response.status}`);
                }

                const data = await response.json();
                const detailedRecommendations: DetailedRecommendation[] = data.recommendations;

                setRecommendations((prevRecommendations) =>
                    prevRecommendations.map((rec) =>
                        detailedRecommendations.find((detRec) => detRec.id === rec.id) || rec
                    )
                );
            } catch (error) {
                console.error("Error fetching detailed recommendations:", error);
            } finally {
                setDetailedLoading(false);
            }
        };

        fetchDetailedRecommendations();
    }, [params.inquiryId]);

    return (
        <MainContainer>
            <Header/>
            <div className="recommendations-list">
                {recommendations.map((recommendation) => (
                    <div className="recommendation-card" key={recommendation.id}>
                        <div className="recommendation-image-wrapper">
                            {"imageUrl" in recommendation && recommendation.imageUrl ? (
                                <img
                                    className="recommendation-image"
                                    src={recommendation.imageUrl}
                                    alt={recommendation.title}
                                    onClick={() => handleImageClick(recommendation.imageUrl || "/logo-circle512.png")}
                                />
                            ) : (
                                <div className="loader"/>
                            )}
                        </div>
                        <div className="recommendation-content">
                            <h2 className="recommendation-title">{recommendation.title}</h2>

                            {"reasoning" in recommendation && (
                                <>
                                    <p><strong>Почему подходит:</strong> {recommendation.reasoning}</p>
                                    <p><strong>Описание:</strong> {recommendation.description}</p>
                                    <button
                                        className="generate-guide-button"
                                        onClick={() => {
                                            if ("guideId" in recommendation && recommendation.guideId) {
                                                navigate(`/travel-guides/${recommendation.guideId}`);
                                            } else {
                                                handleGenerateGuide(recommendation.id);
                                            }
                                        }}
                                    >
                                        {recommendation.guideId ? "Перейти на гайд" : "Сгенерировать гайд"}
                                    </button>
                                </>
                            )}
                        </div>
                    </div>
                ))}
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
            </div>
        </MainContainer>
    );
};

export default Recommendations;
