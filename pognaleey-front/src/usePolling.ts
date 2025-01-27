import {useEffect, useState} from "react";
import apiClient from "./apiClient";

interface PollingOptions<T> {
    url: string;
    timeout?: number;
    interval?: number;
    validate?: (data: T[]) => void;
    mergeData?: (prevData: T[], newData: T[]) => T[];
    stopCondition?: (data: T[]) => boolean;
    dataPath?: string; // Путь к данным в ответе API
}

// TODO: Replace with WebSockets!!!
// TODO: Add status specific logic (too much methods here)
const usePolling = <T>(
    options: PollingOptions<T>
): { data: T[]; isLoading: boolean; error: string | null } => {
    const [data, setData] = useState<T[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchData = async () => {
        const startTime = Date.now();
        const {url, timeout = 30000, interval = 500, validate, mergeData, stopCondition, dataPath} = options;

        while (Date.now() - startTime < timeout) {
            try {
                const response = await apiClient(url);
                if (!response.ok) {
                    throw new Error(`Failed to fetch data: ${response.status}`);
                }

                const result = await response.json();
                const rawData = dataPath ? result[dataPath] : result;

                if (!Array.isArray(rawData)) {
                    throw new Error(`Expected an array but got: ${typeof rawData}`);
                }

                validate && validate(rawData);

                setData((prevData) =>
                    mergeData ? mergeData(prevData, rawData) : rawData
                );

                if (stopCondition && stopCondition(rawData)) {
                    break;
                }
            } catch (err) {
                console.error("Error during polling:", err);
                setError((err as Error).message);
                break;
            }

            await new Promise((resolve) => setTimeout(resolve, interval));
        }

        setData((prevData) => prevData.filter((item: any) => item.status === "READY"));

        setIsLoading(false);
    };

    useEffect(() => {
        fetchData();
    }, [options.url]);

    return {data, isLoading, error};
};

export default usePolling;
