import { Link } from "react-router-dom";
import logo from "../assets/logo.png"; // adjust path

function Topbar() {
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
                    <Link to="/user" className="topbar-brand">
                        <img className="topbar-logo" src={logo} alt="Planora logo" />
                        <span className="topbar-title">Planora</span>
                    </Link>
                </div>

                <button
                    className="btn btn-danger-soft"
                    onClick={() => {
                        fetch("/logout", { method: "POST" }).then(() => {
                            window.location.href = "/login";
                        });
                    }}
                >
                    Sign out
                </button>
            </div>
        </nav>
    );
}

export default Topbar;