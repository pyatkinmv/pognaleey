import React from "react";
import {useNavigate} from "react-router-dom";
import DropdownMenu from "./DropdownMenu";
import "./Header.css";

interface HeaderProps {
    user: { username: string | null };
    language: string;
    languages: { code: string; label: string }[];
    onLogout: () => void;
    onLanguageChange: (code: string) => void;
}

const Header: React.FC<HeaderProps> = ({user, language, languages, onLogout, onLanguageChange}) => {
    const navigate = useNavigate();
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
                        onClick: () => onLanguageChange(lang.code),
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
                                    onClick: onLogout,
                                },
                            ]}
                        />
                    ) : (
                        <a href="/login" className="nav-link">🔒 Войти</a>
                    )}
                </div>
            </nav>
        </header>
    );
};

export default Header;