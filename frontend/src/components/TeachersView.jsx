import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function TeachersView() {
    const [teachers, setTeachers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Estados correspondientes a tu entidad Java
    const [formData, setFormData] = useState({
        run: '',
        fullName: '',
        email: '',
        title: '',
        degree: ''
    });
    const [submitting, setSubmitting] = useState(false);

    const fetchTeachers = () => {
        setLoading(true);
        apiClient.get('/teachers')
            .then((res) => {
                setTeachers(res.data);
                setError(null);
            })
            .catch((err) => {
                setError(err.response?.status === 403
                    ? 'No tienes permisos suficientes.'
                    : 'Error al cargar la lista de docentes.');
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchTeachers();
    }, []);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleCreate = (e) => {
        e.preventDefault();
        setSubmitting(true);

        apiClient.post('/teachers', formData)
            .then(() => {
                // Limpiar el formulario
                setFormData({ run: '', fullName: '', email: '', title: '', degree: '' });
                fetchTeachers();
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al crear docente en la base de datos');
            })
            .finally(() => setSubmitting(false));
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-people-fill me-2"></i>Gestión de Docentes</h2>
                <button className="btn btn-outline-secondary btn-sm" onClick={fetchTeachers}>
                    <i className="bi bi-arrow-clockwise me-1"></i>Actualizar
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {/* Formulario completo con todos los campos obligatorios */}
            <div className="card shadow-sm mb-4">
                <div className="card-header bg-light">
                    <strong>Registrar Nuevo Docente</strong>
                </div>
                <div className="card-body">
                    <form onSubmit={handleCreate} className="row g-3">
                        <div className="col-md-4">
                            <label className="form-label">RUN</label>
                            <input
                                type="text"
                                name="run"
                                className="form-control"
                                placeholder="12345678-9"
                                value={formData.run}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-4">
                            <label className="form-label">Nombre Completo</label>
                            <input
                                type="text"
                                name="fullName"
                                className="form-control"
                                placeholder="Alan Turing"
                                value={formData.fullName}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-4">
                            <label className="form-label">Correo Electrónico</label>
                            <input
                                type="email"
                                name="email"
                                className="form-control"
                                placeholder="turing@academic.cl"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Título Profesional</label>
                            <input
                                type="text"
                                name="title"
                                className="form-control"
                                placeholder="Ingeniero Civil en Informática"
                                value={formData.title}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Grado Académico</label>
                            <input
                                type="text"
                                name="degree"
                                className="form-control"
                                placeholder="Doctor en Ciencias de la Computación"
                                value={formData.degree}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-12 text-end">
                            <button type="submit" className="btn btn-primary" disabled={submitting}>
                                {submitting ? 'Guardando...' : 'Registrar Docente'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {/* Tabla con los nombres de atributos exactos del Entity */}
            <div className="card shadow-sm">
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0 align-middle">
                            <thead className="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>RUN</th>
                                <th>Nombre Completo</th>
                                <th>Correo</th>
                                <th>Título</th>
                                <th>Grado</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="text-center py-4">
                                        Cargando profesores...
                                    </td>
                                </tr>
                            ) : teachers.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center py-4 text-muted">
                                        No hay registros disponibles.
                                    </td>
                                </tr>
                            ) : (
                                teachers.map((teacher) => (
                                    <tr key={teacher.id}>
                                        <td>{teacher.id}</td>
                                        <td>{teacher.run}</td>
                                        <td>{teacher.fullName}</td>
                                        <td>{teacher.email}</td>
                                        <td>{teacher.title}</td>
                                        <td>{teacher.degree}</td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}