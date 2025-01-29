// i18n.ts
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import enTranslations from './locales/en.json';
import ruTranslations from './locales/ru.json';

const savedLanguage = localStorage.getItem('language') || 'ru';

i18n
    .use(initReactI18next) // Подключаем интеграцию с React
    .init({
        resources: {
            en: {translation: enTranslations},
            ru: {translation: ruTranslations},
        },
        lng: savedLanguage, // Устанавливаем язык при инициализации
        fallbackLng: 'ru', // Язык по умолчанию
        interpolation: {
            escapeValue: false, // Для работы с JSX
        },
        react: {
            useSuspense: false, // Отключаем Suspense
        },
        debug: true, // Включить для отладки
    });

export default i18n;
