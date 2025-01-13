// validators.ts

export const validateUsername = (username: string): string | null => {
    if (!username) {
        return "Имя пользователя обязательно.";
    } else if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
        return "Имя пользователя может содержать только буквы, цифры, точки, дефисы и подчёркивания.";
    } else if (username.length < 3 || username.length > 20) {
        return "Имя пользователя должно быть от 3 до 20 символов.";
    }
    return null; // Если ошибок нет
};

export const validatePassword = (password: string): string | null => {
    if (!password) {
        return "Пароль обязателен.";
    } else if (password.length < 3 || password.length > 20) {
        return "Пароль должен быть от 3 до 20 символов.";
    }
    return null; // Если ошибок нет
};

export const validateConfirmPassword = (
    password: string,
    confirmPassword: string
): string | null => {
    if (password !== confirmPassword) {
        return "Пароли не совпадают.";
    }
    return null; // Если ошибок нет
};
