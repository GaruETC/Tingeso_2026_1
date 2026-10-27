import React from 'react';
import { Link } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

export default function Navbar() {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) return null;

    const isAdmin = keycloak.hasRealmRole('ADMIN');
    const isTeacher = keycloak.hasRealmRole('TEACHER');
    const isStudent = keycloak.hasRealmRole('STUDENT');

    return (
        <nav className="navbar navbar-expand navbar-dark bg-dark shadow-sm">
            <div className="container-fluid">
                <Link className="navbar-brand d-flex align-items-center gap-2" to="/">
                    <i className="bi bi-mortarboard-fill text-primary"></i>
                    <span>SIGA IPT</span>
                </Link>

                <ul className="navbar-nav me-auto">
                    <li className="nav-item">
                        <Link className="nav-link" to="/">Inicio</Link>
                    </li>

                    {isAdmin && (
                        <li className="nav-item">
                            <Link className="nav-link" to="/teachers">
                                <i className="bi bi-person-badge me-1"></i>Profesores
                            </Link>
                        </li>
                    )}

                    {(isAdmin || isTeacher) && (
                        <li className="nav-item">
                            <Link className="nav-link" to="/courses">
                                <i className="bi bi-journal-bookmark me-1"></i>Cursos
                            </Link>
                        </li>
                    )}

                    {isStudent && (
                        <li className="nav-item">
                            <Link className="nav-link" to="/enrollment">
                                <i className="bi bi-card-checklist me-1"></i>Inscripción
                            </Link>
                        </li>
                    )}
                </ul>

                <div className="d-flex align-items-center gap-3">
                    {keycloak.authenticated ? (
                        <>
                  <span className="navbar-text text-light">
                    <i className="bi bi-person-circle me-1"></i>
                      {keycloak.tokenParsed?.preferred_username}
                      <span className="badge bg-secondary ms-2">
                      {isAdmin ? 'ADMIN' : isTeacher ? 'TEACHER' : isStudent ? 'STUDENT' : 'USER'}
                    </span>
                  </span>
                    <button
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
                    >
                        <i className="bi bi-box-arrow-right me-1"></i>Salir
                    </button>
                        </>
                    ) : (
                        <button
                            className="btn btn-primary btn-sm"
                            onClick={() => keycloak.login()}
                        >
                            <i className="bi bi-box-arrow-in-right me-1"></i>Iniciar Sesión
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}