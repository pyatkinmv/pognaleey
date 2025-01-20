import React, {useState} from "react";
import {useNavigate} from "react-router-dom"; // Для перехода между страницами
import "./Inquiry.css";
import apiClient from "./apiClient";
import Header from "./Header";
import MainContainer from "./MainContainer";

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
            console.error("Ошибка:", error);
            alert("Не удалось отправить форму.");
        }
    };

    const purposeOptions = [
        {value: "релаксация", label: "Отдых и релаксация", icon: "🧘"},
        {value: "активный отдых", label: "Активный отдых", icon: "🚴"},
        {value: "культура", label: "Культура", icon: "🏛️"},
        {value: "шоппинг", label: "Шоппинг", icon: "🛍️"},
        {value: "оздоровление", label: "Оздоровление", icon: "🌿"}
    ];

    const companionOptions = [
        {value: "один", label: "Один", icon: "🧍"},
        {value: "с партнёром", label: "С партнером", icon: "❤️"},
        {value: "семья", label: "С семьёй", icon: "👨‍👩‍👧‍👦"},
        {value: "друзья", label: "С друзьями", icon: "👫"},
        {value: "группа", label: "В группе", icon: "🚌"},
    ];

    const durationOptions = [
        {value: "1-3 дня", label: "1-3 дня"},
        {value: "4-7 дней", label: "4-7 дней"},
        {value: "8-14 дней", label: "8-14 дней"},
        {value: "Более двух недель", label: "Более двух недель"},
    ]

    const transportOptions = [
        {value: "автомобиль", label: "Автомобиль", icon: "🚗"},
        {value: "самолет", label: "Самолёт", icon: "✈️"},
        {value: "поезд", label: "Поезд", icon: "🚂"},
        {value: "автобус", label: "Автобус", icon: "🚌"},
        {value: "корабль", label: "Корабль", icon: "🛳️"},
    ];

    const budgetOptions = [
        {value: "economy", label: "Хочу сэкономить", icon: "🪙"}, // Economy — Экономичный
        {value: "standard", label: "Стандартный", icon: "💵"}, // Standard — Стандартный
        {value: "comfort", label: "Комфортный", icon: "💳"}, // Comfort — Комфортный
        {value: "luxury", label: "Максимальный комфорт", icon: "💎"} // Luxury — Роскошный
    ];

    const preferencesOptions = [
        {value: "море и пляжи", label: "Море и пляжи", icon: "🏖️"}, // Пляжный отдых
        {value: "спа", label: "СПА", icon: "🛀"}, // СПА и релакс
        {value: "горы", label: "Горы", icon: "🏔️"}, // Активный отдых в горах
        {value: "мегаполис", label: "Мегаполисы", icon: "🏙️"}, // Экскурсии и культурный туризм
        {value: "сельская местность", label: "Сельская местность", icon: "🐓"},
        {value: "кемпинг", label: "Кемпинг", icon: "⛺"}, // СПА и релакс
        {value: "история и культура", label: "История и культура", icon: "🎭"}, // Исторический туризм
        {value: "лыжи", label: "Горнолыжка", icon: "🎿"}, // Лыжи, снег
        {value: "сафари", label: "Сафари", icon: "🦁"}, // Экзотическая природа, животные
        {value: "круиз", label: "Круизы", icon: "🛳️"}, // Морские путешествия
        {value: "еда", label: "Кулинария", icon: "🍔"}, // Еда и дегустации
        {value: "алкоголь", label: "Алкоголь", icon: "🍷"}, // Еда и дегустации
        {value: "фестивали", label: "Фестивали", icon: "🎉"}, // Еда и дегустации
        {value: "ночная жизнь", label: "Ночная жизнь", icon: "🌃"}, // Еда и дегустации
        {value: "экзотика", label: "Экзотика", icon: "🐪"}, // Еда и дегустации\
        {value: "маленькие города", label: "Маленькие города", icon: "🏡"}
    ];

    const seasonOptions = [
        {value: "зима", label: "Зима", icon: "❄️"}, // Холодное время года, горнолыжный отдых, рождественские ярмарки
        {value: "весна", label: "Весна", icon: "🌸"}, // Цветение, мягкая погода, романтичные поездки
        {value: "лето", label: "Лето", icon: "☀️"}, // Жаркая погода, пляжи, каникулы
        {value: "осень", label: "Осень", icon: "🍂"},
    ]

    const regionOptions = [
        {value: "россия", label: "Россия", icon: "🪆"}, // Путешествия внутри страны
        {value: "европа", label: "Европа", icon: "🗼"}, // Европейские страны
        {value: "азия", label: "Азия", icon: "🐉"}, // Восточная культура
        {value: "африка", label: "Африка", icon: "🌴"}, // Африканские страны
        {value: "южная и северная америка", label: "Америка", icon: "🌵"}, // Северная и Южная Америка
        {value: "австралия и океания", label: "Австралия и Океания", icon: "🌊"}, // Австралия и острова
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
            <div className="form-heading">
                Ответьте на вопросы, чтобы мы могли подобрать идеальное путешествие!
            </div>

            {/* Цель поездки */}
            <QuestionContainer label="Какова цель вашего путешествия?">
                <div className="card-grid">
                    {purposeOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.purpose.includes(option.value) ? "selected" : ""}`}
                            onClick={() => handleCardMultiSelect("purpose", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>


            {/* Вопрос о компаньонах */}
            {/* Цель поездки */}
            <QuestionContainer label="С кем вы путешествуете?">
                <div className="card-grid">
                    {companionOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.companions === option.value ? "selected" : ""}`}
                            onClick={() => handleCardSingleSelect("companions", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            <QuestionContainer label="Какой вид транспорта вы предпочитаете?">
                <div className="card-grid">
                    {transportOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.transport.includes(option.value) ? "selected" : ""}`}
                            onClick={() => handleCardMultiSelect("transport", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Бюджет */}
            <QuestionContainer label="Каков ваш бюджет на путешествие?">
                <div className="card-grid">
                    {budgetOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.budget === option.value ? "selected" : ""}`}
                            onClick={() => handleCardSingleSelect("budget", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Куда */}
            <QuestionContainer label="Куда вы хотите поехать?">
                <div className="card-grid">
                    {regionOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.locationTo === option.value ? "selected" : ""} card-narrow`}
                            onClick={() => handleCardSingleSelect("locationTo", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Вопрос о предпочтениях */}
            <QuestionContainer label="Какие направления и условия отдыха вас интересуют?">
                <div className="card-grid">
                    {preferencesOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.preferences.includes(option.value) ? "selected" : ""} card-small`}
                            onClick={() => handleCardMultiSelect("preferences", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Сезон */}
            <QuestionContainer label="Когда вы планируете путешествовать?">
                <div className="card-grid">
                    {seasonOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.season === option.value ? "selected" : ""}`}
                            onClick={() => handleCardSingleSelect("season", option.value)}
                        >
                            <div className="card-icon">{option.icon}</div>
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Вопрос о продолжительности */}
            <QuestionContainer label="Какова продолжительность вашей поездки?">
                <div className="card-grid">
                    {durationOptions.map((option) => (
                        <div
                            key={option.value}
                            className={`card ${formData.duration === option.value ? "selected" : ""} card-short`}
                            onClick={() => handleCardSingleSelect("duration", option.value)}
                        >
                            <div className="card-label">{option.label}</div>
                        </div>
                    ))}
                </div>
            </QuestionContainer>

            {/* Вопрос: Откуда вы начнете путешествие? */}
            <QuestionContainer label="Откуда вы начнете путешествие?">
                <input
                    type="text"
                    name="locationFrom"
                    value={formData.locationFrom}
                    onChange={handleChange}
                    maxLength={25} // Ограничение на количество символов
                    placeholder="Введите ваш город"
                    className="text-input"
                />
            </QuestionContainer>

            {/* Дополнительные предпочтения */}
            <QuestionContainer label="Есть ли у вас дополнительные предпочтения?">
                <input
                    type="text"
                    name="additionalPreferences"
                    value={formData.additionalPreferences}
                    onChange={handleChange}
                    maxLength={50} // Ограничение на количество символов
                    placeholder="Напишите любые дополнительные детали"
                    className="text-input"
                />
            </QuestionContainer>

            {/* Кнопка отправки */}
            <button className="button" type="submit" onClick={handleSubmit}>
                Отправить
            </button>
        </MainContainer>
    );
};

interface QuestionContainerProps {
    label: string;
    children: React.ReactNode;
}

const QuestionContainer: React.FC<QuestionContainerProps> = ({label, children}) => (
    <div className="question-container">
        <label>{label}</label>
        {children}
    </div>
);

export default Inquiry;
