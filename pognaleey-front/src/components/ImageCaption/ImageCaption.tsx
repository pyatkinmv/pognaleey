import React from "react";
import "./ImageCaption.css";
import {useTranslation} from "react-i18next";

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
    const {t} = useTranslation();

    return (
        <div className={`${className}`.trim()}>
            {aiGenerated ? (
                <span>
                    <>{t("aiGenerated")} </>
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
                            {t("author")} <a href={authorUrl}>{authorName}</a>
                        </span>
                    )}
                    {licenceUrl && (
                        <>
                            {authorName && authorUrl && "; "}
                            <a href={licenceUrl}>{t("license")}</a>
                        </>
                    )}
                </>
            )}
        </div>
    );
};

export default ImageCaption;
