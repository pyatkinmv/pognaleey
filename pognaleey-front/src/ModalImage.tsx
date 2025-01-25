import React from "react";
import "./ModalImage.css";
import {ImageDto} from "./ImageDto";

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
                <img className="modal-image" src={image.url} alt="Enlarged"/>
                <div className="image-caption">
                    {image.ownerName && image.ownerUrl && (
                        <span>
                            Автор: <a href={image.ownerUrl}>{image.ownerName}</a>
                        </span>
                    )}
                    {image.licenceUrl && (
                        <>
                            {image.ownerName && image.ownerUrl && "; "}
                            <a href={image.licenceUrl}>Лицензия</a>
                        </>
                    )}
                </div>
                <button className="modal-close" onClick={onClose}>
                    &times;
                </button>
            </div>
        </div>
    );
};

export default ModalImage;
