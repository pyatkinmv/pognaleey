// TileGrid.tsx
import React from "react";
import styles from "./TileGrid.module.css";
import sharedStyles from "../../styles/shared.module.css"; // Импортируем общие стили
import CircleLoader from "../loaders/CircleLoader/CircleLoader";
import {ImageDto} from "../../types/ImageDto";
import {useNavigate} from "react-router-dom";

interface Tile {
    id: number;
    title: string;
    image: ImageDto;
    isLiked: boolean;
    totalLikes: number;
}

interface TileGridProps {
    tiles: Tile[];
    onLike: (id: number, isCurrentlyLiked: boolean) => void;
    lastTileRef: React.RefObject<HTMLDivElement>;
    isLoading: boolean;
    error: string | null;
}

const TileGrid: React.FC<TileGridProps> = ({tiles, onLike, lastTileRef, isLoading, error}) => {
    const navigate = useNavigate();

    return (
        <div className={styles.tileContainer}>
            {error && <div className="error">{error}</div>}
            {tiles.map((tile, index) => (
                <div
                    className={styles.tile}
                    key={tile.id}
                    ref={index === tiles.length - 1 ? lastTileRef : null}
                    onClick={() => navigate(`/travel-guides/${tile.id}`)}
                >
                    <div className={styles.tileImageWrapper}>
                        {tile.image ? (
                            <img src={tile.image.thumbnailUrl} alt={tile.title} className={styles.tileImage}/>
                        ) : (
                            <img
                                className={styles.tileImage}
                                src="/assets/images/not-found512.png"
                                alt="Not Found"
                            />
                        )}
                    </div>
                    <div className={styles.tileTitle}>{tile.title}</div>
                    <div className={sharedStyles.likeCaption}> {/* Используем общий стиль */}
                        <span
                            className={`${sharedStyles.likeButton} ${tile.isLiked ? sharedStyles.liked : ""}`}
                            onClick={(e) => {
                                e.stopPropagation();
                                onLike(tile.id, tile.isLiked);
                            }}
                        >
                            ❤
                        </span>
                        {tile.totalLikes}
                    </div>
                </div>
            ))}
            {isLoading && <CircleLoader/>}
        </div>
    );
};

export default TileGrid;