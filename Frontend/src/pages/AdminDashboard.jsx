import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, ShieldAlert, CheckCircle, XCircle, Clock, Search } from 'lucide-react';
import { motion } from 'framer-motion';
import api from '../api/axiosConfig';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('PENDING');

  useEffect(() => {
    fetchApplications();
  }, [filter]);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const response = await api.get(filter === 'PENDING' ? '/admin/kyc/pending' : '/admin/kyc/high-risk');
      setApplications(response.data);
    } catch (err) { console.error(err); } finally { setLoading(false); }
  };

  const handleReview = async (id, status) => {
    try {
      await api.put(`/admin/kyc/${id}/review`, { status, reviewNotes: `Marked as ${status}` });
      setApplications(applications.filter(app => app.id !== id));
    } catch (err) { alert("Failed to review"); }
  };

  // Animation configuration for staggered rows
  const container = { hidden: { opacity: 0 }, show: { opacity: 1, transition: { staggerChildren: 0.1 } } };
  const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0, transition: { type: "spring", stiffness: 300 } } };

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Premium Dark Navbar */}
      <nav className="bg-slate-900 border-b border-slate-800 text-white px-8 py-5 flex justify-between items-center sticky top-0 z-50">
        <div className="flex items-center gap-4">
          <div className="bg-brand-500/20 p-2 rounded-xl border border-brand-500/30">
            <ShieldAlert className="w-6 h-6 text-brand-400" />
          </div>
          <h1 className="text-xl font-bold tracking-wide">Compliance<span className="font-light text-slate-400">HQ</span></h1>
        </div>
        <button onClick={() => { localStorage.clear(); navigate('/login'); }} className="px-5 py-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-sm font-bold transition-colors flex items-center gap-2">
          Logout <LogOut className="w-4 h-4" />
        </button>
      </nav>

      <div className="max-w-7xl mx-auto p-8 mt-4">
        {/* Animated Tabs */}
        <div className="flex gap-4 mb-8">
          <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} onClick={() => setFilter('PENDING')} className={`px-6 py-3.5 rounded-2xl text-sm font-bold flex items-center gap-3 transition-all ${filter === 'PENDING' ? 'bg-white text-slate-900 shadow-xl border border-slate-200' : 'bg-transparent text-slate-500 hover:bg-slate-200/50'}`}>
            <Clock className="w-5 h-5" /> Pending Queue
            {filter === 'PENDING' && <span className="bg-slate-100 text-slate-900 px-2 py-0.5 rounded-md text-xs">{applications.length}</span>}
          </motion.button>

          <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} onClick={() => setFilter('HIGH_RISK')} className={`px-6 py-3.5 rounded-2xl text-sm font-bold flex items-center gap-3 transition-all ${filter === 'HIGH_RISK' ? 'bg-red-600 text-white shadow-xl shadow-red-500/30 border border-red-500' : 'bg-transparent text-slate-500 hover:bg-red-50 hover:text-red-600'}`}>
            <ShieldAlert className="w-5 h-5" /> High Risk Alerts
          </motion.button>
        </div>

        {/* Data Table */}
        <div className="glass-panel rounded-3xl overflow-hidden">
          {loading ? (
            <div className="p-20 flex flex-col items-center justify-center">
              <Search className="w-10 h-10 text-slate-300 animate-pulse mb-4" />
              <p className="text-slate-500 font-bold">Scanning records...</p>
            </div>
          ) : applications.length === 0 ? (
            <div className="p-20 text-center">
              <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
                <CheckCircle className="w-10 h-10 text-emerald-400" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">Queue is empty</h3>
              <p className="text-slate-500 mt-2">All applications have been processed.</p>
            </div>
          ) : (
            <table className="w-full text-left">
              <thead className="bg-slate-50/50 border-b border-slate-200/60">
                <tr className="text-slate-500 text-xs uppercase tracking-wider font-bold">
                  <th className="py-5 px-8">Applicant</th>
                  <th className="py-5 px-8">Identifiers</th>
                  <th className="py-5 px-8">Risk Engine Output</th>
                  <th className="py-5 px-8 text-right">Decision</th>
                </tr>
              </thead>
              <motion.tbody variants={container} initial="hidden" animate="show" className="divide-y divide-slate-100">
                {applications.map((app) => (
                  <motion.tr variants={item} key={app.id} className="hover:bg-slate-50/80 transition-colors group">
                    <td className="py-6 px-8">
                      <p className="font-extrabold text-slate-900 text-base">{app.userName}</p>
                      <p className="text-sm text-slate-500 mb-2">{app.email}</p>
                      <span className="text-xs font-bold text-indigo-700 bg-indigo-50 px-2.5 py-1 rounded-md border border-indigo-100 uppercase tracking-wide">{app.occupation}</span>
                    </td>
                    <td className="py-6 px-8">
                      <div className="space-y-1.5">
                        <p className="text-sm font-mono font-medium text-slate-700 bg-slate-100 px-3 py-1 rounded-lg w-max border border-slate-200">PAN: {app.pan}</p>
                        <p className="text-sm font-mono font-medium text-slate-700 bg-slate-100 px-3 py-1 rounded-lg w-max border border-slate-200">UID: {app.aadhaar}</p>
                      </div>
                    </td>
                    <td className="py-6 px-8">
                      <span className={`px-4 py-2 text-xs font-black uppercase tracking-wider rounded-xl inline-flex items-center gap-2 border ${
                        app.riskLevel === 'HIGH' ? 'bg-red-50 text-red-700 border-red-200' :
                        app.riskLevel === 'MEDIUM' ? 'bg-amber-50 text-amber-700 border-amber-200' :
                        'bg-emerald-50 text-emerald-700 border-emerald-200'
                      }`}>
                        {app.riskLevel === 'HIGH' && <ShieldAlert className="w-4 h-4"/>}
                        {app.riskLevel} RISK — SCORE: {app.riskScore}
                      </span>
                    </td>
                    <td className="py-6 px-8">
                      <div className="flex justify-end gap-3 opacity-80 group-hover:opacity-100 transition-opacity">
                        <motion.button whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }} onClick={() => handleReview(app.id, 'APPROVED')} className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl text-sm font-bold flex items-center gap-2 shadow-lg shadow-emerald-500/20 transition-all">
                          <CheckCircle className="w-4 h-4" /> Approve
                        </motion.button>
                        <motion.button whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }} onClick={() => handleReview(app.id, 'REJECTED')} className="px-5 py-2.5 bg-white border-2 border-red-200 hover:border-red-500 text-red-600 hover:bg-red-50 rounded-xl text-sm font-bold flex items-center gap-2 transition-all">
                          <XCircle className="w-4 h-4" /> Reject
                        </motion.button>
                      </div>
                    </td>
                  </motion.tr>
                ))}
              </motion.tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}