import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function CareersView() {
    const [careers, setCareers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        code: '',
        description: '',
        status: 'ACTIVO'
    });
    const [submitting, setSubmitting] = useState(false);

    const [selectedCareer, setSelectedCareer] = useState(null);
    const [newPlanCode, setNewPlanCode] = useState('');
    const [submittingPlan, setSubmittingPlan] = useState(false);

    const fetchCareers = () => {
        setLoading(true);
        apiClient.get('/careers')
            .then((res) => {
                setCareers(res.data);
                setError(null);
            })
            .catch((err) => {
                setError(err.response?.status === 403
                    ? 'No tienes permisos suficientes.'
                    : 'Error al cargar las carreras.');
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchCareers();
    }, []);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleCreateCareer = (e) => {
        e.preventDefault();
        setSubmitting(true);

        apiClient.post('/careers', formData)
            .then(() => {
                setFormData({ name: '', code: '', description: '', status: 'ACTIVO' });
                fetchCareers();
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al crear la carrera.');
            })
            .finally(() => setSubmitting(false));
    };

    const handleDeleteCareer = (id, name) => {
        const confirmDelete = window.confirm(`¿Estás seguro de que deseas eliminar la carrera "${name}"?`);
        if (!confirmDelete) return;

        apiClient.delete(`/careers/${id}`)
            .then(() => {
                setCareers((prev) => prev.filter((c) => c.id !== id));
                if (selectedCareer?.id === id) setSelectedCareer(null);
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al eliminar la carrera.');
            });
    };

    const handleAddPlan = (e) => {
        e.preventDefault();
        if (!selectedCareer || !newPlanCode.trim()) return;

        setSubmittingPlan(true);
        const planPayload = {
            careerId: selectedCareer.id,
            code: newPlanCode.trim()
        };

        apiClient.post('/study-plans', planPayload)
            .then(() => {
                alert(`Plan de estudio "${newPlanCode}" creado con éxito para ${selectedCareer.name}`);
                setNewPlanCode('');
                setSelectedCareer(null); // Repliega el panel tras la creación
                fetchCareers();
            })
            .catch((err) => {
                alert(err.response?.data?.message || 'Error al crear plan de estudio.');
            })
            .finally(() => setSubmittingPlan(false));
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-mortarboard-fill me-2"></i>Gestión de Carreras y Planes</h2>
                <button className="btn btn-outline-secondary btn-sm" onClick={fetchCareers}>
                    <i className="bi bi-arrow-clockwise me-1"></i>Actualizar
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {/* Formulario de Carrera */}
            <div className="card shadow-sm mb-4">
                <div className="card-header bg-light">
                    <strong>Registrar Nueva Carrera</strong>
                </div>
                <div className="card-body">
                    <form onSubmit={handleCreateCareer} className="row g-3">
                        <div className="col-md-4">
                            <label className="form-label">Nombre de la Carrera</label>
                            <input
                                type="text"
                                name="name"
                                className="form-control"
                                placeholder="Ingeniería Civil en Informática"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-2">
                            <label className="form-label">Código</label>
                            <input
                                type="text"
                                name="code"
                                className="form-control"
                                placeholder="10450"
                                value={formData.code}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-4">
                            <label className="form-label">Descripción</label>
                            <input
                                type="text"
                                name="description"
                                className="form-control"
                                placeholder="Formación en computación y sistemas..."
                                value={formData.description}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="col-md-2">
                            <label className="form-label">Estado</label>
                            <select
                                name="status"
                                className="form-select"
                                value={formData.status}
                                onChange={handleChange}
                            >
                                <option value="ACTIVO">ACTIVO</option>
                                <option value="INACTIVO">INACTIVO</option>
                            </select>
                        </div>

                        <div className="col-12 text-end">
                            <button type="submit" className="btn btn-primary" disabled={submitting}>
                                {submitting ? 'Guardando...' : 'Registrar Carrera'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {/* Panel de Asignación de Plan Vigente */}
            {selectedCareer && (
                <div className="card shadow-sm border-primary mb-4">
                    <div className="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <span>
                            <strong>Asignar Nuevo Plan Vigente a:</strong> {selectedCareer.name} (ID: {selectedCareer.id})
                        </span>
                        <button
                            type="button"
                            className="btn btn-sm btn-outline-light"
                            onClick={() => setSelectedCareer(null)}
                        >
                            Cerrar
                        </button>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleAddPlan} className="row g-3 align-items-end">
                            <div className="col-md-6">
                                <label className="form-label">Código o Versión del Plan</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Ej: Plan 2026-1"
                                    value={newPlanCode}
                                    onChange={(e) => setNewPlanCode(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="col-md-6">
                                <button type="submit" className="btn btn-success" disabled={submittingPlan}>
                                    {submittingPlan ? 'Guardando...' : 'Asignar como Plan VIGENTE'}
                                </button>
                            </div>
                        </form>
                        <small className="text-muted d-block mt-2">
                            * Al asignar este plan, cualquier plan anterior de esta carrera cambiará automáticamente a NO_VIGENTE.
                        </small>
                    </div>
                </div>
            )}

            {/* Tabla de Carreras */}
            <div className="card shadow-sm">
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0 align-middle">
                            <thead className="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>Código</th>
                                <th>Nombre de Carrera</th>
                                <th>Descripción</th>
                                <th>Estado</th>
                                <th className="text-center">Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="text-center py-4">
                                        Cargando carreras...
                                    </td>
                                </tr>
                            ) : careers.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center py-4 text-muted">
                                        No hay registros disponibles.
                                    </td>
                                </tr>
                            ) : (
                                careers.map((career) => (
                                    <tr key={career.id}>
                                        <td><strong>{career.id}</strong></td>
                                        <td>{career.code || '-'}</td>
                                        <td>{career.name}</td>
                                        <td className="text-truncate" style={{ maxWidth: '250px' }} title={career.description}>
                                            {career.description || '-'}
                                        </td>
                                        <td>
                                                <span className={`badge ${career.status === 'ACTIVO' ? 'bg-success' : 'bg-secondary'}`}>
                                                    {career.status}
                                                </span>
                                        </td>
                                        <td className="text-center">
                                            <button
                                                className="btn btn-outline-primary btn-sm me-2"
                                                onClick={() => setSelectedCareer(career)}
                                                title="Gestionar planes de estudio"
                                            >
                                                <i className="bi bi-diagram-3-fill me-1"></i>Asignar Plan
                                            </button>
                                            <button
                                                className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleDeleteCareer(career.id, career.name)}
                                                title="Eliminar carrera"
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