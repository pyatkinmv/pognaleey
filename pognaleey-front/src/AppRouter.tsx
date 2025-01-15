import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Recommendations from "./Recommendations";
import LoginForm from "./LoginForm";
import RegisterForm from "./RegisterForm";
import PrivateRoute from "./PrivateRoute";
import Guide from "./Guide";
import React from "react";
import Main from "./Main";
import Inquiry from "./Inquiry";

const AppRouter: React.FC = () => {
    return (
        <Router>
            <Routes>
                {/* Доступные без авторизации страницы */}
                <Route path="/login" element={<LoginForm/>}/>
                <Route path="/register" element={<RegisterForm/>}/>

                {/* Защищенные маршруты */}
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
                    path="/travel-inquiries/:inquiryId/recommendations"
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
                            <p>Страница не найдена</p>
                            <a href="/" style={{color: "#4caf50", textDecoration: "none"}}>
                                Вернуться на главную
                            </a>
                        </div>
                    }
                />
            </Routes>
        </Router>
    );
};

export default AppRouter;
