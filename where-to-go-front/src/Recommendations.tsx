import React from "react";
import {useLocation} from "react-router-dom";

// Типы данных
interface QuickRecommendation {
    id: string;
    title: string;
    description: string;
}

interface RecommendationProps {
    quickRecommendations: QuickRecommendation[];
}

const Recommendations: React.FC = () => {
    // Получение состояния через useLocation
    const location = useLocation();
    const {quickRecommendations} = location.state as { quickRecommendations: QuickRecommendation[] };
    return (
        <div style={{backgroundColor: "#E8DFC2", padding: "20px", minHeight: "100vh"}}>
            <h1 style={{color: "#EB6B3B", textAlign: "center"}}>Recommendations</h1>

            {/* Список рекомендаций */}
            <div>
                {quickRecommendations.map((recommendation) => (
                    <div
                        key={recommendation.id}
                        style={{
                            margin: "20px auto",
                            padding: "20px",
                            border: "1px solid #79C8CB",
                            borderRadius: "10px",
                            backgroundColor: "#fff",
                            boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
                            maxWidth: "600px",
                        }}
                    >
                        <h2 style={{color: "#79C8CB"}}>{recommendation.title}</h2>
                        <p style={{color: "#333"}}>{recommendation.description}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Recommendations;
