import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import TeachersView from './pages/TeachersView';
import StudentsView from "./pages/StudentsView";
import CareersView from "./pages/CareersView";
import { PrivateRoute } from './components/PrivateRoute';

function Home() {
    return (
        <div className="container mt-5 text-center">
            <div className="p-5 mb-4 bg-light rounded-3 shadow-sm">
                <h1 className="display-5 fw-bold">Sistema de Gestión de Ramos</h1>
                <p className="col-md-8 fs-5 mx-auto text-muted">
                    Plataforma académica integral para administración curricular, gestión docente e inscripción estudiantil.
                </p>
            </div>
        </div>
    );
}

function Unauthorized() {
    return (
        <div className="container mt-5">
            <div className="alert alert-danger text-center p-4 shadow-sm" role="alert">
                <h4 className="alert-heading"><i className="bi bi-shield-lock-fill me-2"></i>Acceso Denegado</h4>
                <p className="mb-0">No posees los roles ni privilegios necesarios para acceder a este recurso.</p>
            </div>
        </div>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <Navbar />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/unauthorized" element={<Unauthorized />} />

                <Route
                    path="/teachers"
                    element={
                        <PrivateRoute requiredRole="ADMIN">
                            <TeachersView />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/students"
                    element={
                        <PrivateRoute requiredRole="ADMIN">
                            <StudentsView />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/careers"
                    element={
                        <PrivateRoute requiredRole="ADMIN">
                            <CareersView />
                        </PrivateRoute>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}