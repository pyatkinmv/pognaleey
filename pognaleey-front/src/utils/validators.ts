export const validateUsername = (username: string, t: (key: string) => string): string | null => {
    if (!username) {
        return t("usernameRequired");
    } else if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
        return t("usernameInvalid");
    } else if (username.length < 3 || username.length > 20) {
        return t("usernameLength");
    }
    return null; // Если ошибок нет
};

export const validatePassword = (password: string, t: (key: string) => string): string | null => {
    if (!password) {
        return t("passwordRequired");
    } else if (password.length < 3 || password.length > 20) {
        return t("passwordLength");
    }
    return null; // Если ошибок нет
};
