import React, {useState} from "react";

const App: React.FC = () => {
    const [formData, setFormData] = useState({
        purpose: [] as string[],
        nature: [] as string[],
        weather: [] as string[],
        season: "",
        budget: {from: 500, to: 2000}, // Диапазон бюджета
        duration: "",
        interests: [] as string[],
        from: "",
        to: "",
        companions: "",
        preferences: "",
    });

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
    ) => {
        const {name, value, type} = e.target;

        if (type === "checkbox") {
            const isChecked = (e.target as HTMLInputElement).checked;
            setFormData((prevData) => {
                const selectedValues = prevData[name as keyof typeof formData] as string[];
                return {
                    ...prevData,
                    [name]: isChecked
                        ? [...selectedValues, value]
                        : selectedValues.filter((v) => v !== value),
                };
            });
        } else if (type === "range" && name.startsWith("budget")) {
            // Обработка диапазона для бюджета
            const rangeKey = name === "budgetFrom" ? "from" : "to";
            setFormData({
                ...formData,
                budget: {
                    ...formData.budget,
                    [rangeKey]: +value,
                },
            });
        } else {
            setFormData({
                ...formData,
                [name]: value,
            });
        }
    };

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
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Relaxation and leisure"
                        onChange={handleChange}
                    />{" "}
                    Relaxation and leisure
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Active vacation (hiking, surfing, etc.)"
                        onChange={handleChange}
                    />{" "}
                    Active vacation (hiking, surfing, etc.)
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Exploring culture and landmarks"
                        onChange={handleChange}
                    />{" "}
                    Exploring culture and landmarks
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Shopping and entertainment"
                        onChange={handleChange}
                    />{" "}
                    Shopping and entertainment
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Family trip"
                        onChange={handleChange}
                    />{" "}
                    Family trip
                    <input
                        type="checkbox"
                        name="purpose"
                        value="Other"
                        onChange={handleChange}
                    />{" "}
                    Other
                </QuestionContainer>

                <QuestionContainer label="What type of nature or atmosphere do you prefer? (Select all that apply)">
                    <input
                        type="checkbox"
                        name="nature"
                        value="Beaches"
                        onChange={handleChange}
                    />{" "}
                    Beaches
                    <input
                        type="checkbox"
                        name="nature"
                        value="Mountains"
                        onChange={handleChange}
                    />{" "}
                    Mountains
                    <input
                        type="checkbox"
                        name="nature"
                        value="Forests and nature"
                        onChange={handleChange}
                    />{" "}
                    Forests and nature
                    <input
                        type="checkbox"
                        name="nature"
                        value="City life"
                        onChange={handleChange}
                    />{" "}
                    City life
                    <input
                        type="checkbox"
                        name="nature"
                        value="Lakes and rivers"
                        onChange={handleChange}
                    />{" "}
                    Lakes and rivers
                    <input
                        type="checkbox"
                        name="nature"
                        value="Unique and unusual places"
                        onChange={handleChange}
                    />{" "}
                    Unique and unusual places
                </QuestionContainer>

                <QuestionContainer label="What is your approximate budget per person?">
                    <div className="budget-range">
                        <label>
                            From: ${formData.budget.from}
                            <input
                                type="range"
                                name="budgetFrom"
                                min="100"
                                max="5000"
                                step="100"
                                value={formData.budget.from}
                                onChange={handleChange}
                            />
                        </label>
                        <label>
                            To: ${formData.budget.to}
                            <input
                                type="range"
                                name="budgetTo"
                                min="100"
                                max="5000"
                                step="100"
                                value={formData.budget.to}
                                onChange={handleChange}
                            />
                        </label>
                    </div>
                    <p>
                        Selected budget range: ${formData.budget.from} - ${formData.budget.to}
                    </p>
                </QuestionContainer>

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
