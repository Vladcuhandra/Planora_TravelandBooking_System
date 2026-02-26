import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";

function Topbar() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await fetch("https://localhost:8443/api/auth/logout", {
                method: "POST",
                credentials: "include",
            });
        } catch (err) {
            // ignore network/logout errors
        } finally {
            // remove JWT token
            localStorage.removeItem("accessToken");

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

                <button
                    className="btn btn-danger-soft"
                    type="button"
                    onClick={handleLogout}
                >
                    Sign out
                </button>
            </div>
        </nav>
    );
}

export default Topbar;