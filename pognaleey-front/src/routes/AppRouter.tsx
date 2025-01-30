import {Route, Routes} from "react-router-dom";
import Recommendations from "../pages/Recommendations/Recommendations";
import PrivateRoute from "./PrivateRoute";
import Guide from "../pages/Guide/Guide";
import React from "react";
import Main from "../pages/Main/Main";
import Inquiry from "../pages/Inquiry/Inquiry";
import {useTranslation} from "react-i18next";

const AppRouter: React.FC = () => {
    const {t} = useTranslation();

    return (
        <Routes>
            <Route
                path="/"
                element={
                    <PrivateRoute>
                        <Main/>
                    </PrivateRoute>
                }
            />
            <Route
                path="/travel-inquiries"
                element={
                    <PrivateRoute>
                        <Inquiry/>
                    </PrivateRoute>
                }
            />
            <Route
                path="/travel-recommendations"
                element={
                    <PrivateRoute>
                        <Recommendations/>
                    </PrivateRoute>
                }
            />
            <Route
                path="/travel-guides/:guideId"
                element={
                    <PrivateRoute>
                        <Guide/>
                    </PrivateRoute>
                }
            />

            {/* Обработка неизвестных маршрутов */}
            <Route
                path="*"
                element={
                    <div style={{textAlign: "center", marginTop: "20vh"}}>
                        <h1>404</h1>
                        <p>{t("pageNotFound")}</p>
                        <a href="/" style={{color: "#4caf50", textDecoration: "none"}}>
                            {t("backToHome")}
                        </a>
                    </div>
                }
            />
        </Routes>
    );
};

export default AppRouter;
