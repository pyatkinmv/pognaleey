import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ReactMarkdown from "react-markdown";
import apiClient from "./apiClient";
import "./Guide.css";

interface UserDto {
    id: number;
    username: string;
}

interface TravelGuideFullDto {
    id: number;
    title: string;
    imageUrl: string;
    details: string;
    totalLikes: number;
    owner?: UserDto;
}

const Guide: React.FC = () => {
    const {guideId} = useParams<{ guideId: string }>();
    const [guide, setGuide] = useState<TravelGuideFullDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchGuide = async () => {
            try {
                const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-guides/${guideId}`);

                if (response.ok) {
                    const data: TravelGuideFullDto = await response.json();
                    setGuide(data);
                } else {
                    throw new Error("Failed to fetch travel guide.");
                }
            } catch (err) {
                setError((err as Error).message);
            } finally {
                setLoading(false);
            }
        };

        fetchGuide();
    }, [guideId]);

    if (loading) {
        return <div className="loading">Загрузка...</div>;
    }

    if (error) {
        return <div className="error">Ошибка: {error}</div>;
    }

    if (!guide) {
        return <div className="not-found">Гид не найден.</div>;
    }

    return (
        <div className="guide-container">
            <div className="guide-header">
                <h1>{guide.title}</h1>
                {guide.imageUrl && (
                    <img
                        src={guide.imageUrl}
                        alt={`Изображение для гида: ${guide.title}`}
                        className="guide-image"
                    />
                )}
            </div>

            <div className="guide-details">
                <ReactMarkdown>{guide.details}</ReactMarkdown>
            </div>

            <div className="guide-footer">
                <p>Лайков: {guide.totalLikes}</p>
                {guide.owner && <p>Автор: {guide.owner.username}</p>}
            </div>
        </div>
    );
};

export default Guide;
