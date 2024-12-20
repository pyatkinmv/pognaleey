import React, {useEffect, useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import "./Recommendations.css"; // Подключаем стили

interface QuickRecommendation {
    id: string;
    title: string;
    description: string;
    imageUrl?: string; // Добавляем поле для URL картинки
}

interface DetailedRecommendation extends QuickRecommendation {
    budget: { from: string, to: string };
    reasoning: string;
    creativeDescription: string;
    tips: string;
    whereToGo: string[];
    additionalConsideration: string;
}

const Recommendations: React.FC = () => {
    const location = useLocation();
    const params = useParams<{ inquiryId: string }>();

    // Получаем quickRecommendations из состояния предыдущей страницы
    const {quickRecommendations} = location.state || {quickRecommendations: []};

    // Состояния для рекомендаций и загрузки
    const [recommendations, setRecommendations] = useState<QuickRecommendation[] | DetailedRecommendation[]>(quickRecommendations || []);
    const [isLoading, setIsLoading] = useState<boolean>(true); // Изначально показываем индикатор загрузки

    // Для модального окна
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const handleImageClick = (imageUrl: string) => {
        setSelectedImage(imageUrl); // Открыть модальное окно
    };

    const closeModal = () => {
        setSelectedImage(null); // Закрыть модальное окно
    };

    useEffect(() => {
        // Проверяем, что inquiryId существует
        if (!params.inquiryId) {
            console.error("Missing inquiryId in URL");
            return;
        }

        // Асинхронная функция для загрузки рекомендаций
        const fetchRecommendations = async () => {
            console.log("Fetching detailed recommendations...");
            setIsLoading(true); // Устанавливаем индикатор загрузки

            try {
                const response = await fetch(`${process.env.REACT_APP_API_URL}/api/v1/travel-inquiries/${params.inquiryId}/recommendations`);

                if (!response.ok) {
                    throw new Error(`Failed to fetch recommendations: ${response.status}`);
                }

                const data = await response.json();
                const recommendations: DetailedRecommendation[] = data.recommendations;
                console.log("Detailed recommendations received:", recommendations);

                // Обновляем рекомендации с детализированными данными
                setRecommendations(recommendations);
            } catch (error) {
                console.error("Error fetching detailed recommendations:", error);
            } finally {
                setIsLoading(false); // Выключаем индикатор загрузки
            }
        };

        // Запускаем загрузку рекомендаций
        fetchRecommendations();
    }, [params.inquiryId]);

    return (
        <div className="recommendations-container">
            <h1 className="title">Recommendations</h1>
            <div className="recommendations-list">
                {recommendations.map((recommendation) => (
                    <div className="recommendation-card" key={recommendation.id}>
                        {/* Вращающийся индикатор или изображение */}
                        <div className="recommendation-image-wrapper">
                            {isLoading ? (
                                <div className="loader"/> // Прогресс-бар
                            ) : (
                                <img
                                    className="recommendation-image"
                                    src={recommendation.imageUrl}
                                    alt={recommendation.title}
                                    onClick={() => handleImageClick(recommendation.imageUrl || "/logo512.png")}
                                />
                            )}
                        </div>
                        {/* Контент */}
                        <div className="recommendation-content">
                            <h2 className="recommendation-title">{recommendation.title}</h2>
                            <p className="recommendation-description">{recommendation.description}</p>

                            {/* Если это детализированная рекомендация, покажем дополнительные данные */}
                            {"budget" in recommendation && (
                                <>
                                    <p><strong>Необходимый
                                        бюджет:</strong> {recommendation.budget?.from || "Не указано"} - {recommendation.budget?.to || "Не указано"}
                                    </p>
                                    <p><strong>Reasoning:</strong> {recommendation.reasoning}</p>
                                    <p><strong>Description:</strong> {recommendation.creativeDescription}</p>
                                    <p><strong>Tips:</strong> {recommendation.tips}</p>
                                    <p><strong>Where to Go:</strong></p>
                                    <ul>
                                        {recommendation.whereToGo.map((place, index) => (
                                            <li key={index}>{place}</li>
                                        ))}
                                    </ul>
                                    <p><strong>Additional
                                        Consideration:</strong> {recommendation.additionalConsideration}</p>
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

        </div>
    );
};

export default Recommendations;
