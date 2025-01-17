import React from "react";
import "./DropdownMenu.css";

interface DropdownMenuProps {
    label: React.ReactNode;
    items: { label: string; onClick: () => void; icon?: React.ReactNode }[];
}

const DropdownMenu: React.FC<DropdownMenuProps> = ({label, items}) => {
    return (
        <div className="menu-container">
            {label}
            <div className="dropdown-menu">
                {items.map((item, index) => (
                    <button
                        key={index}
                        className="dropdown-button"
                        onClick={item.onClick}
                    >
                        {item.icon && <span className="dropdown-icon">{item.icon}</span>}
                        <span className="dropdown-label">{item.label}</span>
                    </button>
                ))}
            </div>
        </div>
    );
};


export default DropdownMenu;
