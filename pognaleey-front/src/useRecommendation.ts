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

const useRecommendations = (inquiryId: string | null, timeout: number = 20000) => {
    const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
    const [isLoading, setIsLoading] = useState(true);

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
        const startTime = Date.now();
        const timeout = 20000;

        while (Date.now() - startTime < timeout) {
            try {
                const response = await apiClient(
                    `${process.env.REACT_APP_API_URL}/travel-recommendations?inquiryId=${inquiryId}`
                );

                if (!response.ok) {
                    throw new Error(`Failed to fetch recommendations: ${response.status}`);
                }

                const data: { recommendations: Recommendation[] } = await response.json();

                setRecommendations((prevRecommendations) =>
                    mergeRecommendations(prevRecommendations, data.recommendations)
                );

                if (
                    data.recommendations.length !== 0 &&
                    data.recommendations.every(
                        (rec) => rec.status === "READY" || rec.status === "FAILED"
                    )
                ) {
                    break;
                }
            } catch (error) {
                console.error("Error fetching recommendations:", error);
                break;
            }

            await new Promise((resolve) => setTimeout(resolve, 500));
        }

        setIsLoading(false);
    };

    useEffect(() => {
        if (!inquiryId) {
            console.error("Missing inquiryId in URL");
            return;
        }

        fetchRecommendations(inquiryId);
    }, [inquiryId]);


    return {recommendations, isLoading};
};

export default useRecommendations;
