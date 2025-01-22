// TileGrid.tsx
import React from "react";
import "./TileGrid.css";
import {useNavigate} from "react-router-dom";
import CircleLoader from "./CircleLoader";

interface Tile {
    id: number;
    title: string;
    imageUrl: string;
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
        <div className="tile-container">
            {error && <div className="error">{error}</div>}
            {tiles.map((tile, index) => (
                <div
                    className="tile"
                    key={tile.id}
                    ref={index === tiles.length - 1 ? lastTileRef : null}
                    onClick={() => navigate(`/travel-guides/${tile.id}`)}
                >
                    <div className="tile-image-wrapper">
                        <img src={tile.imageUrl} alt={tile.title} className="tile-image"/>
                    </div>
                    <div className="tile-title">{tile.title}</div>
                    <div className="tile-likes">
            <span
                className={`like-button ${tile.isLiked ? "liked" : ""}`}
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