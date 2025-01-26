import React from "react";
import "./ImageCaption.css";

interface ImageCaptionProps {
    aiGenerated?: boolean;
    authorName?: string;
    authorUrl?: string;
    licenceUrl?: string;
    className?: string; // Дополнительный CSS-класс
}

const ImageCaption: React.FC<ImageCaptionProps> = ({
                                                       aiGenerated,
                                                       authorName,
                                                       authorUrl,
                                                       licenceUrl,
                                                       className,
                                                   }) => {
    return (
        <div className={`${className}`.trim()}>
            {aiGenerated ? (
                <span>
                    <>Сгенерировано нейросетью </>
                    {authorName && authorUrl && (
                        <>
                            <a href={authorUrl}>{authorName}</a>
                        </>
                    )}
                </span>
            ) : (
                <>
                    {authorName && authorUrl && (
                        <span>
                            Автор: <a href={authorUrl}>{authorName}</a>
                        </span>
                    )}
                    {licenceUrl && (
                        <>
                            {authorName && authorUrl && "; "}
                            <a href={licenceUrl}>Лицензия</a>
                        </>
                    )}
                </>
            )}
        </div>
    );
};

export default ImageCaption;
