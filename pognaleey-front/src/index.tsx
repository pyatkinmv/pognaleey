import React from 'react';
import ReactDOM from 'react-dom/client';
import AppRouter from "./routes/AppRouter";
import {AppProvider} from "./context/AppContext";
import './styles/index.css';
import {BrowserRouter as Router} from "react-router-dom"; // Перемещаем сюда Router
import './i18n/i18n'; // Импортируем и инициализируем i18next

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

root.render(
    <React.StrictMode>
        <Router>
            <AppProvider>
                <AppRouter/>
            </AppProvider>
        </Router>
    </React.StrictMode>,
);
