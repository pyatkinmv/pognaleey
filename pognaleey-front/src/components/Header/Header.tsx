import React, {useState} from "react";
import {useAppContext} from "../../context/AppContext";
import DropdownMenu from "../DropdownMenu/DropdownMenu";
import "./Header.css";
import LoginPopup from "../LoginPopup/LoginPopup";
import {useTranslation} from "react-i18next";

interface HeaderProps {
    onLanguageChange?: () => void; // Новый пропс для перезагрузки контента
}

const Header: React.FC<HeaderProps> = ({onLanguageChange}) => {
    const {user, language, languages, handleLogout, handleLanguageChange} = useAppContext();
    const [showLoginPopup, setShowLoginPopup] = useState<boolean>(false);
    const {t} = useTranslation();

    const currentLanguage = languages.find((lang) => lang.code === language);

    const handleLanguageSwitch = (langCode: string) => {
        handleLanguageChange(langCode); // Смена языка в контексте
        if (onLanguageChange) {
            onLanguageChange(); // Вызываем перезагрузку контента
        }
    };

    return (
        <header className="header">
            <img src="/assets/logos/logo-circle192.png" alt={t("logoAlt")} className="logo"/>
            <nav className="navbar">
                <a href="/" className="nav-link">{t("home")}</a>
                <a href="https://t.me/pyatkinmv" className="nav-link">{t("contacts")}</a>
                <DropdownMenu
                    label={
                        <>
                            <img
                                src={`/assets/flags/${language}.svg`}
                                alt={currentLanguage?.label}
                                className="language-flag"
                            />
                            <span className="menu-label">{currentLanguage?.label}</span>
                        </>
                    }
                    items={languages.map((lang) => ({
                        label: lang.label,
                        onClick: () => handleLanguageSwitch(lang.code),
                        icon: (
                            <img
                                src={`/assets/flags/${lang.code}.svg`}
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
                                    label: t("logout"),
                                    onClick: handleLogout,
                                },
                            ]}
                        />
                    ) : (
                        <a className="nav-link" onClick={() => setShowLoginPopup(true)}>{t("loginHeader")}</a>
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
