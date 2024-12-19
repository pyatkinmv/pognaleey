// src/App.tsx
import React, {useState} from "react";

const App: React.FC = () => {
    // Состояние формы
    const [formData, setFormData] = useState({
        purpose: [] as string[],
        nature: [] as string[],
        weather: [] as string[],
        season: "",
        budget: 1000, // Слайдер: начальное значение бюджета
        duration: "",
        interests: [] as string[],
        from: "",
        to: "",
        companions: "",
        preferences: "",
    });

    // Обработчик изменения значений формы
    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
    ) => {
        const {name, value, type} = e.target;

        if (type === "checkbox") {
            const isChecked = (e.target as HTMLInputElement).checked; // Приведение типов для доступа к checked
            setFormData((prevData) => {
                const selectedValues = prevData[name as keyof typeof formData] as string[];
                return {
                    ...prevData,
                    [name]: isChecked
                        ? [...selectedValues, value] // Добавляем значение, если флажок установлен
                        : selectedValues.filter((v) => v !== value), // Удаляем значение, если флажок снят
                };
            });
        } else {
            setFormData({
                ...formData,
                [name]: type === "range" ? +value : value, // Для ползунка преобразуем строку в число
            });
        }
    };


    // Отправка данных формы
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await fetch("/api/v1/travel-inquiries", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                alert("Form submitted successfully!");
            } else {
                alert("Error submitting form.");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Failed to submit form.");
        }
    };

    return (
        <div>
            <h1>Travel Picker Survey</h1>
            <form onSubmit={handleSubmit}>
                <div className="form-heading">
                    Let us know your preferences to find the perfect travel destination!
                </div>

                {/* Чекбоксы для цели поездки */}
                <QuestionContainer label="What is the purpose of your trip? (Select all that apply)">
                    {[
                        "Relaxation and leisure",
                        "Active vacation (hiking, surfing, etc.)",
                        "Exploring culture and landmarks",
                        "Shopping and entertainment",
                        "Family trip",
                        "Other",
                    ].map((option) => (
                        <div key={option}>
                            <label>
                                <input
                                    type="checkbox"
                                    name="purpose"
                                    value={option}
                                    checked={formData.purpose.includes(option)}
                                    onChange={handleChange}
                                />
                                {option}
                            </label>
                        </div>
                    ))}
                </QuestionContainer>

                {/* Чекбоксы для природы */}
                <QuestionContainer label="What type of nature or atmosphere do you prefer? (Select all that apply)">
                    {[
                        "Beaches",
                        "Mountains",
                        "Forests and nature",
                        "City life",
                        "Lakes and rivers",
                        "Unique and unusual places",
                    ].map((option) => (
                        <div key={option}>
                            <label>
                                <input
                                    type="checkbox"
                                    name="nature"
                                    value={option}
                                    checked={formData.nature.includes(option)}
                                    onChange={handleChange}
                                />
                                {option}
                            </label>
                        </div>
                    ))}
                </QuestionContainer>

                {/* Чекбоксы для погоды */}
                <QuestionContainer label="What kind of weather do you enjoy? (Select all that apply)">
                    {[
                        "Warm and sunny",
                        "Cool climate",
                        "Cold and snowy",
                        "Moderate weather",
                        "No preference",
                    ].map((option) => (
                        <div key={option}>
                            <label>
                                <input
                                    type="checkbox"
                                    name="weather"
                                    value={option}
                                    checked={formData.weather.includes(option)}
                                    onChange={handleChange}
                                />
                                {option}
                            </label>
                        </div>
                    ))}
                </QuestionContainer>

                {/* Поле для бюджета (ползунок) */}
                <QuestionContainer label={`What is your approximate budget per person? ($${formData.budget})`}>
                    <input
                        type="range"
                        name="budget"
                        min="100"
                        max="5000"
                        step="100"
                        value={formData.budget}
                        onChange={handleChange}
                    />
                </QuestionContainer>

                {/* Остальные вопросы */}
                <QuestionContainer label="When do you plan to travel? (Time of year)">
                    <select name="season" id="season" value={formData.season} onChange={handleChange}>
                        <option value="Winter">Winter</option>
                        <option value="Spring">Spring</option>
                        <option value="Summer">Summer</option>
                        <option value="Fall">Fall</option>
                        <option value="Flexible">Flexible</option>
                    </select>
                </QuestionContainer>

                <QuestionContainer label="Where are you starting your journey from?">
                    <input
                        type="text"
                        id="from"
                        name="from"
                        placeholder="Enter your starting location"
                        value={formData.from}
                        onChange={handleChange}
                    />
                </QuestionContainer>

                <QuestionContainer label="Where do you want to go?">
                    <input
                        type="text"
                        id="to"
                        name="to"
                        placeholder="Enter your desired destination"
                        value={formData.to}
                        onChange={handleChange}
                    />
                </QuestionContainer>

                <QuestionContainer label="Who are you traveling with?">
                    <select
                        name="companions"
                        id="companions"
                        value={formData.companions}
                        onChange={handleChange}
                    >
                        <option value="Traveling solo">Solo</option>
                        <option value="Traveling with a partner">With a partner</option>
                        <option value="Traveling with family">With family</option>
                        <option value="Traveling with friends">With friends</option>
                    </select>
                </QuestionContainer>

                <QuestionContainer label="Do you have any additional preferences?">
          <textarea
              id="preferences"
              name="preferences"
              rows={4}
              placeholder="Write any additional details here"
              value={formData.preferences}
              onChange={handleChange}
          />
                </QuestionContainer>

                {/* Кнопка отправки */}
                <button type="submit">Submit</button>
            </form>
        </div>
    );
};

// Вспомогательный компонент для каждого вопроса
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

export default App;
