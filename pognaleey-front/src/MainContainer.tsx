import React from "react";
import "./MainContainer.css";

interface MainContainerProps {
    children: React.ReactNode;
}

const MainContainer: React.FC<MainContainerProps> = ({children}) => {
    return (
        <div className="main-container">
            <div className="content-container">{children}</div>
        </div>
    );
};

export default MainContainer;
