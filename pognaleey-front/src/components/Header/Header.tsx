// Header.tsx
import React, {useState} from "react";
import {useAppContext} from "../../context/AppContext";
import DropdownMenu from "../DropdownMenu/DropdownMenu";
import styles from "./Header.module.css";
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
        <header className={styles.header}>
            <img src="/assets/logos/logo-circle192.png" alt={t("logoAlt")} className={styles.logo}/>
            <nav className={styles.navbar}>
                <a href="/" className={styles.navLink}>{t("home")}</a>
                <a href="https://www.linkedin.com/in/pyatkinmv" className={styles.navLink}>{t("contacts")}</a>

                <div className={styles.dropdownMenu}>
                    <DropdownMenu
                        label={
                            <>
                                <img
                                    src={`/assets/flags/${language}.svg`}
                                    alt={currentLanguage?.label}
                                    className={styles.languageFlag}
                                />
                                <span className={styles.menuLabel}>{currentLanguage?.label}</span>
                            </>
                        }
                        items={languages.map((lang) => ({
                            label: lang.label,
                            onClick: () => handleLanguageSwitch(lang.code),
                            icon: (
                                <img
                                    src={`/assets/flags/${lang.code}.svg`}
                                    alt={lang.label}
                                    className={styles.languageFlag}
                                />
                            ),
                        }))}
                    />
                </div>
                <div className={styles.dropdownMenu}>
                    {user.username ? (
                        <DropdownMenu
                            label={
                                <>
                                    <div className={styles.profileIcon}></div>
                                    <span className={styles.menuLabel}>{user.username}</span>
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
                        <a className={styles.navLink} onClick={() => setShowLoginPopup(true)}>{t("loginHeader")}</a>
                    )}
                </div>
            </nav>
            {showLoginPopup && (
                <LoginPopup
                    onClose={() => setShowLoginPopup(false)}
                    onLoginSuccess={() => setShowLoginPopup(false)}
                />
            )}
        </header>
    );
};

export default Header;