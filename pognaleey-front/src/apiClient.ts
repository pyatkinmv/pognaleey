import i18next from "i18next";

const apiClient = async (url: string, options: RequestInit = {}) => {
    const token = localStorage.getItem("jwtToken"); // Получаем токен из локального хранилища

    const headers = {
        ...(options.headers || {}),
        Authorization: token ? `Bearer ${token}` : "", // Добавляем токен в заголовок
        "Content-Type": "application/json",
        "Accept-Language": i18next.language, // Добавляем язык из i18next
    };

    const response = await fetch(url, {...options, headers});
    if (response.status === 401) {
        // Если токен недействителен, перенаправляем на логин
        localStorage.removeItem("jwtToken");
        window.location.href = "/login";
    }

    return response;
};

export default apiClient;
