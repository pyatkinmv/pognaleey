import usePolling from "./usePolling";
import {ImageDto} from "./ImageDto";
import {useTranslation} from "react-i18next";

interface Recommendation {
    id: number;
    title: string;
    status: "READY" | "IN_PROGRESS" | "FAILED";
    details?: {
        description: string;
        reasoning: string;
    };
    image?: ImageDto;
    guideId?: number | null;
}

const useRecommendations = (inquiryId: string | null, timeout: number = 30000) => {
    const {t} = useTranslation();

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

    const validateRecommendations = (
        recommendations: Recommendation[]
    ): void => {
        if (recommendations.length === 0) {
            throw new Error(t("recommendationsNotFound"));
        }

        if (recommendations.every((rec) => rec.status === "FAILED")) {
            throw new Error(`Failed to fetch recommendations`);
        }
    }

    const {data, isLoading, error} = usePolling<Recommendation>({
        url: `${process.env.REACT_APP_API_URL}/travel-recommendations?inquiryId=${inquiryId}`,
        timeout,
        validate: validateRecommendations,
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
