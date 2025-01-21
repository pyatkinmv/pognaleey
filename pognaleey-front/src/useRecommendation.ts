import {useEffect, useState} from "react";
import apiClient from "./apiClient";

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

const useRecommendations = (inquiryId: string | null, timeout: number = 30000) => {
    const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const mergeRecommendations = (
        prevRecommendations: Recommendation[],
        newRecommendations: Recommendation[]
    ): Recommendation[] => {
        const filtered = newRecommendations.filter((rec) => rec.status !== "FAILED");
        return filtered.map((rec) => {
            const existing = prevRecommendations.find((prevRec) => prevRec.id === rec.id);
            return existing ? {...existing, ...rec} : rec;
        });
    };

    const fetchRecommendations = async (inquiryId: string) => {
        console.log("Fetching recommendations...");
        setIsLoading(true);
        setError(null); // Reset error state
        const startTime = Date.now();

        while (Date.now() - startTime < timeout) {
            try {
                const response = await apiClient(
                    `${process.env.REACT_APP_API_URL}/travel-recommendations?inquiryId=${inquiryId}`
                );

                if (!response.ok) {
                    throw new Error(`Failed to fetch recommendations: ${response.status}`);
                }

                const data: { recommendations: Recommendation[] } = await response.json();

                if (data.recommendations.length === 0) {
                    throw new Error(`Рекомендации не найдены.`);
                }

                if (data.recommendations.every((rec) => rec.status === "FAILED")) {
                    throw new Error(`Failed to fetch recommendations`);
                }

                setRecommendations((prevRecommendations) =>
                    mergeRecommendations(prevRecommendations, data.recommendations)
                );

                if (
                    data.recommendations.every(
                        (rec) => rec.status === "READY" || rec.status === "FAILED"
                    )
                ) {
                    break;
                }
            } catch (error) {
                console.error("Error fetching recommendations:", error);
                setError("Ошибка при загрузке рекомендаций.");
                break;
            }

            await new Promise((resolve) => setTimeout(resolve, 500));
        }

        // Перед завершением загрузки оставляем только READY рекомендации
        setRecommendations((prevRecommendations) =>
            prevRecommendations.filter((rec) => rec.status === "READY")
        );

        setIsLoading(false);
    };

    useEffect(() => {
        if (!inquiryId) {
            console.error("Missing inquiryId in URL");
            setError("Идентификатор запроса отсутствует.");
            setIsLoading(false);
            return;
        }

        fetchRecommendations(inquiryId);
    }, [inquiryId]);

    return {recommendations, isLoading, error};
};

export default useRecommendations;
