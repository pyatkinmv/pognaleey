import React from "react";
import "./FilterButtons.css";

interface FilterButtonsProps {
    selectedFilter: string;
    onFilterChange: (value: string) => void;
}

const FilterButtons: React.FC<FilterButtonsProps> = ({selectedFilter, onFilterChange}) => {
    return (
        <div className="radio-buttons-container">
            {[
                {label: "Лучшее", value: "feed"},
                {label: "Понравилось", value: "liked"},
                {label: "Моё", value: "my"},
            ].map(({label, value}) => (
                <label
                    key={value}
                    className={`radio-button ${selectedFilter === value ? "active" : ""}`}
                >
                    <input
                        type="radio"
                        name="filter"
                        value={value}
                        checked={selectedFilter === value}
                        onChange={() => onFilterChange(value)}
                    />
                    {label}
                </label>
            ))}
        </div>
    );
};

export default FilterButtons;