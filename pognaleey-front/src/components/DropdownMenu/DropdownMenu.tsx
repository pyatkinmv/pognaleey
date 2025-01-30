// DropdownMenu.tsx
import React from "react";
import styles from "./DropdownMenu.module.css";

interface DropdownMenuProps {
    label: React.ReactNode;
    items: { label: string; onClick: () => void; icon?: React.ReactNode }[];
}

const DropdownMenu: React.FC<DropdownMenuProps> = ({label, items}) => {
    return (
        <div className={styles.menuContainer}>
            {label} {/* Передаем label напрямую */}
            <div className={styles.dropdownMenu}>
                {items.map((item) => (
                    <button
                        key={item.label}
                        className={styles.dropdownButton}
                        onClick={item.onClick}
                    >
                        {item.icon && <span className={styles.dropdownIcon}>{item.icon}</span>}
                        <span className={styles.dropdownLabel}>{item.label}</span>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default DropdownMenu;