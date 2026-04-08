import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { UploadCloud, CheckCircle, CreditCard, LogOut, FileText, ArrowRight } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import api from '../api/axiosConfig';

export default function UserDashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [fetchingStatus, setFetchingStatus] = useState(true);
  const [kycStatus, setKycStatus] = useState('NOT_SUBMITTED');
  const [formData, setFormData] = useState({ pan: '', aadhaar: '', address: '', dob: '', occupation: '' });
  const [documents, setDocuments] = useState(null);

  useEffect(() => {
    // 1. Fetch real status from DB on load
    checkActualKycStatus();

    // 2. Check if we just returned from Stripe
    const query = new URLSearchParams(location.search);
    const sessionId = query.get('session_id');
    if (sessionId) {
      verifyStripePayment(sessionId);
    }
  }, [location]);

  const checkActualKycStatus = async () => {
    try {
      const response = await api.get('/user/kyc/status');

      if (response.status === 204) {
        // User hasn't submitted yet
        setKycStatus('NOT_SUBMITTED');
      } else {
        // We got data back! Let's check the exact state.
        const actualStatus = response.data.status; // 'PENDING', 'APPROVED', 'REJECTED'
        const isFunded = response.data.isFunded;   // true or false

        if (isFunded) {
          setKycStatus('FUNDED');
        } else {
          setKycStatus(actualStatus);
        }
      }
    } catch (err) {
      console.error("Failed to fetch KYC status", err);
    } finally {
      setFetchingStatus(false);
    }
  };

  const verifyStripePayment = async (sessionId) => {
    try {
      await api.get(`/user/payment/success?session_id=${sessionId}`);
      // Refresh the page state from the backend after payment verification
      checkActualKycStatus();
    } catch (err) {
      console.error(err);
    }
  };

  const handleKycSubmit = async (e) => {
    e.preventDefault(); setLoading(true);
    const data = new FormData();
    Object.keys(formData).forEach(key => data.append(key, formData[key]));
    if (documents) Array.from(documents).forEach(file => data.append('documents', file));

    try {
      await api.post('/user/kyc/submit', data, { headers: { 'Content-Type': 'multipart/form-data' } });
      setKycStatus('PENDING');
    } catch (err) {
      alert("Submission failed");
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async () => {
    setLoading(true);
    try {
      const res = await api.post('/user/payment/initiate');
      window.location.href = res.data.paymentLink;
    } catch (err) {
      setLoading(false);
      alert(err.response?.data?.message || 'Payment initiation failed.');
    }
  };

  // Animation variants
  const pageVariant = {
    initial: { opacity: 0, scale: 0.95, y: 20 },
    animate: { opacity: 1, scale: 1, y: 0, transition: { duration: 0.5, type: 'spring' } },
    exit: { opacity: 0, scale: 1.05, transition: { duration: 0.3 } }
  };

  return (
    <div className="min-h-screen p-6 relative overflow-hidden">
      {/* Navbar */}
      <motion.div initial={{ y: -50, opacity: 0 }} animate={{ y: 0, opacity: 1 }} className="max-w-5xl mx-auto flex justify-between items-center mb-10 glass-panel p-6 rounded-3xl mt-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Identity Portal</h1>
          <p className="text-slate-500 text-sm font-medium">Secure User Dashboard</p>
        </div>
        <button onClick={() => { localStorage.clear(); navigate('/login'); }} className="px-5 py-2.5 bg-slate-100 hover:bg-red-50 text-slate-600 hover:text-red-600 rounded-xl flex items-center gap-2 font-bold transition-all cursor-pointer">
          <LogOut className="w-4 h-4" /> Sign Out
        </button>
      </motion.div>

      <div className="max-w-3xl mx-auto relative">
        {fetchingStatus ? (
          <div className="glass-panel p-20 rounded-3xl flex flex-col items-center justify-center text-slate-500">
             <div className="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mb-4"></div>
             <p className="font-bold">Syncing profile securely...</p>
          </div>
        ) : (
          <AnimatePresence mode="wait">

            {kycStatus === 'NOT_SUBMITTED' && (
              <motion.div key="form" variants={pageVariant} initial="initial" animate="animate" exit="exit" className="glass-panel rounded-3xl overflow-hidden">
                <div className="bg-gradient-to-r from-slate-900 to-slate-800 px-10 py-8">
                  <h2 className="text-2xl font-bold text-white flex items-center gap-3">
                    <FileText className="w-7 h-7 text-brand-400" /> Complete your KYC
                  </h2>
                  <p className="text-slate-300 mt-2">Government regulations require us to verify your identity.</p>
                </div>

                <form onSubmit={handleKycSubmit} className="p-10 space-y-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div>
                      <label className="block text-sm font-bold text-slate-700 mb-2">PAN Number</label>
                      <input type="text" required placeholder="ABCDE1234F" className="w-full px-5 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:bg-white transition-all" value={formData.pan} onChange={e => setFormData({...formData, pan: e.target.value})} />
                    </div>
                    <div>
                      <label className="block text-sm font-bold text-slate-700 mb-2">Aadhaar Number</label>
                      <input type="text" required placeholder="1234 5678 9012" className="w-full px-5 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:bg-white transition-all" value={formData.aadhaar} onChange={e => setFormData({...formData, aadhaar: e.target.value})} />
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div>
                      <label className="block text-sm font-bold text-slate-700 mb-2">Date of Birth</label>
                      <input type="date" required className="w-full px-5 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:bg-white transition-all" value={formData.dob} onChange={e => setFormData({...formData, dob: e.target.value})} />
                    </div>
                    <div>
                      <label className="block text-sm font-bold text-slate-700 mb-2">Occupation</label>
                      <input type="text" required placeholder="Engineer" className="w-full px-5 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:bg-white transition-all" value={formData.occupation} onChange={e => setFormData({...formData, occupation: e.target.value})} />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-slate-700 mb-2">Full Address</label>
                    <textarea required rows="2" className="w-full px-5 py-3.5 bg-slate-50/50 border border-slate-200 rounded-2xl focus:outline-hidden focus:ring-3 focus:ring-brand-600/20 focus:bg-white transition-all" value={formData.address} onChange={e => setFormData({...formData, address: e.target.value})}></textarea>
                  </div>

                  <div className="pt-6 border-t border-slate-100">
                    <div className="border-2 border-dashed border-slate-300 rounded-3xl p-10 text-center bg-slate-50 hover:bg-brand-50 hover:border-brand-300 transition-all relative group">
                      <motion.div whileHover={{ scale: 1.1 }} className="w-16 h-16 bg-white shadow-sm rounded-full flex items-center justify-center mx-auto mb-4 group-hover:text-brand-600 text-slate-400">
                        <UploadCloud className="w-8 h-8" />
                      </motion.div>
                      <p className="font-bold text-slate-700 mb-1">Upload ID Documents</p>
                      <p className="text-sm text-slate-500">Drag & drop or click to browse</p>
                      <input type="file" multiple required className="absolute inset-0 w-full h-full opacity-0 cursor-pointer" onChange={e => setDocuments(e.target.files)} />
                      {documents && <div className="mt-4 inline-block px-4 py-1.5 bg-brand-100 text-brand-700 font-bold rounded-full text-sm">{documents.length} File(s) attached</div>}
                    </div>
                  </div>

                  <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} type="submit" disabled={loading} className="w-full py-4 bg-gradient-to-r from-brand-600 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 text-white font-bold rounded-2xl shadow-xl shadow-brand-500/30 flex justify-center items-center gap-2 cursor-pointer">
                    {loading ? 'Processing securely...' : 'Submit Identity Data'} <ArrowRight className="w-5 h-5" />
                  </motion.button>
                </form>
              </motion.div>
            )}

            {kycStatus === 'PENDING' && (
              <motion.div key="pending" variants={pageVariant} initial="initial" animate="animate" exit="exit" className="glass-panel p-16 rounded-3xl text-center">
                <motion.div animate={{ rotate: 360 }} transition={{ duration: 8, repeat: Infinity, ease: "linear" }} className="w-32 h-32 bg-amber-50 rounded-full border-4 border-amber-100 flex items-center justify-center mx-auto mb-8 relative">
                  <UploadCloud className="w-12 h-12 text-amber-500 absolute" />
                </motion.div>
                <h2 className="text-3xl font-extrabold text-slate-900 mb-4">Verification in Progress</h2>
                <p className="text-slate-500 text-lg leading-relaxed max-w-md mx-auto">Our compliance team is securely reviewing your documents. Please wait for Admin approval.</p>
              </motion.div>
            )}

            {kycStatus === 'APPROVED' && (
              <motion.div key="approved" variants={pageVariant} initial="initial" animate="animate" exit="exit" className="glass-panel p-16 rounded-3xl text-center overflow-hidden relative">
                <div className="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-emerald-400 to-teal-500"></div>
                <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ type: "spring", delay: 0.2 }} className="w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-8 shadow-xl shadow-emerald-200/50">
                  <CheckCircle className="w-12 h-12 text-emerald-600" />
                </motion.div>
                <h2 className="text-3xl font-extrabold text-slate-900 mb-4">Identity Verified!</h2>
                <p className="text-slate-500 text-lg mb-10 max-w-md mx-auto">You're almost there. Make your initial account deposit of <span className="font-bold text-slate-900">$50.00</span> to activate your wallet features.</p>

                <motion.button whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }} onClick={handlePayment} disabled={loading} className="py-4 px-10 bg-slate-900 hover:bg-slate-800 text-white font-bold rounded-2xl shadow-2xl shadow-slate-900/30 flex items-center justify-center gap-3 mx-auto text-lg cursor-pointer">
                  <CreditCard className="w-6 h-6" /> {loading ? 'Securing Connection...' : 'Deposit via Stripe'}
                </motion.button>
              </motion.div>
            )}

            {kycStatus === 'FUNDED' && (
              <motion.div key="funded" variants={pageVariant} initial="initial" animate="animate" className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-16 rounded-3xl text-center shadow-2xl relative overflow-hidden">
                <div className="absolute -top-40 -right-40 w-80 h-80 bg-emerald-500/20 rounded-full blur-3xl"></div>
                <motion.div initial={{ scale: 0.5, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ type: "spring", bounce: 0.5 }} className="relative z-10 w-28 h-28 bg-gradient-to-tr from-emerald-400 to-teal-400 rounded-full flex items-center justify-center mx-auto mb-8 shadow-2xl shadow-emerald-500/40">
                  <CheckCircle className="w-14 h-14 text-white" />
                </motion.div>
                <h2 className="text-4xl font-extrabold text-white mb-4 relative z-10 tracking-tight">Wallet Active</h2>
                <p className="text-slate-300 text-lg relative z-10 font-medium">Your account is fully verified, funded, and ready to dominate.</p>
              </motion.div>
            )}
          </AnimatePresence>
        )}
      </div>
    </div>
  );
}