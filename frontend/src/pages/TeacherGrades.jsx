import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function TeacherGradesView() {
    const [currentTeacherId, setCurrentTeacherId] = useState(null);
    const [currentTermId, setCurrentTermId] = useState(null);

    const [sections, setSections] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [students, setStudents] = useState([]);

    const [selectedSectionId, setSelectedSectionId] = useState(null);
    const [sectionRegistrations, setSectionRegistrations] = useState([]);
    const [grades, setGrades] = useState({});

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        setLoading(true);
        Promise.all([
            apiClient.get('/teachers/me'),
            apiClient.get('/academic-terms/current'),
            apiClient.get('/subjects'),
            apiClient.get('/students')
        ]).then(([teacherRes, termRes, subjectsRes, studentsRes]) => {
            const teacher = teacherRes.data;
            const term = termRes.data;

            if (teacher && teacher.id && term && term.id) {
                setCurrentTeacherId(teacher.id);
                setCurrentTermId(term.id);
                setSubjects(subjectsRes.data);
                setStudents(studentsRes.data);

                apiClient.get('/sections').then(sectionsRes => {
                    const myActiveSections = sectionsRes.data.filter(
                        sec => sec.teacherId === teacher.id && sec.academicTermId === term.id
                    );
                    setSections(myActiveSections);
                });
            }
        }).catch(err => {
            alert("Error al cargar la información del docente.");
        }).finally(() => setLoading(false));
    }, []);

    const handleSelectSection = (sectionId) => {
        setSelectedSectionId(sectionId);
        setSectionRegistrations([]);
        setGrades({});

        apiClient.get(`/registrations?sectionId=${sectionId}`)
            .then(res => {
                setSectionRegistrations(res.data);
                const initialGrades = {};
                res.data.forEach(reg => {
                    initialGrades[reg.id] = reg.grade || '';
                });
                setGrades(initialGrades);
            })
            .catch(err => alert("Error al cargar los estudiantes de esta sección."));
    };

    const handleGradeChange = (registrationId, value) => {
        setGrades(prev => ({
            ...prev,
            [registrationId]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setSubmitting(true);

        const gradesArray = Object.keys(grades).map(regId => ({
            registrationId: Number(regId),
            grade: parseFloat(grades[regId])
        }));

        const payload = {
            teacherId: currentTeacherId,
            grades: gradesArray
        };

        apiClient.post(`/registrations/section/${selectedSectionId}/grades`, payload)
            .then(() => {
                alert("Calificaciones guardadas con éxito.");
            })
            .catch(err => {
                alert(err.response?.data?.message || "Error al guardar calificaciones.");
            })
            .finally(() => setSubmitting(false));
    };

    const getSubjectName = (subjectId) => {
        const sub = subjects.find(s => s.id === subjectId);
        return sub ? `${sub.code} - ${sub.name}` : 'Desconocida';
    };

    const getStudentName = (studentId) => {
        const stu = students.find(s => s.id === studentId);
        return stu ? stu.fullName : `ID: ${studentId}`;
    };

    const getStudentRun = (studentId) => {
        const stu = students.find(s => s.id === studentId);
        return stu ? stu.run : '';
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-journal-text me-2"></i>Ingreso de Calificaciones</h2>

            <div className="row">
                <div className="col-lg-4 mb-4">
                    <div className="card shadow-sm border-primary h-100">
                        <div className="card-header bg-primary text-white">
                            <strong>Mis Secciones Asignadas</strong>
                        </div>
                        <div className="card-body p-0">
                            {loading ? (
                                <div className="p-4 text-center text-muted">Cargando secciones...</div>
                            ) : sections.length === 0 ? (
                                <div className="p-4 text-center text-muted">No tienes secciones asignadas este semestre.</div>
                            ) : (
                                <div className="list-group list-group-flush">
                                    {sections.map(sec => {
                                        const isSelected = selectedSectionId === sec.id;
                                        return (
                                            <button
                                                key={sec.id}
                                                type="button"
                                                className={`list-group-item list-group-item-action ${isSelected ? 'active' : ''}`}
                                                onClick={() => handleSelectSection(sec.id)}
                                            >
                                                <div className="d-flex justify-content-between align-items-center mb-1">
                                                    <strong>{getSubjectName(sec.subjectId)}</strong>
                                                </div>
                                                <small className={isSelected ? 'text-light' : 'text-muted'}>
                                                    Sección: {sec.id} | Horario: {Array.isArray(sec.schedule) ? sec.schedule.join(' - ') : sec.schedule}
                                                </small>
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="col-lg-8">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-dark text-white">
                            <strong>Listado de Estudiantes</strong>
                        </div>
                        <div className="card-body p-0">
                            {!selectedSectionId ? (
                                <div className="p-4 text-center text-muted">Selecciona una sección de la lista para ingresar notas.</div>
                            ) : (
                                <form onSubmit={handleSubmit}>
                                    <div className="table-responsive">
                                        <table className="table table-hover table-striped mb-0 align-middle">
                                            <thead className="table-light">
                                            <tr>
                                                <th>RUN</th>
                                                <th>Nombre</th>
                                                <th className="text-end" style={{ width: '150px' }}>Calificación</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {sectionRegistrations.length === 0 ? (
                                                <tr><td colSpan="3" className="text-center py-4 text-muted">No hay estudiantes inscritos en esta sección.</td></tr>
                                            ) : (
                                                sectionRegistrations.map(reg => (
                                                    <tr key={reg.id}>
                                                        <td>{getStudentRun(reg.studentId)}</td>
                                                        <td><strong>{getStudentName(reg.studentId)}</strong></td>
                                                        <td className="text-end">
                                                            <input
                                                                type="number"
                                                                className="form-control form-control-sm"
                                                                min="1.0"
                                                                max="7.0"
                                                                step="0.1"
                                                                value={grades[reg.id] || ''}
                                                                onChange={(e) => handleGradeChange(reg.id, e.target.value)}
                                                                required
                                                            />
                                                        </td>
                                                    </tr>
                                                ))
                                            )}
                                            </tbody>
                                        </table>
                                    </div>
                                    {sectionRegistrations.length > 0 && (
                                        <div className="card-footer bg-light text-end">
                                            <button type="submit" className="btn btn-success" disabled={submitting}>
                                                {submitting ? 'Guardando...' : 'Guardar Calificaciones'}
                                            </button>
                                        </div>
                                    )}
                                </form>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}