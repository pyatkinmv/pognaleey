// ModalImage.tsx
import React from "react";
import styles from "./ModalImage.module.css";
import {ImageDto} from "../../types/ImageDto";
import ImageCaption from "../ImageCaption/ImageCaption";

interface ModalImageProps {
    image: ImageDto | null;
    onClose: () => void;
}

const ModalImage: React.FC<ModalImageProps> = ({image, onClose}) => {
    if (!image) {
        return null;
    }

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                <img className={styles.modalImage} src={image.url} alt={image.url || "Enlarged"}/>
                <div className="image-wrapper">
                    <ImageCaption
                        aiGenerated={image.aiGenerated}
                        authorName={image.authorName}
                        authorUrl={image.authorUrl}
                        licenceUrl={image.licenceUrl}
                        className={styles.modalImageCaption}
                    />
                </div>
                <button className={styles.modalClose} onClick={onClose}>
                    &times;
                </button>
            </div>
        </div>
    );
};

export default ModalImage;