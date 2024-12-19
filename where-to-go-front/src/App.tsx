// src/App.tsx
import React, {useState} from "react";

const App: React.FC = () => {
    // Состояние формы
    const [formData, setFormData] = useState({
        purpose: [] as string[],
        nature: [] as string[],
        weather: [] as string[],
        season: "",
        budget: "",
        duration: "",
        interests: [] as string[],
        from: "",
        to: "",
        companions: "",
        preferences: "",
    });

    // Обработчик изменения значений формы
    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
    ) => {
        const {name, value} = e.target;

        // Обработка множественного выбора (например, select с multiple)
        if (e.target instanceof HTMLSelectElement && e.target.multiple) {
            const selectedValues = Array.from(
                e.target.options as HTMLCollectionOf<HTMLOptionElement>
            )
                .filter((option) => option.selected)
                .map((option) => option.value);

            setFormData({...formData, [name]: selectedValues});
        } else {
            setFormData({...formData, [name]: value});
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

                {/* Вопросы формы */}
                <QuestionContainer label="What is the purpose of your trip? (Select all that apply)">
                    <select
                        name="purpose"
                        id="purpose"
                        multiple
                        value={formData.purpose}
                        onChange={handleChange}
                    >
                        <option value="Relaxation and leisure">Relaxation and leisure</option>
                        <option value="Active vacation (hiking, surfing, etc.)">
                            Active vacation (hiking, surfing, etc.)
                        </option>
                        <option value="Exploring culture and landmarks">
                            Exploring culture and landmarks
                        </option>
                        <option value="Shopping and entertainment">Shopping and entertainment</option>
                        <option value="Family trip">Family trip</option>
                        <option value="Other">Other</option>
                    </select>
                </QuestionContainer>

                <QuestionContainer label="What type of nature or atmosphere do you prefer? (Select all that apply)">
                    <select
                        name="nature"
                        id="nature"
                        multiple
                        value={formData.nature}
                        onChange={handleChange}
                    >
                        <option value="Beaches">Beaches</option>
                        <option value="Mountains">Mountains</option>
                        <option value="Forests and nature">Forests and nature</option>
                        <option value="City life">City life</option>
                        <option value="Lakes and rivers">Lakes and rivers</option>
                        <option value="Unique and unusual places">Unique and unusual places</option>
                    </select>
                </QuestionContainer>

                <QuestionContainer label="What kind of weather do you enjoy? (Select all that apply)">
                    <select
                        name="weather"
                        id="weather"
                        multiple
                        value={formData.weather}
                        onChange={handleChange}
                    >
                        <option value="Warm and sunny">Warm and sunny</option>
                        <option value="Cool climate">Cool climate</option>
                        <option value="Cold and snowy">Cold and snowy</option>
                        <option value="Moderate weather">Moderate weather</option>
                        <option value="No preference">No preference</option>
                    </select>
                </QuestionContainer>

                {/* Остальные вопросы */}
                <QuestionContainer label="When do you plan to travel? (Time of year)">
                    <select
                        name="season"
                        id="season"
                        value={formData.season}
                        onChange={handleChange}
                    >
                        <option value="Winter">Winter</option>
                        <option value="Spring">Spring</option>
                        <option value="Summer">Summer</option>
                        <option value="Fall">Fall</option>
                        <option value="Flexible">Flexible</option>
                    </select>
                </QuestionContainer>

                <QuestionContainer label="What is your approximate budget per person?">
                    <select
                        name="budget"
                        id="budget"
                        value={formData.budget}
                        onChange={handleChange}
                    >
                        <option value="Under $500">Under $500</option>
                        <option value="Between $500 and $1,000">
                            Between $500 and $1,000
                        </option>
                        <option value="Between $1,000 and $2,000">
                            Between $1,000 and $2,000
                        </option>
                        <option value="Over $2,000">Over $2,000</option>
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
