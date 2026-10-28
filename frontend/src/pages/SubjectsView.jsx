import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function SubjectsView() {
    const [careers, setCareers] = useState([]);
    const [studyPlans, setStudyPlans] = useState([]);
    const [selectedPlanId, setSelectedPlanId] = useState('');

    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        code: '',
        name: '',
        semester: 1,
        theoryHours: 0,
        exerciseHours: 0,
        labHours: 0,
        sct: 1,
        prerequisiteIds: []
    });

    useEffect(() => {
        Promise.all([
            apiClient.get('/careers'),
            apiClient.get('/study-plans') // Asumiendo que tienes este endpoint
        ]).then(([careersRes, plansRes]) => {
            setCareers(careersRes.data);
            setStudyPlans(plansRes.data);
        }).catch(err => {
            console.error("Error cargando datos base", err);
            setError("No se pudieron cargar las carreras o planes de estudio.");
        });
    }, []);

    useEffect(() => {
        if (!selectedPlanId) {
            setSubjects([]);
            return;
        }
        setLoading(true);
        apiClient.get(`/subjects?studyPlanId=${selectedPlanId}`)
            .then(res => setSubjects(res.data))
            .catch(err => setError("Error al cargar las asignaturas del plan."))
            .finally(() => setLoading(false));
    }, [selectedPlanId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: ['semester', 'theoryHours', 'exerciseHours', 'labHours', 'sct'].includes(name)
                ? Number(value)
                : value
        }));
    };

    const handlePrerequisitesChange = (e) => {
        const options = e.target.options;
        const selectedValues = [];
        for (let i = 0; i < options.length; i++) {
            if (options[i].selected) {
                selectedValues.push(Number(options[i].value));
            }
        }
        if (selectedValues.length > 3) {
            alert("Puedes seleccionar un máximo de 3 prerrequisitos.");
            return;
        }
        setFormData(prev => ({ ...prev, prerequisiteIds: selectedValues }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // Regla 1: TEL suma < 8
        const totalTel = formData.theoryHours + formData.exerciseHours + formData.labHours;
        if (totalTel >= 8) {
            alert("La suma total de horas TEL (Teoría, Ejercicios, Laboratorio) debe ser inferior a 8.");
            return;
        }

        // Regla 2: SCT entre 1 y 7
        if (formData.sct <= 0 || formData.sct > 7) {
            alert("Los créditos (SCT) deben ser mayores a 0 y menores o iguales a 7.");
            return;
        }

        setSubmitting(true);
        const payload = {
            ...formData,
            studyPlanId: Number(selectedPlanId),
            tel: `${formData.theoryHours}-${formData.exerciseHours}-${formData.labHours}` // Formato requerido ej: 2-2-2
        };

        apiClient.post('/subjects', payload)
            .then(() => {
                alert('Asignatura creada con éxito');
                // Limpiar formulario manteniendo el semestre
                setFormData({
                    code: '', name: '', semester: formData.semester,
                    theoryHours: 0, exerciseHours: 0, labHours: 0,
                    sct: 1, prerequisiteIds: []
                });
                // Recargar lista
                return apiClient.get(`/subjects?studyPlanId=${selectedPlanId}`);
            })
            .then(res => setSubjects(res.data))
            .catch(err => {
                alert(err.response?.data?.message || 'Error al crear la asignatura.');
            })
            .finally(() => setSubmitting(false));
    };

    const handleDelete = (id, name) => {
        if (!window.confirm(`¿Eliminar la asignatura "${name}"?`)) return;
        apiClient.delete(`/subjects/${id}`)
            .then(() => setSubjects(prev => prev.filter(s => s.id !== id)))
            .catch(err => alert(err.response?.data?.message || 'Error al eliminar.'));
    };

    const availablePrerequisites = subjects.filter(s => s.semester < formData.semester);

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-book-half me-2"></i>Gestión de Asignaturas</h2>

            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <label className="form-label fw-bold">Selecciona el Plan de Estudios para gestionar sus asignaturas</label>
                    <select
                        className="form-select"
                        value={selectedPlanId}
                        onChange={(e) => setSelectedPlanId(e.target.value)}
                    >
                        <option value="">-- Seleccione un Plan --</option>
                        {studyPlans.map(plan => {
                            const career = careers.find(c => c.id === plan.careerId);
                            return (
                                <option key={plan.id} value={plan.id}>
                                    {career?.name} - Código Plan: {plan.code} ({plan.status})
                                </option>
                            );
                        })}
                    </select>
                </div>
            </div>

            {selectedPlanId && (
                <>
                    <div className="card shadow-sm mb-4 border-primary">
                        <div className="card-header bg-primary text-white">
                            <strong>Registrar Nueva Asignatura</strong>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleSubmit} className="row g-3">
                                <div className="col-md-3">
                                    <label className="form-label">Código</label>
                                    <input type="text" name="code" className="form-control" placeholder="Ej: TAP101" value={formData.code} onChange={handleChange} required />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Nombre de Asignatura</label>
                                    <input type="text" name="name" className="form-control" value={formData.name} onChange={handleChange} required />
                                </div>
                                <div className="col-md-3">
                                    <label className="form-label">Semestre</label>
                                    <select name="semester" className="form-select" value={formData.semester} onChange={handleChange}>
                                        {[1, 2, 3, 4].map(s => <option key={s} value={s}>Semestre {s}</option>)}
                                    </select>
                                </div>

                                <div className="col-md-4">
                                    <label className="form-label">Horas TEL (Teoría - Ejercicios - Lab)</label>
                                    <div className="input-group">
                                        <input type="number" name="theoryHours" className="form-control" min="0" value={formData.theoryHours} onChange={handleChange} title="Teoría" />
                                        <span className="input-group-text">-</span>
                                        <input type="number" name="exerciseHours" className="form-control" min="0" value={formData.exerciseHours} onChange={handleChange} title="Ejercicios" />
                                        <span className="input-group-text">-</span>
                                        <input type="number" name="labHours" className="form-control" min="0" value={formData.labHours} onChange={handleChange} title="Laboratorio" />
                                    </div>
                                    <small className="text-muted">Suma total debe ser &lt; 8</small>
                                </div>

                                <div className="col-md-2">
                                    <label className="form-label">SCT (Créditos)</label>
                                    <input type="number" name="sct" className="form-control" min="1" max="7" value={formData.sct} onChange={handleChange} required />
                                </div>

                                <div className="col-md-6">
                                    <label className="form-label">Prerrequisitos (Ctrl+Click para varios)</label>
                                    <select multiple className="form-select" style={{height: '80px'}} value={formData.prerequisiteIds} onChange={handlePrerequisitesChange}>
                                        {availablePrerequisites.length === 0 ? (
                                            <option disabled>No hay asignaturas en semestres anteriores</option>
                                        ) : (
                                            availablePrerequisites.map(s => (
                                                <option key={s.id} value={s.id}>{s.code} - {s.name}</option>
                                            ))
                                        )}
                                    </select>
                                    <small className="text-muted">Máx 3. Solo se muestran asignaturas de semestres anteriores.</small>
                                </div>

                                <div className="col-12 text-end">
                                    <button type="submit" className="btn btn-primary" disabled={submitting}>
                                        {submitting ? 'Guardando...' : 'Registrar Asignatura'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div className="card shadow-sm">
                        <div className="card-body p-0">
                            <table className="table table-hover table-striped mb-0 align-middle">
                                <thead className="table-dark">
                                <tr>
                                    <th>Sem</th>
                                    <th>Código</th>
                                    <th>Nombre</th>
                                    <th>TEL</th>
                                    <th>SCT</th>
                                    <th className="text-center">Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                {loading ? (
                                    <tr><td colSpan="6" className="text-center py-4">Cargando asignaturas...</td></tr>
                                ) : subjects.length === 0 ? (
                                    <tr><td colSpan="6" className="text-center py-4 text-muted">No hay asignaturas registradas en este plan.</td></tr>
                                ) : (
                                    subjects.sort((a,b) => a.semester - b.semester).map((subject) => (
                                        <tr key={subject.id}>
                                            <td><span className="badge bg-secondary">{subject.semester}</span></td>
                                            <td><strong>{subject.code}</strong></td>
                                            <td>{subject.name}</td>
                                            <td>{subject.tel}</td>
                                            <td>{subject.sct}</td>
                                            <td className="text-center">
                                                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(subject.id, subject.name)}>
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
                </>
            )}
        </div>
    );
}