// FilterButtons.tsx
import React from "react";
import styles from "./FilterButtons.module.css";
import {useTranslation} from "react-i18next";

interface FilterButtonsProps {
    selectedFilter: string;
    onFilterChange: (value: string) => void;
}

const FilterButtons: React.FC<FilterButtonsProps> = ({selectedFilter, onFilterChange}) => {
    const {t} = useTranslation();
    return (
        <div className={styles.radioButtonsContainer}>
            {[
                {label: t("best"), value: "feed"},
                {label: t("liked"), value: "liked"},
                {label: t("my"), value: "my"},
            ].map(({label, value}) => (
                <label
                    key={value}
                    className={`${styles.radioButton} ${selectedFilter === value ? styles.active : ""}`}
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