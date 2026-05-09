import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { Loader2, User, ArrowLeft, Save, X } from 'lucide-react';

export default function EditProfile() {
    const navigate = useNavigate();
    const { user, isAuthenticated, login } = useAuth();
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        about: ''
    });

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { replace: true });
            return;
        }

        const fetchProfile = async () => {
            try {
                const profile = await userService.fetchCurrentUser();
                setFormData({
                    firstName: profile.firstName || '',
                    lastName: profile.lastName || '',
                    about: profile.about || ''
                });
                setLoading(false);
            } catch (err) {
                console.error('Failed to fetch profile:', err);
                setError('Failed to load profile data');
                setLoading(false);
            }
        };

        fetchProfile();
    }, [isAuthenticated, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear errors when user types
        if (error) setError(null);
        if (success) setSuccess(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // Validation
        if (!formData.firstName.trim() || formData.firstName.length < 3) {
            setError('First name must be at least 3 characters');
            return;
        }
        if (!formData.lastName.trim() || formData.lastName.length < 3) {
            setError('Last name must be at least 3 characters');
            return;
        }
        if (formData.about && formData.about.length > 500) {
            setError('About section cannot exceed 500 characters');
            return;
        }

        setSubmitting(true);
        setError(null);

        try {
            const updatedProfile = await userService.updateProfile(formData);
            
            // Update the user context with new data
            const updatedUser = {
                ...user,
                name: `${updatedProfile.firstName} ${updatedProfile.lastName}`,
                firstName: updatedProfile.firstName,
                lastName: updatedProfile.lastName,
                about: updatedProfile.about
            };
            login(updatedUser);
            
            setSuccess(true);
            setTimeout(() => {
                navigate('/', { replace: true });
            }, 1500);
        } catch (err) {
            console.error('Failed to update profile:', err);
            setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <Link to="/" className="inline-flex items-center text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 mb-8 transition-colors group">
                <ArrowLeft className="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
                Back to Home
            </Link>

            <div className="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
                <div className="h-2 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500"></div>
                
                <div className="p-8 md:p-12">
                    <div className="flex items-center gap-4 mb-8">
                        <div className="h-16 w-16 bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900/50 dark:to-purple-900/50 rounded-full flex items-center justify-center border-2 border-white dark:border-slate-700 shadow-sm ring-2 ring-indigo-50 dark:ring-indigo-900/30">
                            <User className="h-8 w-8 text-indigo-500 dark:text-indigo-400" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Edit Profile</h1>
                            <p className="text-slate-500 dark:text-slate-400">Update your personal information</p>
                        </div>
                    </div>

                    {error && (
                        <div className="mb-6 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-4 rounded-xl border border-red-100 dark:border-red-800">
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="mb-6 bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 p-4 rounded-xl border border-green-100 dark:border-green-800">
                            Profile updated successfully! Redirecting...
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="firstName" className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">
                                First Name <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                id="firstName"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                disabled={submitting}
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors disabled:opacity-50"
                                placeholder="Enter your first name"
                                required
                                minLength={3}
                                maxLength={20}
                            />
                            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                                {formData.firstName.length}/20 characters
                            </p>
                        </div>

                        <div>
                            <label htmlFor="lastName" className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">
                                Last Name <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                id="lastName"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                disabled={submitting}
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors disabled:opacity-50"
                                placeholder="Enter your last name"
                                required
                                minLength={3}
                                maxLength={20}
                            />
                            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                                {formData.lastName.length}/20 characters
                            </p>
                        </div>

                        <div>
                            <label htmlFor="about" className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">
                                About / Bio
                            </label>
                            <textarea
                                id="about"
                                name="about"
                                value={formData.about}
                                onChange={handleChange}
                                disabled={submitting}
                                rows={5}
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none disabled:opacity-50"
                                placeholder="Tell us about yourself..."
                                maxLength={500}
                            />
                            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                                {formData.about.length}/500 characters
                            </p>
                        </div>

                        <div className="flex items-center gap-4 pt-4">
                            <button
                                type="submit"
                                disabled={submitting}
                                className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-bold rounded-xl transition-colors disabled:cursor-not-allowed shadow-lg hover:shadow-xl"
                            >
                                {submitting ? (
                                    <>
                                        <Loader2 className="w-5 h-5 animate-spin" />
                                        Saving...
                                    </>
                                ) : (
                                    <>
                                        <Save className="w-5 h-5" />
                                        Save Changes
                                    </>
                                )}
                            </button>
                            <button
                                type="button"
                                onClick={() => navigate('/')}
                                disabled={submitting}
                                className="inline-flex items-center gap-2 px-6 py-3 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 font-medium transition-colors disabled:opacity-50"
                            >
                                <X className="w-5 h-5" />
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
