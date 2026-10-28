import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common.js';

export default function StudentsView() {
    const [students, setStudents] = useState([]);
    const [careers, setCareers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        run: '',
        fullName: '',
        email: '',
        careerId: '',
    });
    const [submitting, setSubmitting] = useState(false);

    const fetchStudents = () => {
        setLoading(true);
        apiClient.get('/students')
            .then((res) => {
                setStudents(res.data);
                setError(null);
            })
            .catch((err) => {
                setError(err.response?.status === 403
                    ? 'No tienes permisos suficientes.'
                    : 'Error al cargar la lista de estudiantes.');
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchStudents();
        apiClient.get('/careers')
            .then((res) => setCareers(res.data))
            .catch(() => console.warn('No se pudo cargar la lista de carreras'));
    }, []);

    const getCareerName = (careerId) => {
        const career = careers.find(c => c.id === careerId);
        return career ? career.name : 'Sin asignar';
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleCreate = (e) => {
        e.preventDefault();
        setSubmitting(true);

        apiClient.post('/students', formData)
            .then(() => {
                setFormData({ run: '', fullName: '', email: '', careerId: ''});
                fetchStudents();
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al crear estudiante en la base de datos');
            })
            .finally(() => setSubmitting(false));
    };

    const handleDelete = (id, fullName) => {
        const confirmDelete = window.confirm(`¿Estás seguro de que deseas eliminar al estudiante "${fullName}"?`);
        if (!confirmDelete) return;

        apiClient.delete(`/students/${id}`)
            .then(() => {
                setStudents((prev) => prev.filter((s) => s.id !== id));
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al eliminar el docente.');
            });
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-people-fill me-2"></i>Gestión de Estudiantes</h2>
                <button className="btn btn-outline-secondary btn-sm" onClick={fetchStudents}>
                    <i className="bi bi-arrow-clockwise me-1"></i>Actualizar
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            <div className="card shadow-sm mb-4">
                <div className="card-header bg-light">
                    <strong>Registrar Nuevo Estudiante</strong>
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
                            <label className="form-label">Carrera</label>
                            <select
                                name="careerId"
                                className="form-select"
                                value={formData.careerId}
                                onChange={handleChange}
                                required
                            >
                                <option value="">Seleccionar Carrera...</option>
                                {careers.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {c.name || `Carrera #${c.id}`}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-12 text-end">
                            <button type="submit" className="btn btn-primary" disabled={submitting}>
                                {submitting ? 'Guardando...' : 'Registrar Estudiante'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

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
                                <th>Carrera</th>
                                <th>Estado</th>
                                <th className="text-center">Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="7" className="text-center py-4">
                                        Cargando Estudiantes...
                                    </td>
                                </tr>
                            ) : students.length === 0 ? (
                                <tr>
                                    <td colSpan="7" className="text-center py-4 text-muted">
                                        No hay registros disponibles.
                                    </td>
                                </tr>
                            ) : (
                                students.map((student) => (
                                    <tr key={student.id}>
                                        <td>{student.id}</td>
                                        <td>{student.run}</td>
                                        <td>{student.fullName}</td>
                                        <td>{student.email}</td>
                                        <td>{getCareerName(student.careerId)}</td>
                                        <td>{student.status}</td>
                                        <td className="text-center">
                                            <button
                                                className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleDelete(student.id, student.fullName)}
                                            >
                                                <i className="bi bi-trash-fill me-1"></i>Eliminar
                                            </button>
                                        </td>
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