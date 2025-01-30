import React, {useState} from "react";
import {useNavigate} from "react-router-dom"; // Для перехода между страницами
import styles from "./Inquiry.module.css";
import apiClient from "../../services/apiClient";
import Header from "../../components/Header/Header";
import MainContainer from "../../components/MainContainer/MainContainer";
import {useTranslation} from "react-i18next";


const Inquiry: React.FC = () => {
    const [formData, setFormData] = useState({
        purpose: [] as string[],
        preferences: [] as string[],
        budget: "",
        duration: "",
        transport: [] as string[],
        season: "",
        locationTo: "",
        locationFrom: "", // Добавляем поле "откуда"
        companions: "",
        additionalPreferences: "",
    });

    const navigate = useNavigate(); // Для редиректа

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
    ) => {
        const {name, value, type} = e.target;

        if (type === "select-multiple") {
            const options = Array.from(
                (e.target as HTMLSelectElement).selectedOptions,
                (option) => option.value
            );
            setFormData({...formData, [name]: options});
        } else {
            setFormData({...formData, [name]: value});
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await apiClient(`${process.env.REACT_APP_API_URL}/travel-inquiries`, {
                method: "POST",
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                const data = await response.json();
                navigate(`/travel-recommendations?inquiryId=${data.id}`);
            } else {
                alert("Ошибка отправки формы.");
            }
        } catch (error) {
            console.error(t("error"), error);
            alert("Не удалось отправить форму.");
        }
    };

    const {t} = useTranslation();

    const purposeOptions = [
        {value: t("inquiry.relaxation"), label: t("inquiry.relaxation"), icon: "🧘"},
        {value: t("inquiry.activeRest"), label: t("inquiry.activeRest"), icon: "🚴"},
        {value: t("inquiry.culture"), label: t("inquiry.culture"), icon: "🏛️"},
        {value: t("inquiry.shopping"), label: t("inquiry.shopping"), icon: "🛍️"},
        {value: t("inquiry.wellness"), label: t("inquiry.wellness"), icon: "🌿"}
    ];

    const companionOptions = [
        {value: t("inquiry.alone"), label: t("inquiry.alone"), icon: "🧍"},
        {value: t("inquiry.withPartner"), label: t("inquiry.withPartner"), icon: "❤️"},
        {value: t("inquiry.family"), label: t("inquiry.family"), icon: "👨‍👩‍👧‍👦"},
        {value: t("inquiry.friends"), label: t("inquiry.friends"), icon: "👫"},
        {value: t("inquiry.group"), label: t("inquiry.group"), icon: "🚌"}
    ];

    const durationOptions = [
        {value: t("inquiry.1-3Days"), label: t("inquiry.1-3Days")},
        {value: t("inquiry.4-7Days"), label: t("inquiry.4-7Days")},
        {value: t("inquiry.8-14Days"), label: t("inquiry.8-14Days")},
        {value: t("inquiry.moreThanTwoWeeks"), label: t("inquiry.moreThanTwoWeeks")}
    ];

    const transportOptions = [
        {value: t("inquiry.car"), label: t("inquiry.car"), icon: "🚗"},
        {value: t("inquiry.plane"), label: t("inquiry.plane"), icon: "✈️"},
        {value: t("inquiry.train"), label: t("inquiry.train"), icon: "🚂"},
        {value: t("inquiry.bus"), label: t("inquiry.bus"), icon: "🚌"},
        {value: t("inquiry.ship"), label: t("inquiry.ship"), icon: "🛳️"}
    ];

    const budgetOptions = [
        {value: t("inquiry.economy"), label: t("inquiry.economy"), icon: "🪙"},
        {value: t("inquiry.standard"), label: t("inquiry.standard"), icon: "💵"},
        {value: t("inquiry.comfort"), label: t("inquiry.comfort"), icon: "💳"},
        {value: t("inquiry.luxury"), label: t("inquiry.luxury"), icon: "💎"}
    ];

    const preferencesOptions = [
        {value: t("inquiry.seaAndBeaches"), label: t("inquiry.seaAndBeaches"), icon: "🏖️"},
        {value: t("inquiry.spa"), label: t("inquiry.spa"), icon: "🛀"},
        {value: t("inquiry.mountains"), label: t("inquiry.mountains"), icon: "🏔️"},
        {value: t("inquiry.megapolis"), label: t("inquiry.megapolis"), icon: "🏙️"},
        {value: t("inquiry.countryside"), label: t("inquiry.countryside"), icon: "🐓"},
        {value: t("inquiry.camping"), label: t("inquiry.camping"), icon: "⛺"},
        {value: t("inquiry.historyAndCulture"), label: t("inquiry.historyAndCulture"), icon: "🎭"},
        {value: t("inquiry.skiing"), label: t("inquiry.skiing"), icon: "🎿"},
        {value: t("inquiry.safari"), label: t("inquiry.safari"), icon: "🦁"},
        {value: t("inquiry.cruise"), label: t("inquiry.cruise"), icon: "🛳️"},
        {value: t("inquiry.food"), label: t("inquiry.food"), icon: "🍔"},
        {value: t("inquiry.alcohol"), label: t("inquiry.alcohol"), icon: "🍷"},
        {value: t("inquiry.festivals"), label: t("inquiry.festivals"), icon: "🎉"},
        {value: t("inquiry.nightLife"), label: t("inquiry.nightLife"), icon: "🌃"},
        {value: t("inquiry.exotic"), label: t("inquiry.exotic"), icon: "🐪"},
        {value: t("inquiry.smallTowns"), label: t("inquiry.smallTowns"), icon: "🏡"},
        {value: t("inquiry.walking"), label: t("inquiry.walking"), icon: "🚶‍♂️"},
        {value: t("inquiry.adventureSports"), label: t("inquiry.adventureSports"), icon: "🪂"}
    ];

    const seasonOptions = [
        {value: t("inquiry.winter"), label: t("inquiry.winter"), icon: "❄️"},
        {value: t("inquiry.spring"), label: t("inquiry.spring"), icon: "🌸"},
        {value: t("inquiry.summer"), label: t("inquiry.summer"), icon: "☀️"},
        {value: t("inquiry.autumn"), label: t("inquiry.autumn"), icon: "🍂"}
    ];

    const regionOptions = [
        {value: t("inquiry.europe"), label: t("inquiry.europe"), icon: "🗼"},
        {value: t("inquiry.southeastAsia"), label: t("inquiry.southeastAsia"), icon: "🌴"}, // Юго-Восточная Азия
        {value: t("inquiry.eastAsia"), label: t("inquiry.eastAsia"), icon: "🐉"}, // Восточная Азия
        {value: t("inquiry.africa"), label: t("inquiry.africa"), icon: "🌍"}, // Африка
        {value: t("inquiry.northAmerica"), label: t("inquiry.northAmerica"), icon: "🗽"}, // Северная Америка
        {value: t("inquiry.caribbean"), label: t("inquiry.caribbean"), icon: "🏝️"}, // Карибский бассейн
        {value: t("inquiry.southAmerica"), label: t("inquiry.southAmerica"), icon: "🌵"}, // Южная Америка
        {value: t("inquiry.russia"), label: t("inquiry.russia"), icon: "🪆"}, // Россия
        {value: t("inquiry.australiaOceania"), label: t("inquiry.australiaOceania"), icon: "🦘"} // Австралия и Океания
    ];

    const handleCardMultiSelect = (field: keyof typeof formData, value: string) => {
        setFormData((prevData) => {
            const fieldData = prevData[field]; // Получаем данные для указанного поля

            // Убедимся, что данные поля — это массив
            if (Array.isArray(fieldData)) {
                return {
                    ...prevData,
                    [field]: fieldData.includes(value)
                        ? fieldData.filter((v) => v !== value) // Удаление значения
                        : [...fieldData, value], // Добавление значения
                };
            }

            console.error(`Field "${field}" is not an array.`);
            return prevData; // Возвращаем без изменений, если поле — не массив
        });
    };

    const handleCardSingleSelect = (field: keyof typeof formData, value: string) => {
        setFormData((prevData) => ({
            ...prevData,
            [field]: prevData[field] === value ? "" : value, // Снимаем выбор, если нажали на уже выбранное
        }));
    };


    return (
        <MainContainer>
            <Header/>
            {/* Заголовок формы */}
            <div className={styles.formHeading}>
                {t("answerQuestions")}
            </div>

            {/* Цель поездки */}
            <QuestionContainer label={t("purposeOfTravel")}>
                <div className={styles.cardGrid}>
                    {purposeOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.purpose.includes(option.value) ? styles.cardSelected : ""}`}
                            onClick={() => handleCardMultiSelect("purpose", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Вопрос о компаньонах */}
            <QuestionContainer label={t("travelCompanions")}>
                <div className={styles.cardGrid}>
                    {companionOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.companions === option.value ? styles.cardSelected : ""}`}
                            onClick={() => handleCardSingleSelect("companions", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Предпочтительный транспорт */}
            <QuestionContainer label={t("preferredTransport")}>
                <div className={styles.cardGrid}>
                    {transportOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.transport.includes(option.value) ? styles.cardSelected : ""}`}
                            onClick={() => handleCardMultiSelect("transport", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Бюджет */}
            <QuestionContainer label={t("budget")}>
                <div className={styles.cardGrid}>
                    {budgetOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.budget === option.value ? styles.cardSelected : ""}`}
                            onClick={() => handleCardSingleSelect("budget", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Куда */}
            <QuestionContainer label={t("destination")}>
                <div className={styles.cardGrid}>
                    {regionOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.locationTo === option.value ? styles.cardSelected : ""} ${styles.cardSmall}`}
                            onClick={() => handleCardSingleSelect("locationTo", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Предпочтения */}
            <QuestionContainer label={t("preferences")}>
                <div className={styles.cardGrid}>
                    {preferencesOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.preferences.includes(option.value) ? styles.cardSelected : ""} ${styles.cardSmall}`}
                            onClick={() => handleCardMultiSelect("preferences", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Сезон */}
            <QuestionContainer label={t("travelSeason")}>
                <div className={styles.cardGrid}>
                    {seasonOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.season === option.value ? styles.cardSelected : ""}`}
                            onClick={() => handleCardSingleSelect("season", option.value)}
                        >
                            <div className={styles.cardIcon}>{option.icon}</div>
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Продолжительность */}
            <QuestionContainer label={t("travelDuration")}>
                <div className={styles.cardGrid}>
                    {durationOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.card} ${formData.duration === option.value ? styles.cardSelected : ""}`}
                            onClick={() => handleCardSingleSelect("duration", option.value)}
                        >
                            <div className={styles.cardLabel}>{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Откуда начнется путешествие */}
            <QuestionContainer label={t("departureLocation")}>
                <input
                    type="text"
                    name="locationFrom"
                    value={formData.locationFrom}
                    onChange={handleChange}
                    maxLength={35} // Ограничение на количество символов
                    placeholder={t("enterYourCity")}
                    className={styles.textInput}
                />
            </QuestionContainer>

            {/* Дополнительные предпочтения */}
            <QuestionContainer label={t("additionalPreferences")}>
                <input
                    type="text"
                    name="additionalPreferences"
                    value={formData.additionalPreferences}
                    onChange={handleChange}
                    maxLength={10} // Ограничение на количество символов
                    placeholder={t("writeAdditionalDetails")}
                    className={styles.textInput}
                />
            </QuestionContainer>

            {/* Кнопка отправки */}
            <button className={styles.button} type="submit" onClick={handleSubmit}>
                {t("submit")}
            </button>
        </MainContainer>
    );
};

interface QuestionContainerProps {
    label: string;
    children: React.ReactNode;
}

const QuestionContainer: React.FC<QuestionContainerProps> = ({label, children}) => (
    <div className={styles.questionContainer}>
        <label>{label}</label>
        {children}
    </div>
);

export default Inquiry;
