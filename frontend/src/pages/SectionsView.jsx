import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function SectionsView() {
    const [teachers, setTeachers] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [currentTerm, setCurrentTerm] = useState(null);

    const [sections, setSections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        teacherId: '',
        subjectId: '',
        maxCapacity: 30,
        scheduleInput: ''
    });

    useEffect(() => {
        setLoading(true);
        Promise.all([
            apiClient.get('/sections'),
            apiClient.get('/teachers'),
            apiClient.get('/subjects'),
            apiClient.get('/academic-terms/current')
        ])
            .then(([sectionsRes, teachersRes, subjectsRes, termRes]) => {
                setSections(sectionsRes.data);
                setTeachers(teachersRes.data);
                setSubjects(subjectsRes.data);
                setCurrentTerm(termRes.data);
            })
            .catch(err => {
                console.error("Error cargando datos", err);
                setError("Error al cargar los datos maestros. Verifica los endpoints.");
            })
            .finally(() => setLoading(false));
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'maxCapacity' ? Number(value) : value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!currentTerm || !currentTerm.id) {
            alert("No hay un periodo académico activo para crear la sección.");
            return;
        }

        const scheduleArray = formData.scheduleInput
            .split(',')
            .map(s => s.trim().toUpperCase())
            .filter(s => s !== '');

        if (scheduleArray.length === 0) {
            alert("Debes ingresar al menos un bloque de horario válido.");
            return;
        }

        setSubmitting(true);

        const payload = {
            teacherId: Number(formData.teacherId),
            subjectId: Number(formData.subjectId),
            academicTermId: currentTerm.id,
            maxCapacity: formData.maxCapacity,
            schedule: scheduleArray
        };

        apiClient.post('/sections', payload)
            .then((res) => {
                alert('Sección creada con éxito');
                setFormData({
                    teacherId: '', subjectId: '',
                    maxCapacity: 30, scheduleInput: ''
                });
                setSections(prev => [...prev, res.data]);
            })
            .catch(err => {
                alert(err.response?.data?.message || 'Error al crear la sección.');
            })
            .finally(() => setSubmitting(false));
    };

    const handleDelete = (id) => {
        if (!window.confirm("¿Eliminar esta sección?")) return;
        apiClient.delete(`/sections/${id}`)
            .then(() => setSections(prev => prev.filter(s => s.id !== id)))
            .catch(err => alert(err.response?.data?.message || 'Error al eliminar.'));
    };

    const getTeacherName = (id) => teachers.find(t => t.id === id)?.fullName || 'Desconocido';
    const getSubjectName = (id) => subjects.find(s => s.id === id)?.name || 'Desconocida';
    const getTermName = (id) => currentTerm ? currentTerm.period : 'Cargando...';

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-calendar-range me-2"></i>Gestión de Secciones</h2>

            {error && <div className="alert alert-danger">{error}</div>}

            <div className="card shadow-sm mb-4 border-primary">
                <div className="card-header bg-primary text-white">
                    <strong>Aperturar Nueva Sección</strong>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit} className="row g-3 align-items-end">
                        <div className="col-md-3">
                            <label className="form-label">Periodo Académico</label>
                            <div className="form-control">
                                {currentTerm ? currentTerm.period : 'Cargando...'}
                            </div>
                        </div>

                        <div className="col-md-4">
                            <label className="form-label">Asignatura</label>
                            <select name="subjectId" className="form-select" value={formData.subjectId} onChange={handleChange} required>
                                <option value="">Seleccione...</option>
                                {subjects.map(sub => (
                                    <option key={sub.id} value={sub.id}>{sub.code} - {sub.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-5">
                            <label className="form-label">Docente</label>
                            <select name="teacherId" className="form-select" value={formData.teacherId} onChange={handleChange} required>
                                <option value="">Seleccione...</option>
                                {teachers.map(teacher => (
                                    <option key={teacher.id} value={teacher.id}>{teacher.run} - {teacher.fullName}</option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-2">
                            <label className="form-label">Cupo Máximo</label>
                            <input type="number" name="maxCapacity" className="form-control" min="1" value={formData.maxCapacity} onChange={handleChange} required />
                        </div>

                        <div className="col-md-7">
                            <label className="form-label">Horario (Bloques)</label>
                            <input type="text" name="scheduleInput" className="form-control" placeholder="Ej: L2, M3, X4, J1, V5" value={formData.scheduleInput} onChange={handleChange} required />
                            <small className="text-muted">Separar por comas. Usar L, M, W, J, V + número de bloque.</small>
                        </div>

                        <div className="col-md-3 text-end">
                            <button type="submit" className="btn btn-success w-100" disabled={submitting || !currentTerm}>
                                {submitting ? 'Guardando...' : 'Crear Sección'}
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
                                <th>Periodo</th>
                                <th>Asignatura</th>
                                <th>Docente</th>
                                <th>Horario</th>
                                <th>Cupos</th>
                                <th className="text-center">Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr><td colSpan="7" className="text-center py-4">Cargando datos...</td></tr>
                            ) : sections.length === 0 ? (
                                <tr><td colSpan="7" className="text-center py-4 text-muted">No hay secciones aperturadas.</td></tr>
                            ) : (
                                sections.map((sec) => (
                                    <tr key={sec.id}>
                                        <td><strong>{sec.id}</strong></td>
                                        <td>{getTermName(sec.academicTermId)}</td>
                                        <td>{getSubjectName(sec.subjectId)}</td>
                                        <td>{getTeacherName(sec.teacherId)}</td>
                                        <td>
                                            {Array.isArray(sec.schedule) ? sec.schedule.join(' - ') : sec.schedule}
                                        </td>
                                        <td><span className="badge bg-info text-dark">{sec.maxCapacity}</span></td>
                                        <td className="text-center">
                                            <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(sec.id)}>
                                                <i className="bi bi-trash-fill"></i>
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