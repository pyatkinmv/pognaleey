import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import App from "./App";
import Recommendations from "./Recommendations";

const AppRouter: React.FC = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<App/>}/>
                <Route
                    path="/api/v1/travel-inquiries/:inquiryId/recommendations"
                    element={<Recommendations/>}
                />
            </Routes>
        </Router>
    );
};

export default AppRouter;
