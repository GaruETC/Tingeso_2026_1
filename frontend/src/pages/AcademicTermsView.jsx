import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function AcademicTermsView() {
    const [terms, setTerms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        year: new Date().getFullYear(),
        semester: 1
    });

    const fetchTerms = () => {
        setLoading(true);
        apiClient.get('/academic-terms')
            .then(res => {
                setTerms(res.data);
                setError(null);
            })
            .catch(err => setError('Error al cargar los periodos académicos.'))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchTerms();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: Number(value)
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (formData.year < 2000 || formData.year > 2100) {
            alert("Por favor ingresa un año válido.");
            return;
        }

        setSubmitting(true);
        apiClient.post('/academic-terms', formData)
            .then(() => {
                alert(`Periodo ${formData.year}-${formData.semester} creado con éxito.`);
                setFormData(prev => ({ year: prev.year, semester: prev.semester === 1 ? 2 : 1 }));
                fetchTerms();
            })
            .catch(err => {
                alert(err.response?.data?.message || 'Error al crear el periodo académico.');
            })
            .finally(() => setSubmitting(false));
    };

    const handleCloseTerm = (id, period) => {
        if (!window.confirm(`¿Marcar el periodo ${period} como CERRADO?`)) return;
        apiClient.patch(`/academic-terms/${id}/close`)
            .then(() => fetchTerms())
            .catch(err => alert(err.response?.data?.message || 'Error al cerrar el periodo.'));
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-calendar3 me-2"></i>Gestión de Periodos Académicos</h2>

            {error && <div className="alert alert-danger">{error}</div>}

            <div className="row">
                <div className="col-md-4">
                    <div className="card shadow-sm mb-4 border-primary">
                        <div className="card-header bg-primary text-white">
                            <strong>Aperturar Periodo</strong>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label">Año</label>
                                    <input
                                        type="number"
                                        name="year"
                                        className="form-control"
                                        min="2000"
                                        max="2100"
                                        value={formData.year}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Semestre</label>
                                    <select
                                        name="semester"
                                        className="form-select"
                                        value={formData.semester}
                                        onChange={handleChange}
                                    >
                                        <option value={1}>Primer Semestre (1)</option>
                                        <option value={2}>Segundo Semestre (2)</option>
                                    </select>
                                    <small className="text-muted d-block mt-2">
                                        El periodo se creará automáticamente en estado ABIERTO.
                                    </small>
                                </div>
                                <button type="submit" className="btn btn-success w-100" disabled={submitting}>
                                    <i className="bi bi-plus-circle me-1"></i>
                                    {submitting ? 'Guardando...' : 'Crear Periodo'}
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div className="col-md-8">
                    <div className="card shadow-sm">
                        <div className="card-body p-0">
                            <table className="table table-hover table-striped mb-0 align-middle">
                                <thead className="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Periodo</th>
                                    <th>Estado</th>
                                    <th className="text-center">Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                {loading ? (
                                    <tr><td colSpan="5" className="text-center py-4">Cargando periodos...</td></tr>
                                ) : terms.length === 0 ? (
                                    <tr><td colSpan="5" className="text-center py-4 text-muted">No hay periodos registrados.</td></tr>
                                ) : (
                                    terms.map(term => (
                                        <tr key={term.id}>
                                            <td><strong>{term.id}</strong></td>
                                            <td>{term.period}</td>
                                            <td>
                                                    <span className={`badge ${term.status === 'ABIERTO' ? 'bg-success' : 'bg-secondary'}`}>
                                                        {term.status}
                                                    </span>
                                            </td>
                                            <td className="text-center">
                                                {term.status === 'ABIERTO' && (
                                                    <button
                                                        className="btn btn-outline-warning btn-sm me-2"
                                                        onClick={() => handleCloseTerm(term.id, term.period)}
                                                        title="Cerrar periodo"
                                                    >
                                                        <i className="bi bi-door-closed-fill"></i>
                                                    </button>
                                                )}
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
        </div>
    );
}