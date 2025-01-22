import React from "react";
import "./PencilLoader.css";

const PencilLoader: React.FC = () => {
    return (
        <div className="pencil-loader">
            <div className="pencil">✏️</div>
            <div className="writing">
                <span>.</span>
                <span>.</span>
                <span>.</span>
            </div>
        </div>
    );
};

export default PencilLoader;
