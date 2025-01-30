// PencilLoader.tsx
import React from "react";
import styles from "./PencilLoader.module.css";

const PencilLoader: React.FC = () => {
    return (
        <div className={styles.pencilLoader}>
            <div className={styles.pencil}>✏️</div>
            <div className={styles.writing}>
                <span>.</span>
                <span>.</span>
                <span>.</span>
            </div>
        </div>
    );
};

export default PencilLoader;