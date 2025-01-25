import usePolling from "./usePolling";

interface ContentItem {
    id: number;
    guideId: number;
    ordinal: number;
    content: string;
    status: "READY" | "IN_PROGRESS" | "FAILED";
    type: "MARKDOWN" | "IMAGE";
}

const useGuideContent = (guideId: string | null, timeout: number = 30000) => {
    const mergeContentItems = (
        prevContentItems: ContentItem[],
        newContentItems: ContentItem[]
    ): ContentItem[] => {
        const filtered = newContentItems.filter((item) => item.status !== "FAILED");
        return filtered.map((item) => {
            const existing = prevContentItems.find((prevRec) => prevRec.id === item.id);
            return existing ? {...existing, ...item} : item;
        });
    };

    const validateItems = (
        newContentItems: ContentItem[]
    ): void => {
        if (newContentItems.length === 0) {
            throw new Error(`Рекомендации не найдены.`);
        }

        if (newContentItems.every((rec) => rec.status === "FAILED")) {
            throw new Error(`Failed to fetch recommendations`);
        }
    }

    const {data, isLoading, error} = usePolling<ContentItem>({
        url: `${process.env.REACT_APP_API_URL}/travel-guides/${guideId}/content`,
        timeout,
        validate: validateItems,
        mergeData: mergeContentItems, // Используем функцию мержинга
        stopCondition: (contentItems) =>
            contentItems.every((item) => item.status === "READY" || item.status === "FAILED"),
        dataPath: "contentItems",
    });

    return {contentItems: data, isLoading, error};
};

export default useGuideContent;
