// i18n.ts
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import enTranslations from './locales/en.json';
import ruTranslations from './locales/ru.json';

i18n
    .use(initReactI18next)  // Подключаем интеграцию с React
    .init({
        resources: {
            en: {translation: enTranslations},
            ru: {translation: ruTranslations},
        },
        lng: 'ru',  // Язык по умолчанию
        fallbackLng: 'ru',  // Язык по умолчанию
        interpolation: {
            escapeValue: false,  // Для работы с JSX
        },
        react: {
            useSuspense: false,  // Отключаем Suspense, если вы не хотите асинхронную загрузку
        },
        debug: true
    });

export default i18n;
