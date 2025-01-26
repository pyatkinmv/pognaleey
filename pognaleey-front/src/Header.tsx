import React, {useState} from "react";
import {useAppContext} from "./AppContext";
import DropdownMenu from "./DropdownMenu";
import "./Header.css";
import LoginPopup from "./LoginPopup";

const Header: React.FC = () => {
    const {user, language, languages, handleLogout, handleLanguageChange} = useAppContext();
    const [showLoginPopup, setShowLoginPopup] = useState<boolean>(false);

    const currentLanguage = languages.find((lang) => lang.code === language);

    return (
        <header className="header">
            <img src="/logo-circle192.png" alt="Логотип" className="logo"/>
            <nav className="navbar">
                <a href="/" className="nav-link">Главная</a>
                <a href="https://t.me/pyatkinmv" className="nav-link">Контакты</a>
                <DropdownMenu
                    label={
                        <>
                            <img
                                src={`/flags/${language}.svg`}
                                alt={currentLanguage?.label}
                                className="language-flag"
                            />
                            <span className="menu-label">{currentLanguage?.label}</span>
                        </>
                    }
                    items={languages.map((lang) => ({
                        label: lang.label,
                        onClick: () => handleLanguageChange(lang.code),
                        icon: (
                            <img
                                src={`/flags/${lang.code}.svg`}
                                alt={lang.label}
                                className="language-flag"
                            />
                        ),
                    }))}
                />
                <div className="user-menu">
                    {user.username ? (
                        <DropdownMenu
                            label={
                                <>
                                    <div className="profile-icon"></div>
                                    <span className="user-name">{user.username}</span>
                                </>
                            }
                            items={[
                                {
                                    label: "Выйти",
                                    onClick: handleLogout,
                                },
                            ]}
                        />
                    ) : (
                        <a className="nav-link" onClick={() => setShowLoginPopup(true)}>🔒 Войти</a>
                    )}
                </div>
            </nav>
            {
                showLoginPopup && (
                    <LoginPopup
                        onClose={() => setShowLoginPopup(false)}
                        onLoginSuccess={() => setShowLoginPopup(false)}
                    />
                )
            }
        </header>
    );
};

export default Header;
