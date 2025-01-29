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
                    {/*TODO: get back!*/}
                    {/*<>{t("aiGenerated")} </>*/}
                    {/*{authorName && authorUrl && (*/}
                    {/*    <>*/}
                    {/*        <a href={authorUrl}>{authorName}</a>*/}
                    {/*    </>*/}
                    {/*)}*/}
                </span>
            ) : (
                <>
                    {authorName && authorUrl && (
                        <span>
                            {t("author")} {authorUrl}
                        </span>
                    )}
                    {licenceUrl && (
                        <>
                            {authorName && authorUrl && "; "}
                            {t("license")} {licenceUrl}
                        </>
                    )}
                </>
            )}
        </div>
    );
};

export default ImageCaption;
