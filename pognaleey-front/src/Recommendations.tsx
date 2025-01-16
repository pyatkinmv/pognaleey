import React, {useEffect, useState} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import "./Recommendations.css";
import apiClient from "./apiClient"; // Подключаем стили

interface QuickRecommendation {
    id: number;
    title: string;
    description: string;
    imageUrl?: string; // Добавляем поле для URL картинки
}

interface DetailedRecommendation extends QuickRecommendation {
    budget: string;
    reasoning: string;
    creativeDescription: string;
    tips: string;
    whereToGo: string[];
    additionalConsideration: string;
    guideId?: number
}

const Recommendations: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
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

    const handleGenerateGuide = async (recommendationId: number) => {
        try {
            const response = await apiClient(
                `${process.env.REACT_APP_API_URL}/travel-guides?recommendationId=${recommendationId}`,
                {
                    method: "POST",
                }
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
                const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-inquiries/${params.inquiryId}/recommendations`);

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
            {/* Заголовок с логотипом */}
            <div className="header">
                <img
                    className="logo"
                    src="/logo192.png" // Путь к логотипу
                    alt="Логотип"
                />
                <h1 className="recommendations-title">Погнали!</h1>
            </div>
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
                                    <p><strong>Необходимый бюджет:</strong> {recommendation.budget || "Не указано"}</p>
                                    <p><strong>Почему подходит:</strong> {recommendation.reasoning}</p>
                                    <p><strong>Описание:</strong> {recommendation.creativeDescription}</p>
                                    <p><strong>Советы:</strong> {recommendation.tips}</p>
                                    <p><strong>Места для посещения:</strong></p>
                                    <ul>
                                        {recommendation.whereToGo.map((place, index) => (
                                            <li key={index}>{place}</li>
                                        ))}
                                    </ul>
                                    <p><strong>Дополнительно:</strong> {recommendation.additionalConsideration}</p>
                                    <button
                                        className="generate-guide-button"
                                        onClick={() => {
                                            if ("guideId" in recommendation && recommendation.guideId) {
                                                // Если guideId существует, перенаправляем на гайд
                                                navigate(`/travel-guides/${recommendation.guideId}`);
                                            } else {
                                                // Если guideId отсутствует, генерируем новый гайд
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

        </div>
    );
};

export default Recommendations;
