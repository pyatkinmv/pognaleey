import React, {useEffect, useRef, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import "./Recommendations.css";
import apiClient from "./apiClient";
import Header from "./Header";
import MainContainer from "./MainContainer";

interface Recommendation {
    id: number;
    title: string;
    status: "READY" | "IN_PROGRESS" | "FAILED";
    details?: {
        description: string;
        reasoning: string;
    };
    image?: {
        thumbnailUrl: string;
        imageUrl: string;
    };
    guideId?: number | null;
}

const Recommendations: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // Извлекаем query-параметры
    const searchParams = new URLSearchParams(location.search);
    const inquiryId = searchParams.get("inquiryId");
    const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedImage, setSelectedImage] = useState<string | null>(null);
    const [timeoutExceeded, setTimeoutExceeded] = useState(false); // Используем useState для таймера
    const timeoutExceededRef = useRef(false); // Используем useRef для отслеживания состояния

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
        console.log("Effect triggered", inquiryId);
    }, [inquiryId]);

    useEffect(() => {
        if (!inquiryId) {
            console.error("Missing inquiryId in URL");
            return;
        }

        const fetchRecommendations = async () => {
            console.log("Fetching recommendations...");

            setIsLoading(true);

            const startTime = Date.now(); // Время старта
            const timeout = 15000; // 20 секунд

            while (Date.now() - startTime < timeout) {
                try {
                    const response = await apiClient(
                        `${process.env.REACT_APP_API_URL}/travel-recommendations?inquiryId=${inquiryId}`
                    );

                    if (!response.ok) {
                        throw new Error(`Failed to fetch recommendations: ${response.status}`);
                    }

                    const data: { recommendations: Recommendation[] } = await response.json();

                    setRecommendations((prevRecommendations) => {
                        const updated = data.recommendations.filter(
                            (rec) => rec.status !== "FAILED"
                        );

                        return updated.map((rec) => {
                            const existing = prevRecommendations.find((prevRec) => prevRec.id === rec.id);
                            return existing ? {...existing, ...rec} : rec;
                        });
                    });

                    // Если все рекомендации в READY или FAILED, выходим из цикла
                    if (
                        data.recommendations.length !== 0 && data.recommendations.every(
                            (rec) => rec.status === "READY" || rec.status === "FAILED"
                        )
                    ) {
                        break;
                    }
                } catch (error) {
                    console.error("Error fetching recommendations:", error);
                    alert("Ошибка при загрузке рекомендаций.");
                    break;
                }

                // Ожидание 500 мс перед следующей итерацией
                await new Promise((resolve) => setTimeout(resolve, 500));
            }

            setIsLoading(false);
        };

        fetchRecommendations();
    }, [inquiryId]);


    return (
        <MainContainer>
            <Header/>
            {isLoading && <div className="loading-indicator">Загрузка рекомендаций...</div>}
            <div className="recommendations-list">
                {recommendations.map((recommendation) => (
                    <div className="recommendation-card" key={recommendation.id}>
                        <div className="recommendation-image-wrapper">
                            {recommendation.image ? (
                                <img
                                    className="recommendation-image"
                                    src={recommendation.image.thumbnailUrl}
                                    alt={recommendation.title}
                                    onClick={() => handleImageClick(recommendation.image?.imageUrl || "")}
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
                                        <strong>Почему подходит:</strong> {recommendation.details.reasoning}
                                    </p>
                                    <p>
                                        <strong>Описание:</strong> {recommendation.details.description}
                                    </p>
                                </>
                            ) : (
                                <div className="loader"/>
                            )}
                            <button
                                className="generate-guide-button"
                                onClick={() =>
                                    recommendation.guideId
                                        ? navigate(`/travel-guides/${recommendation.guideId}`)
                                        : handleGenerateGuide(recommendation.id)
                                }
                            >
                                {recommendation.guideId ? "Перейти на гайд" : "Сгенерировать гайд"}
                            </button>
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
