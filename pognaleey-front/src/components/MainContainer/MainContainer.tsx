// MainContainer.tsx
import React from "react";
import styles from "./MainContainer.module.css";

interface MainContainerProps {
    children: React.ReactNode;
}

const MainContainer: React.FC<MainContainerProps> = ({children}) => {
    return (
        <div className={styles.mainContainer}>
            <div className={styles.contentContainer}>{children}</div>
        </div>
    );
};

export default MainContainer;