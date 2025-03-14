import React from 'react';
import ReactDOM from 'react-dom/client';
import AppRouter from "./routes/AppRouter";
import {AppProvider} from "./context/AppContext";
import './styles/index.css';
import {BrowserRouter as Router} from "react-router-dom";
import './i18n/i18n';

document.title = process.env.APP_NAME || "Travel Smart!";

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
