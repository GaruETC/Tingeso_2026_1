import React, { useEffect, useState } from 'react';
import apiClient from '../services/http-common';

export default function StudentHistoryView() {
    const [currentStudentId, setCurrentStudentId] = useState(null);
    const [records, setRecords] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);

        apiClient.get('/students/me')
            .then(studentRes => {
                const student = studentRes.data;
                if (!student || !student.id) {
                    alert("No se pudo identificar tu usuario.");
                    return Promise.reject("No student ID");
                }

                setCurrentStudentId(student.id);

                return Promise.all([
                    apiClient.get('/subjects'),
                    apiClient.get(`/academic-historial/student/${student.id}`)
                ]);
            })
            .then(([subjectsRes, recordsRes]) => {
                setSubjects(subjectsRes.data);
                setRecords(recordsRes.data);
            })
            .catch(err => {
                console.error(err);
                alert("Error al cargar el historial académico.");
            })
            .finally(() => setLoading(false));
    }, []);

    const getSubject = (subjectId) => {
        return subjects.find(s => s.id === subjectId) || {};
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4"><i className="bi bi-clock-history me-2"></i>Mi Historial Académico</h2>

            <div className="card shadow-sm border-success">
                <div className="card-header bg-success text-white">
                    <strong>Asignaturas Aprobadas</strong>
                </div>
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0 align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Código</th>
                                <th>Asignatura</th>
                                <th className="text-center">Nota Final</th>
                                <th className="text-center">Estado</th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr><td colSpan="4" className="text-center py-4">Cargando historial...</td></tr>
                            ) : records.length === 0 ? (
                                <tr><td colSpan="4" className="text-center py-4 text-muted">Aún no tienes asignaturas aprobadas registradas.</td></tr>
                            ) : (
                                records.map(record => {
                                    const subject = getSubject(record.subjectId);
                                    return (
                                        <tr key={record.id}>
                                            <td><span className="badge bg-secondary">{subject.code || '-'}</span></td>
                                            <td><strong>{subject.name || 'Desconocida'}</strong></td>
                                            <td className="text-center">
                                                    <span className="badge bg-primary fs-6">
                                                        {record.finalGrade ? record.finalGrade.toFixed(1) : '-'}
                                                    </span>
                                            </td>
                                            {record.status == "APROBADO" ?
                                                <td className="text-center">
                                                    <span className="text-success fw-bold">
                                                        <i className="bi bi-check-circle-fill me-1"></i>{record.status}
                                                    </span>
                                                </td>
                                                :
                                                <td className="text-center">
                                                    <span className="text-danger fw-bold">
                                                        <i className="bi bi-x-circle-fill me-1"></i>{record.status}
                                                    </span>
                                                </td>
                                            }
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
    );
}