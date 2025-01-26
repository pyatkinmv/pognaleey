import React from "react";
import "./ModalImage.css";
import {ImageDto} from "./ImageDto";
import ImageCaption from "./ImageCaption";

interface ModalImageProps {
    image: ImageDto | null;
    onClose: () => void;
}

const ModalImage: React.FC<ModalImageProps> = ({image, onClose}) => {
    if (!image) {
        return null;
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <img className="modal-image" src={image.url} alt={image.url || "Enlarged"}/>
                <div className="image-wrapper">
                    <ImageCaption
                        aiGenerated={image.aiGenerated}
                        authorName={image.authorName}
                        authorUrl={image.authorUrl}
                        licenceUrl={image.licenceUrl}
                        className="modal-image-caption"
                    />
                </div>
                <button className="modal-close" onClick={onClose}>
                    &times;
                </button>
            </div>
        </div>
    );
};

export default ModalImage;
