import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";

// Типы данных для рекомендаций
interface Recommendation {
    id: string;
    title: string;
    description: string;
    budget?: { from: string, to: string };
    reasoning?: string;
    creativeDescription?: string;
    tips?: string;
    whereToGo?: string[];
    additionalConsideration?: string;
    imageUrl?: string;
}

// Основной компонент
const Recommendation: React.FC = () => {
    // Достаем inquiryId из параметров маршрута
    const {inquiryId} = useParams<{ inquiryId: string }>();

    // Состояние для списка рекомендаций
    const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Функция загрузки данных с сервера
    const fetchRecommendations = async () => {
        try {
            setLoading(true);
            const response = await fetch(
                `${process.env.REACT_APP_API_URL}/api/v1/travel-inquiries/${inquiryId}/recommendations`
            );

            if (!response.ok) {
                throw new Error("Failed to fetch recommendations");
            }

            const data = await response.json();
            setRecommendations(data.recommendations); // Предполагаем, что данные придут в поле recommendations
        } catch (err: any) {
            setError(err.message || "An unexpected error occurred");
        } finally {
            setLoading(false);
        }
    };

    // Загружаем данные при монтировании компонента
    useEffect(() => {
        fetchRecommendations();
    }, [inquiryId]);

    // Обработка ошибок и состояния загрузки
    if (loading) {
        return <div>Загрузка рекомендаций...</div>;
    }

    if (error) {
        return <div>Ошибка: {error}</div>;
    }

    return (
        <div style={styles.page}>
            {recommendations.map((recommendation) => (
                <section key={recommendation.id} style={styles.optionContainer}>
                    {/* Картинка */}
                    {recommendation.imageUrl && (
                        <img
                            src={recommendation.imageUrl}
                            alt={`Изображение ${recommendation.title}`}
                            style={styles.optionImage}
                        />
                    )}

                    {/* Детали о месте */}
                    <div style={styles.optionDetails}>
                        <h2 style={styles.title}>{recommendation.title}</h2>
                        <p>
                            <strong>Необходимый
                                бюджет:</strong> {recommendation.budget?.from || "Не указано"} - {recommendation.budget?.to || "Не указано"}
                        </p>
                        <p>
                            <strong>Почему подходит:</strong> {recommendation.reasoning || "Нет информации"}
                        </p>
                        <p>
                            <strong>Описание:</strong> {recommendation.creativeDescription || "Нет описания"}
                        </p>
                        <p>
                            <strong>Учтите:</strong> {recommendation.tips || "Нет рекомендаций"}
                        </p>

                        {/* Список достопримечательностей */}
                        {recommendation.whereToGo && recommendation.whereToGo.length > 0 && (
                            <div>
                                <strong>Рекомендуемые места для посещения:</strong>
                                <ul>
                                    {recommendation.whereToGo.map((place, index) => (
                                        <li key={index}>{place}</li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        <p>
                            <strong>Дополнительные замечания:</strong>{" "}
                            {recommendation.additionalConsideration || "Нет информации"}
                        </p>
                    </div>
                </section>
            ))}
        </div>
    );
};

// Стили
const styles: { [key: string]: React.CSSProperties } = {
    page: {
        backgroundColor: "#E8DFC2", // Цвет страницы
        padding: "20px",
    },
    optionContainer: {
        display: "flex",
        alignItems: "flex-start",
        backgroundColor: "#ffffff",
        borderRadius: "10px",
        boxShadow: "0 4px 10px rgba(0, 0, 0, 0.1)",
        margin: "20px",
        padding: "20px",
        overflow: "hidden",
    },
    optionImage: {
        maxWidth: "200px",
        maxHeight: "200px",
        borderRadius: "10px",
        objectFit: "cover",
        marginRight: "20px",
        boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
    },
    optionDetails: {
        flex: 1,
        color: "#333", // Цвет текста
    },
    title: {
        color: "#EB6B3B", // Оранжевый цвет заголовков
        fontSize: "1.5em",
    },
    button: {
        backgroundColor: "#79C8CB", // Голубой цвет кнопки
        color: "white",
        border: "none",
        padding: "10px 20px",
        borderRadius: "5px",
        cursor: "pointer",
        fontSize: "16px",
        marginTop: "10px",
    },
};

export default Recommendation;
