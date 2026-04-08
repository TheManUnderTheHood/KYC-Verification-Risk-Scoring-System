import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Shield, Mail, Lock, AlertCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import api from '../api/axiosConfig';

export default function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const response = await api.post('/auth/login', formData);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('role', response.data.role);
      navigate(response.data.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      {/* Animated Glass Card */}
      <motion.div
        initial={{ opacity: 0, y: 30, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.5, type: 'spring', bounce: 0.4 }}
        className="max-w-md w-full glass-panel rounded-3xl p-10 relative overflow-hidden"
      >
        {/* Decorative background glow */}
        <div className="absolute -top-20 -right-20 w-40 h-40 bg-blue-500/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-indigo-500/10 rounded-full blur-3xl"></div>

        <div className="flex flex-col items-center mb-10 relative z-10">
          <motion.div
            initial={{ rotate: -180, opacity: 0 }}
            animate={{ rotate: 0, opacity: 1 }}
            transition={{ duration: 0.7, delay: 0.2 }}
            className="bg-gradient-to-tr from-brand-600 to-indigo-500 p-4 rounded-2xl mb-5 shadow-lg shadow-brand-500/30"
          >
            <Shield className="w-8 h-8 text-white" />
          </motion.div>
          <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">Welcome Back</h2>
          <p className="text-slate-500 text-sm mt-2">Access your Compliance Dashboard</p>
        </div>

        {error && (
          <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} className="mb-6 p-4 bg-red-50/80 backdrop-blur-md border border-red-200 rounded-2xl flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
            <p className="text-sm text-red-700 font-medium">{error}</p>
          </motion.div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
          <div className="group">
            <label className="block text-sm font-bold text-slate-700 mb-2 transition-colors group-focus-within:text-brand-600">Email Address</label>
            <div className="relative">
              <Mail className="w-5 h-5 text-slate-400 absolute left-4 top-3.5 transition-colors group-focus-within:text-brand-600" />
              <input type="email" required className="w-full pl-12 pr-4 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:border-brand-600 transition-all focus:bg-white" placeholder="you@example.com" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} />
            </div>
          </div>

          <div className="group">
            <label className="block text-sm font-bold text-slate-700 mb-2 transition-colors group-focus-within:text-brand-600">Password</label>
            <div className="relative">
              <Lock className="w-5 h-5 text-slate-400 absolute left-4 top-3.5 transition-colors group-focus-within:text-brand-600" />
              <input type="password" required className="w-full pl-12 pr-4 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:border-brand-600 transition-all focus:bg-white" placeholder="••••••••" value={formData.password} onChange={(e) => setFormData({ ...formData, password: e.target.value })} />
            </div>
          </div>

          <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} type="submit" disabled={loading} className="w-full py-4 px-4 bg-gradient-to-r from-slate-900 to-slate-800 hover:from-slate-800 hover:to-slate-700 text-white font-bold rounded-2xl shadow-xl shadow-slate-900/20 transition-all flex justify-center items-center cursor-pointer disabled:opacity-70 mt-4">
            {loading ? 'Authenticating...' : 'Secure Log In'}
          </motion.button>
        </form>

        <p className="mt-8 text-center text-sm text-slate-500 relative z-10">
          Don't have an account? <Link to="/register" className="font-bold text-brand-600 hover:text-indigo-600 transition-colors">Register here</Link>
        </p>
      </motion.div>
    </div>
  );
}