import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function RegistrationsView() {
    const [currentStudentId, setCurrentStudentId] = useState(null);
    const [currentTermId, setCurrentTermId] = useState(null);

    const [sections, setSections] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [myRegistrations, setMyRegistrations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [selectedSectionId, setSelectedSectionId] = useState(null);

    const fetchMyRegistrations = () => {
        if (!currentStudentId || !currentTermId) return;
        apiClient.get(`/registrations?studentId=${currentStudentId}&termId=${currentTermId}`)
            .then(res => setMyRegistrations(res.data))
            .catch(err => console.error("Error al actualizar la tabla de inscripciones", err));
    };

    useEffect(() => {
        setLoading(true);

        Promise.all([
            apiClient.get('/students/me'),
            apiClient.get('/academic-terms/current')
        ])
            .then(([studentRes, termsRes]) => {
                const student = studentRes.data;

                const activeTerm = termsRes.data;

                if (!student || !student.id) {
                    alert("No se pudo identificar tu usuario.");
                    return;
                }

                const activeTermId = activeTerm.id;
                setCurrentStudentId(student.id);
                setCurrentTermId(activeTermId);

                return Promise.all([
                    apiClient.get('/sections'),
                    apiClient.get('/subjects'),
                    apiClient.get(`/registrations?studentId=${student.id}&termId=${activeTermId}`)
                ]).then(([sectionsRes, subjectsRes, regRes]) => {
                    const currentTermSections = sectionsRes.data.filter(sec => sec.academicTermId === activeTermId);
                    setSections(currentTermSections);
                    setSubjects(subjectsRes.data);
                    setMyRegistrations(regRes.data);
                });
            })
            .catch(err => {
                console.error(err);
                alert("Error al cargar la información académica. Revisa tu sesión.");
            })
            .finally(() => setLoading(false));
    }, []);

    const handleEnroll = () => {
        if (!selectedSectionId) return;
        setSubmitting(true);

        const payload = {
            studentId: currentStudentId,
            sectionId: selectedSectionId
        };

        apiClient.post('/registrations', payload)
            .then(() => {
                alert('¡Asignatura inscrita con éxito!');
                setSelectedSectionId(null);
                fetchMyRegistrations();
            })
            .catch(err => {
                alert(err.response?.data?.message || 'Error al inscribir la asignatura. Verifica choques de horario o prerrequisitos.');
            })
            .finally(() => setSubmitting(false));
    };

    const handleUnenroll = (registrationId) => {
        if (!window.confirm('¿Estás seguro de desinscribir esta asignatura?')) return;
        apiClient.delete(`/registrations/${registrationId}`)
            .then(() => {
                setMyRegistrations(prev => prev.filter(r => r.id !== registrationId));
            })
            .catch(err => alert(err.response?.data?.message || 'Error al anular la inscripción.'));
    };

    const getSubjectName = (subjectId) => {
        const sub = subjects.find(s => s.id === subjectId);
        return sub ? `${sub.code} - ${sub.name}` : 'Asignatura Desconocida';
    };

    const isAlreadyEnrolled = (sectionId) => {
        return myRegistrations.some(reg => reg.sectionId === sectionId);
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-laptop me-2"></i>Toma de Ramos</h2>

            <div className="row">
                <div className="col-lg-5 mb-4">
                    <div className="card shadow-sm border-primary h-100">
                        <div className="card-header bg-primary text-white">
                            <strong>Asignaturas Disponibles</strong>
                        </div>
                        <div className="card-body p-0">
                            {loading ? (
                                <div className="p-4 text-center text-muted">Cargando oferta...</div>
                            ) : sections.length === 0 ? (
                                <div className="p-4 text-center text-muted">No hay asignaturas ofertadas en este periodo.</div>
                            ) : (
                                <div className="list-group list-group-flush">
                                    {sections.map(sec => {
                                        const enrolled = isAlreadyEnrolled(sec.id);
                                        const isSelected = selectedSectionId === sec.id;

                                        return (
                                            <button
                                                key={sec.id}
                                                type="button"
                                                className={`list-group-item list-group-item-action ${isSelected ? 'active' : ''} ${enrolled ? 'list-group-item-light text-muted' : ''}`}
                                                onClick={() => !enrolled && setSelectedSectionId(sec.id)}
                                                disabled={enrolled}
                                            >
                                                <div className="d-flex justify-content-between align-items-center mb-1">
                                                    <strong>{getSubjectName(sec.subjectId)}</strong>
                                                    {enrolled && <span className="badge bg-secondary">Inscrita</span>}
                                                </div>
                                                <small className={isSelected ? 'text-light' : 'text-muted'}>
                                                    Sección ID: {sec.id} | Cupos: {sec.maxCapacity} | Horario: {Array.isArray(sec.schedule) ? sec.schedule.join(' - ') : sec.schedule}
                                                </small>
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                        <div className="card-footer bg-light text-end">
                            <button
                                className="btn btn-success"
                                disabled={!selectedSectionId || submitting}
                                onClick={handleEnroll}
                            >
                                {submitting ? (
                                    <span><span className="spinner-border spinner-border-sm me-2" />Procesando...</span>
                                ) : (
                                    <span><i className="bi bi-check2-circle me-2"></i>Inscribir Asignatura</span>
                                )}
                            </button>
                        </div>
                    </div>
                </div>

                <div className="col-lg-7">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-dark text-white">
                            <strong>Mi Carga Académica Actual</strong>
                        </div>
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table table-hover table-striped mb-0 align-middle">
                                    <thead className="table-light">
                                    <tr>
                                        <th>Asignatura</th>
                                        <th>Sección</th>
                                        <th>Horario</th>
                                        <th className="text-end">Acciones</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {loading ? (
                                        <tr><td colSpan="4" className="text-center py-4">Cargando carga académica...</td></tr>
                                    ) : myRegistrations.length === 0 ? (
                                        <tr><td colSpan="4" className="text-center py-4 text-muted">Aún no has inscrito ninguna asignatura.</td></tr>
                                    ) : (
                                        myRegistrations.map(reg => {
                                            const section = sections.find(s => s.id === reg.sectionId);
                                            const schedule = section ? (Array.isArray(section.schedule) ? section.schedule.join(' - ') : section.schedule) : '';

                                            return (
                                                <tr key={reg.id}>
                                                    <td><strong>{getSubjectName(section?.subjectId)}</strong></td>
                                                    <td>Sec. {reg.sectionId}</td>
                                                    <td><span className="badge bg-light text-dark border">{schedule}</span></td>
                                                    <td className="text-end">
                                                        <button
                                                            className="btn btn-outline-danger btn-sm"
                                                            onClick={() => handleUnenroll(reg.id)}
                                                            title="Desinscribir"
                                                        >
                                                            <i className="bi bi-x-lg me-1"></i>Anular
                                                        </button>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}