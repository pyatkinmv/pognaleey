import apiClient from "../services/apiClient";
import {useTranslation} from "react-i18next";

interface LikeResponse {
    guideId: number;
    isLiked: boolean;
    totalLikes: number;
}

export const useLikeHandler = (showLoginPopup: () => void) => {
    const {t} = useTranslation();

    const handleLike = async (
        id: number,
        isCurrentlyLiked: boolean,
        onUpdate: (id: number, isLiked: boolean, totalLikes: number) => void
    ) => {
        try {
            const token = localStorage.getItem("jwtToken");
            if (!token) {
                showLoginPopup();
                return;
            }

            const endpoint = isCurrentlyLiked
                ? `${process.env.REACT_APP_API_URL}/travel-guides/${id}/unlike`
                : `${process.env.REACT_APP_API_URL}/travel-guides/${id}/like`;

            const response = await apiClient(endpoint, {
                method: isCurrentlyLiked ? "DELETE" : "PUT",
            });

            if (!response.ok) {
                throw new Error(t("likeError"));
            }

            const {guideId, isLiked, totalLikes} = await response.json();
            onUpdate(guideId, isLiked, totalLikes);
        } catch (error) {
            console.error("Ошибка обработки лайка:", error);
        }
    };

    return {handleLike};
};
