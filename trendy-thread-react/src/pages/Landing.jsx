import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BookOpen, PenTool, Users, Zap } from 'lucide-react';

export default function Landing() {
    return (
        <div className="bg-white dark:bg-slate-900 transition-colors duration-300">
            {/* Hero Section */}
            <div className="relative overflow-hidden pt-16 pb-32">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="relative z-10 text-center lg:text-left lg:grid lg:grid-cols-2 lg:items-center lg:gap-8">
                        <div>
                            <h1 className="text-5xl font-extrabold tracking-tight text-slate-900 dark:text-white sm:text-6xl md:text-7xl mb-6">
                                Share your <span className="text-indigo-600 dark:text-indigo-400">ideas</span> with the world.
                            </h1>
                            <p className="mt-4 text-xl text-slate-500 dark:text-slate-400 max-w-2xl mx-auto lg:mx-0">
                                TrendyThread is the space where creativity meets community. Write, read, and connect with millions of users sharing their stories.
                            </p>
                            <div className="mt-10 flex flex-col sm:flex-row justify-center lg:justify-start gap-4">
                                <Link to="/signup" className="flex items-center justify-center px-8 py-4 border border-transparent text-base font-bold rounded-2xl text-white bg-indigo-600 hover:bg-indigo-700 shadow-xl shadow-indigo-200 dark:shadow-none transition-all hover:scale-105">
                                    Get Started Free
                                    <ArrowRight className="ml-2 w-5 h-5" />
                                </Link>
                                <Link to="/login" className="flex items-center justify-center px-8 py-4 border border-slate-200 dark:border-slate-700 text-base font-bold rounded-2xl text-slate-900 dark:text-white bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 transition-all">
                                    Sign In
                                </Link>
                            </div>
                        </div>
                        <div className="mt-16 lg:mt-0 relative">
                            <div className="bg-gradient-to-tr from-indigo-100 to-purple-100 dark:from-indigo-900/20 dark:to-purple-900/20 rounded-[3rem] p-8 relative overflow-hidden">
                                <img
                                    src="https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&q=80&w=800"
                                    alt="Creative workspace"
                                    className="rounded-2xl shadow-2xl"
                                />
                                <div className="absolute -bottom-6 -right-6 bg-white dark:bg-slate-800 p-6 rounded-3xl shadow-xl flex items-center gap-4 border border-indigo-50 dark:border-slate-700 animate-bounce">
                                    <div className="bg-green-100 p-3 rounded-full">
                                        <Zap className="w-6 h-6 text-green-600" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-slate-900 dark:text-white">New Post Published</p>
                                        <p className="text-xs text-slate-500">Just now by Alex</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Features Section */}
            <div className="py-24 bg-slate-50 dark:bg-slate-800/50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-extrabold text-slate-900 dark:text-white sm:text-4xl">Everything you need to write</h2>
                        <p className="mt-4 text-lg text-slate-500 dark:text-slate-400">A powerful yet simple platform for modern storytellers.</p>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-100 dark:border-slate-700 shadow-sm hover:shadow-md transition-shadow">
                            <div className="bg-indigo-100 dark:bg-indigo-900/50 w-12 h-12 rounded-xl flex items-center justify-center mb-6">
                                <PenTool className="w-6 h-6 text-indigo-600 dark:text-indigo-400" />
                            </div>
                            <h3 className="text-xl font-bold mb-3 text-slate-900 dark:text-white">Seamless Writing</h3>
                            <p className="text-slate-500 dark:text-slate-400">Our editor is designed to let you focus on what matters most: your words.</p>
                        </div>
                        <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-100 dark:border-slate-700 shadow-sm hover:shadow-md transition-shadow">
                            <div className="bg-purple-100 dark:bg-purple-900/50 w-12 h-12 rounded-xl flex items-center justify-center mb-6">
                                <Users className="w-6 h-6 text-purple-600 dark:text-purple-400" />
                            </div>
                            <h3 className="text-xl font-bold mb-3 text-slate-900 dark:text-white">Growing Community</h3>
                            <p className="text-slate-500 dark:text-slate-400">Join thousands of writers and readers from all over the globe.</p>
                        </div>
                        <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-100 dark:border-slate-700 shadow-sm hover:shadow-md transition-shadow">
                            <div className="bg-blue-100 dark:bg-blue-900/50 w-12 h-12 rounded-xl flex items-center justify-center mb-6">
                                <BookOpen className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                            </div>
                            <h3 className="text-xl font-bold mb-3 text-slate-900 dark:text-white">Rich Insights</h3>
                            <p className="text-slate-500 dark:text-slate-400">Deep dive into categories and discover content tailored to you.</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* CTA Section */}
            <div className="py-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="bg-indigo-600 rounded-[3rem] p-12 md:p-20 text-center relative overflow-hidden">
                        <div className="relative z-10">
                            <h2 className="text-4xl font-extrabold text-white mb-6">Ready to start your journey?</h2>
                            <p className="text-indigo-100 text-xl mb-10 max-w-2xl mx-auto">Join TrendyThread today and start sharing your unique perspective with the world.</p>
                            <Link to="/signup" className="inline-flex items-center px-10 py-4 bg-white text-indigo-600 font-black rounded-2xl hover:bg-indigo-50 transition-all hover:scale-105 shadow-xl">
                                Join the Community
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
