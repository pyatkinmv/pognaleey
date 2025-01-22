import usePolling from "./usePolling";

interface ContentItem {
    id: number;
    guideId: number;
    ordinal: number;
    content: string;
    status: "READY" | "IN_PROGRESS" | "FAILED";
}

const useGuideContent = (guideId: string | null, timeout: number = 30000) => {
    const mergeContentItems = (
        prevContentItems: ContentItem[],
        newContentItems: ContentItem[]
    ): ContentItem[] => {
        return newContentItems.map((item) => {
            const existing = prevContentItems.find((prevItem) => prevItem.id === item.id);
            return existing ? {...existing, ...item} : item;
        });
    };

    const {data, isLoading, error} = usePolling<ContentItem>({
        url: `${process.env.REACT_APP_API_URL}/travel-guides/${guideId}/content`,
        timeout,
        processData: (contentItems) =>
            contentItems.filter((item) => item.status !== "FAILED"),
        mergeData: mergeContentItems, // Используем функцию мержинга
        stopCondition: (contentItems) =>
            contentItems.every((item) => item.status === "READY" || item.status === "FAILED"),
        dataPath: "contentItems",
    });

    return {contentItems: data, isLoading, error};
};

export default useGuideContent;
