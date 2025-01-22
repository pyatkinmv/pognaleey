import usePolling from "./usePolling";

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

    const {data, isLoading, error} = usePolling<Recommendation>({
        url: `${process.env.REACT_APP_API_URL}/travel-recommendations?inquiryId=${inquiryId}`,
        timeout,
        processData: (recommendations) =>
            recommendations.filter((rec) => rec.status !== "FAILED"),
        mergeData: mergeRecommendations, // Используем функцию мержинга
        stopCondition: (recommendations) =>
            recommendations.every(
                (rec) => rec.status === "READY" || rec.status === "FAILED"
            ),
        dataPath: "recommendations",
    });

    return {recommendations: data, isLoading, error};
};

export default useRecommendations;
