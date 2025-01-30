// CircleLoader.tsx
import React from "react";
import styles from "./CircleLoader.module.css";

const CircleLoader: React.FC = () => {
    return (
        <div className={styles.circleLoaderContainer}>
            <div className={styles.circleLoader}></div>
        </div>
    );
};

export default CircleLoader;