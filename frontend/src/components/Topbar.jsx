import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import signout from "../assets/icons/sign-out.png";
import avatar from "../assets/icons/user.gif";
import {clearAccessToken} from "../api/tokenStore.js";

function Topbar() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await fetch("https://localhost:8443/api/auth/logout", {
                method: "POST",
                credentials: "include",
            });
        } catch (err) {
        }
        finally {
                clearAccessToken();
                navigate("/login", { replace: true });
        }

    };

    return (
        <nav className="navbar p-topbar sticky-top">
            <div className="container-fluid topbar-tight">
                <div className="d-flex align-items-center gap-2 topbar-left">
                    {/* Burger */}
                    <button
                        className="btn btn-soft topbar-burger"
                        type="button"
                        data-bs-toggle="offcanvas"
                        data-bs-target="#sidebar"
                        aria-label="Menu"
                    >
                        ☰
                    </button>

                    {/* Logo + Title */}
                    <Link to="/main" className="topbar-brand">
                        <img className="topbar-logo" src={logo} alt="Planora logo" />
                        <span className="topbar-title">Planora</span>
                    </Link>
                </div>

                {/* User Dropdown */}
                <div className="dropdown">
                    <button
                        className="btn p-0 border-0 bg-transparent dropdown-toggle"
                        type="button"
                        data-bs-toggle="dropdown"
                        aria-expanded="false"
                    >
                        <img
                            src={avatar}
                            alt="User menu"
                            className="rounded-circle"
                            style={{ width: "36px", height: "36px", objectFit: "cover" }}
                        />
                    </button>

                    <ul className="dropdown-menu dropdown-menu-end shadow p-2">
                        <li>
                            <button
                                className="dropdown-item btn btn-danger-soft d-flex align-items-center gap-2"
                                type="button"
                                onClick={handleLogout}
                            >
                                <img
                                    src={signout}
                                    alt="Sign out"
                                    style={{ width: "18px", height: "18px" }}
                                />
                                <span>Sign out</span>
                            </button>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
    );
}

export default Topbar;