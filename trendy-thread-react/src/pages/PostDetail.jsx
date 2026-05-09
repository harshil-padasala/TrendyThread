import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { postService } from '../services/postService';
import { commentService } from '../services/commentService';
import { useAuth } from '../context/AuthContext';
import { Loader2, User, Clock, MessageSquare, ArrowLeft, Send, Edit2, X, Check, Save } from 'lucide-react';

export default function PostDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuth();
    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Comment form state
    const [commentContent, setCommentContent] = useState('');
    const [submittingComment, setSubmittingComment] = useState(false);
    const [commentError, setCommentError] = useState(null);
    
    // Edit comment state
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editContent, setEditContent] = useState('');
    const [updatingComment, setUpdatingComment] = useState(false);
    const [editError, setEditError] = useState(null);

    // Edit post state
    const [isEditingPost, setIsEditingPost] = useState(false);
    const [editPostData, setEditPostData] = useState({ title: '', description: '', content: '' });
    const [updatingPost, setUpdatingPost] = useState(false);
    const [postEditError, setPostEditError] = useState(null);

    useEffect(() => {
        const fetchPostAndComments = async () => {
            try {
                const postData = await postService.fetchById(id);
                setPost(postData);

                try {
                    const commentsData = await commentService.fetchByPostId(id);
                    setComments(commentsData.content || commentsData || []);
                } catch (commentErr) {
                    console.error("Failed to fetch comments", commentErr);
                }

                setLoading(false);
            } catch (err) {
                setError('Failed to load post. Please try again later.');
                setLoading(false);
            }
        };
        fetchPostAndComments();
    }, [id]);

    const handleCommentSubmit = async (e) => {
        e.preventDefault();
        if (!commentContent.trim()) return;

        setCommentError(null);
        setSubmittingComment(true);

        try {
            const newComment = await commentService.create(id, { content: commentContent });
            // Add the new comment to the list
            setComments([...comments, newComment]);
            // Clear the form
            setCommentContent('');
        } catch (err) {
            console.error('Failed to create comment:', err);
            setCommentError(err.response?.data?.message || 'Failed to post comment. Please try again.');
        } finally {
            setSubmittingComment(false);
        }
    };

    const handleEditClick = (comment) => {
        setEditingCommentId(comment.id);
        setEditContent(comment.content);
        setEditError(null);
    };

    const handleCancelEdit = () => {
        setEditingCommentId(null);
        setEditContent('');
        setEditError(null);
    };

    const handleUpdateComment = async (commentId) => {
        if (!editContent.trim()) return;

        setEditError(null);
        setUpdatingComment(true);

        try {
            // Find the original comment to get name and email
            const originalComment = comments.find(c => c.id === commentId);
            
            const updatedComment = await commentService.update(id, commentId, { 
                name: originalComment.name,
                email: originalComment.email,
                content: editContent 
            });
            // Update the comment in the list
            setComments(comments.map(c => c.id === commentId ? updatedComment : c));
            // Exit edit mode
            setEditingCommentId(null);
            setEditContent('');
        } catch (err) {
            console.error('Failed to update comment:', err);
            setEditError(err.response?.data?.message || 'Failed to update comment. Please try again.');
        } finally {
            setUpdatingComment(false);
        }
    };

    // Post edit handlers
    const handleEditPost = () => {
        setEditPostData({
            title: post.title,
            description: post.description,
            content: post.content
        });
        setIsEditingPost(true);
        setPostEditError(null);
    };

    const handleCancelEditPost = () => {
        setIsEditingPost(false);
        setEditPostData({ title: '', description: '', content: '' });
        setPostEditError(null);
    };

    const handleUpdatePost = async () => {
        if (!editPostData.title.trim() || !editPostData.description.trim() || !editPostData.content.trim()) {
            setPostEditError('All fields are required');
            return;
        }

        setPostEditError(null);
        setUpdatingPost(true);

        try {
            const updatedPost = await postService.update(id, editPostData);
            setPost(updatedPost);
            setIsEditingPost(false);
            setEditPostData({ title: '', description: '', content: '' });
        } catch (err) {
            console.error('Failed to update post:', err);
            setPostEditError(err.response?.data?.message || 'Failed to update post. Please try again.');
        } finally {
            setUpdatingPost(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            </div>
        );
    }

    if (error || !post) {
        return (
            <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-6 rounded-2xl text-center font-medium max-w-2xl mx-auto shadow-sm border border-red-100 dark:border-red-800">
                <p className="text-xl mb-4">{error || 'Post not found'}</p>
                <Link to="/" className="inline-flex items-center text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 transition-colors">
                    <ArrowLeft className="w-4 h-4 mr-2" /> Back to Home
                </Link>
            </div>
        );
    }

    const displayDate = post.updatedAt && post.updatedAt !== post.createdAt ? post.updatedAt : post.createdAt;
    const isUpdated = post.updatedAt && post.updatedAt !== post.createdAt;
    
    const formattedDate = displayDate
        ? new Date(displayDate).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })
        : 'Recently';
    
    const dateLabel = isUpdated ? 'Last updated' : 'Published';
    
    const authorName = post.blogger ? `${post.blogger.firstName} ${post.blogger.lastName}`.trim() || post.blogger.userName : 'Anonymous Writer';
    
    const isPostOwner = isAuthenticated && user && post.blogger && post.blogger.email === user.username;

    return (
        <article className="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <Link to="/" className="inline-flex items-center text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 mb-8 transition-colors group">
                <ArrowLeft className="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
                Back to all posts
            </Link>

            <div className="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden mb-12 relative">
                <div className="h-2 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 absolute top-0 left-0 right-0"></div>
                
                {isEditingPost ? (
                    /* Edit Mode */
                    <div className="p-8 md:p-12">
                        <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-6">Edit Post</h2>
                        
                        {postEditError && (
                            <div className="mb-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-3 rounded-lg text-sm border border-red-100 dark:border-red-800">
                                {postEditError}
                            </div>
                        )}

                        <div className="space-y-6">
                            <div>
                                <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Title</label>
                                <input
                                    type="text"
                                    value={editPostData.title}
                                    onChange={(e) => setEditPostData({ ...editPostData, title: e.target.value })}
                                    disabled={updatingPost}
                                    className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors disabled:opacity-50"
                                    placeholder="Enter post title"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Description</label>
                                <textarea
                                    value={editPostData.description}
                                    onChange={(e) => setEditPostData({ ...editPostData, description: e.target.value })}
                                    disabled={updatingPost}
                                    rows={3}
                                    className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none disabled:opacity-50"
                                    placeholder="Enter post description"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Content</label>
                                <textarea
                                    value={editPostData.content}
                                    onChange={(e) => setEditPostData({ ...editPostData, content: e.target.value })}
                                    disabled={updatingPost}
                                    rows={12}
                                    className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none disabled:opacity-50"
                                    placeholder="Enter post content"
                                />
                            </div>

                            <div className="flex items-center gap-3 pt-4">
                                <button
                                    onClick={handleUpdatePost}
                                    disabled={updatingPost}
                                    className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-bold rounded-xl transition-colors disabled:cursor-not-allowed"
                                >
                                    {updatingPost ? (
                                        <>
                                            <Loader2 className="w-5 h-5 animate-spin" />
                                            Saving...
                                        </>
                                    ) : (
                                        <>
                                            <Check className="w-5 h-5" />
                                            Save Changes
                                        </>
                                    )}
                                </button>
                                <button
                                    onClick={handleCancelEditPost}
                                    disabled={updatingPost}
                                    className="inline-flex items-center gap-2 px-6 py-3 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 font-medium transition-colors disabled:opacity-50"
                                >
                                    <X className="w-5 h-5" />
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    /* View Mode */
                    <>
                        <header className="p-8 md:p-12 border-b border-slate-100 dark:border-slate-700">
                            <div className="flex items-center justify-between mb-6">
                                <span className="px-4 py-1.5 bg-indigo-50 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-300 text-sm font-bold rounded-full tracking-wide shadow-sm">
                                    {post.category?.categoryName || 'General'}
                                </span>
                                
                                {isPostOwner && (
                                    <button
                                        onClick={handleEditPost}
                                        className="inline-flex items-center gap-2 px-4 py-2 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 rounded-lg transition-colors font-medium"
                                    >
                                        <Edit2 className="w-4 h-4" />
                                        Edit Post
                                    </button>
                                )}
                            </div>

                            <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold text-slate-900 dark:text-white tracking-tight leading-tight mb-8">
                                {post.title}
                            </h1>

                            <div className="flex flex-wrap items-center gap-6 mt-6 pt-6 border-t border-slate-100 dark:border-slate-700">
                                <div className="flex items-center">
                                    <div className="h-12 w-12 bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900/50 dark:to-purple-900/50 rounded-full flex items-center justify-center border-2 border-white dark:border-slate-700 shadow-sm ring-2 ring-indigo-50 dark:ring-indigo-900/30">
                                        <User className="h-6 w-6 text-indigo-500 dark:text-indigo-400" />
                                    </div>
                                    <div className="ml-4">
                                        <p className="text-base font-bold text-slate-900 dark:text-slate-100 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors">
                                            <Link to={`/user/${post.blogger?.id}`}>{authorName}</Link>
                                        </p>
                                        <p className="text-sm text-slate-500 dark:text-slate-400">Author</p>
                                    </div>
                                </div>
                                <div className="flex items-center text-slate-500 dark:text-slate-400 text-sm bg-slate-50 dark:bg-slate-700/60 px-4 py-2 rounded-full">
                                    <Clock className="w-4 h-4 mr-2 text-indigo-400" />
                                    <span className="font-semibold mr-1">{dateLabel}:</span>
                                    {formattedDate}
                                </div>
                            </div>
                        </header>

                        <div className="p-8 md:p-12 prose prose-lg md:prose-xl max-w-none prose-indigo prose-headings:font-bold prose-a:text-indigo-600 hover:prose-a:text-indigo-800 dark:prose-invert">
                            <p className="text-xl text-slate-600 dark:text-slate-300 leading-relaxed font-medium mb-8 border-l-4 border-indigo-500 pl-6">
                                {post.description}
                            </p>
                            <div className="text-slate-800 dark:text-slate-200 leading-loose whitespace-pre-line" dangerouslySetInnerHTML={{ __html: post.content }}></div>
                        </div>
                    </>
                )}
            </div>

            <section className="bg-slate-50 dark:bg-slate-800/50 rounded-3xl p-8 md:p-12 border border-slate-200 dark:border-slate-700">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-8 flex items-center gap-3">
                    <MessageSquare className="w-6 h-6 text-indigo-500" />
                    Comments <span className="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 text-sm py-1 px-3 rounded-full">{comments.length}</span>
                </h3>

                {/* Add Comment Form - only for authenticated users */}
                {isAuthenticated && (
                    <div className="mb-8 bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm">
                        <h4 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Add a Comment</h4>
                        
                        {commentError && (
                            <div className="mb-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-3 rounded-lg text-sm border border-red-100 dark:border-red-800">
                                {commentError}
                            </div>
                        )}

                        <form onSubmit={handleCommentSubmit} className="space-y-4">
                            <div>
                                <textarea
                                    value={commentContent}
                                    onChange={(e) => setCommentContent(e.target.value)}
                                    placeholder="Share your thoughts..."
                                    rows={4}
                                    required
                                    disabled={submittingComment}
                                    className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none disabled:opacity-50"
                                />
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                                    Posting as {user?.name}
                                </p>
                            </div>
                            <div className="flex items-center gap-3">
                                <button
                                    type="submit"
                                    disabled={submittingComment || !commentContent.trim()}
                                    className="inline-flex items-center gap-2 px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-bold rounded-xl transition-all hover:scale-105 disabled:hover:scale-100 shadow-md disabled:cursor-not-allowed"
                                >
                                    {submittingComment ? (
                                        <>
                                            <Loader2 className="w-4 h-4 animate-spin" />
                                            Posting...
                                        </>
                                    ) : (
                                        <>
                                            <Send className="w-4 h-4" />
                                            Post Comment
                                        </>
                                    )}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setCommentContent('')}
                                    disabled={submittingComment}
                                    className="px-4 py-2.5 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 font-medium transition-colors disabled:opacity-50"
                                >
                                    Clear
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                {!isAuthenticated && (
                    <div className="mb-8 bg-indigo-50 dark:bg-indigo-900/20 p-6 rounded-2xl border border-indigo-100 dark:border-indigo-800 text-center">
                        <p className="text-slate-700 dark:text-slate-300 mb-3">
                            Want to join the conversation?
                        </p>
                        <Link
                            to="/login"
                            className="inline-flex items-center gap-2 px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition-all hover:scale-105 shadow-md"
                        >
                            Sign in to comment
                        </Link>
                    </div>
                )}

                {comments.length === 0 ? (
                    <div className="text-center py-12 bg-white dark:bg-slate-800 rounded-2xl border-2 border-dashed border-slate-300 dark:border-slate-600">
                        <MessageSquare className="w-12 h-12 mx-auto mb-4 text-slate-400 dark:text-slate-500" />
                        <p className="text-slate-500 dark:text-slate-400 text-lg font-medium mb-2">
                            No comments yet
                        </p>
                        <p className="text-slate-400 dark:text-slate-500 text-sm">
                            Be the first to share your thoughts!
                        </p>
                    </div>
                ) : (
                    <div className="space-y-5">
                        {comments.map((comment, index) => {
                            // Generate consistent gradient based on index
                            const gradients = [
                                'from-indigo-500 to-purple-600',
                                'from-violet-500 to-fuchsia-600',
                                'from-blue-500 to-cyan-600',
                                'from-rose-500 to-pink-600',
                                'from-emerald-500 to-teal-600',
                                'from-amber-500 to-orange-600'
                            ];
                            const gradient = gradients[index % gradients.length];
                            const isOwner = isAuthenticated && user && comment.email === user.username;
                            const isEditing = editingCommentId === comment.id;
                            
                            return (
                                <div key={comment.id || comment.commentId} className="group relative bg-white dark:bg-slate-800 rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 border border-slate-200 dark:border-slate-700 overflow-hidden">
                                    {/* Gradient accent bar */}
                                    <div className={`h-1 bg-gradient-to-r ${gradient}`}></div>
                                    
                                    <div className="p-6">
                                        {/* Author info */}
                                        <div className="flex items-start justify-between gap-4 mb-4">
                                            <div className="flex items-start gap-4 flex-grow min-w-0">
                                                <div className={`relative flex-shrink-0 w-12 h-12 rounded-full bg-gradient-to-br ${gradient} p-0.5 transition-transform group-hover:scale-110 duration-300`}>
                                                    <div className="w-full h-full bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                                                        <span className={`text-lg font-bold bg-gradient-to-br ${gradient} bg-clip-text text-transparent`}>
                                                            {(comment.name || 'A')[0].toUpperCase()}
                                                        </span>
                                                    </div>
                                                </div>
                                                
                                                <div className="flex-grow min-w-0">
                                                    <div className="flex items-center gap-2 flex-wrap">
                                                        <h4 className="font-bold text-slate-900 dark:text-slate-100 text-base">
                                                            {comment.name || 'Anonymous'}
                                                        </h4>
                                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gradient-to-r ${gradient} text-white`}>
                                                            {isOwner ? 'You' : 'Commenter'}
                                                        </span>
                                                    </div>
                                                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">
                                                        {comment.email}
                                                    </p>
                                                </div>
                                            </div>
                                            
                                            {/* Edit button - only show for comment owner */}
                                            {isOwner && !isEditing && (
                                                <button
                                                    onClick={() => handleEditClick(comment)}
                                                    className="flex-shrink-0 p-2 text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 rounded-lg transition-colors"
                                                    title="Edit comment"
                                                >
                                                    <Edit2 className="w-4 h-4" />
                                                </button>
                                            )}
                                        </div>
                                        
                                        {/* Comment content or edit form */}
                                        {isEditing ? (
                                            <div className="space-y-3">
                                                {editError && (
                                                    <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-3 rounded-lg text-sm border border-red-100 dark:border-red-800">
                                                        {editError}
                                                    </div>
                                                )}
                                                <textarea
                                                    value={editContent}
                                                    onChange={(e) => setEditContent(e.target.value)}
                                                    rows={3}
                                                    disabled={updatingComment}
                                                    className="w-full px-4 py-3 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none disabled:opacity-50"
                                                />
                                                <div className="flex items-center gap-2">
                                                    <button
                                                        onClick={() => handleUpdateComment(comment.id)}
                                                        disabled={updatingComment || !editContent.trim()}
                                                        className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white text-sm font-bold rounded-lg transition-colors disabled:cursor-not-allowed"
                                                    >
                                                        {updatingComment ? (
                                                            <>
                                                                <Loader2 className="w-4 h-4 animate-spin" />
                                                                Saving...
                                                            </>
                                                        ) : (
                                                            <>
                                                                <Check className="w-4 h-4" />
                                                                Save
                                                            </>
                                                        )}
                                                    </button>
                                                    <button
                                                        onClick={handleCancelEdit}
                                                        disabled={updatingComment}
                                                        className="inline-flex items-center gap-2 px-4 py-2 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 text-sm font-medium transition-colors disabled:opacity-50"
                                                    >
                                                        <X className="w-4 h-4" />
                                                        Cancel
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="relative pl-4 border-l-4 border-slate-200 dark:border-slate-700 group-hover:border-indigo-400 dark:group-hover:border-indigo-500 transition-colors">
                                                <p className="text-slate-700 dark:text-slate-300 leading-relaxed text-base">
                                                    {comment.content}
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                    
                                    {/* Hover effect overlay */}
                                    <div className={`absolute inset-0 bg-gradient-to-br ${gradient} opacity-0 group-hover:opacity-[0.02] transition-opacity duration-500 pointer-events-none`}></div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </section>
        </article>
    );
}
