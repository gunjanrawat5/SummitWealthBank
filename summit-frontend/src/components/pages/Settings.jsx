import React, { useState, useEffect } from 'react';
import {
  User,
  Lock,
  Phone,
  Camera,
  Save,
  CheckCircle,
  AlertCircle
} from 'lucide-react';
import axios from 'axios';
import { useAuth } from '../../contexts/AuthContext';

const Settings = () => {
  const { user, setUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('password');

  // Password change state
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState(false);

  // Phone update state
  const [phoneData, setPhoneData] = useState({
    phone: user?.phone || ''
  });
  const [phoneError, setPhoneError] = useState('');
  const [phoneSuccess, setPhoneSuccess] = useState(false);

  // Profile pic state
  const [profilePicUrl, setProfilePicUrl] = useState(user?.profilePicUrl || '');
  const [picError, setPicError] = useState('');
  const [picSuccess, setPicSuccess] = useState(false);

  useEffect(() => {
    setPhoneData({ phone: user?.phone || '' });
    setProfilePicUrl(user?.profilePicUrl || '');
  }, [user]);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setLoading(true);
    setPasswordError('');
    setPasswordSuccess(false);

    // Client-side validation
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordError('New passwords do not match');
      setLoading(false);
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setPasswordError('Password must be at least 6 characters');
      setLoading(false);
      return;
    }

    try {
      await axios.put('/api/user/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });

      setPasswordSuccess(true);
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });

      setTimeout(() => setPasswordSuccess(false), 3000);
    } catch (error) {
      console.error('Error changing password:', error);
      const message = error.response?.data?.message || 'Failed to change password';
      setPasswordError(message);
    } finally {
      setLoading(false);
    }
  };

  const handlePhoneUpdate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setPhoneError('');
    setPhoneSuccess(false);

    if (!phoneData.phone || phoneData.phone.trim() === '') {
      setPhoneError('Phone number cannot be empty');
      setLoading(false);
      return;
    }

    try {
      const response = await axios.put('/api/user/update-phone', {
        phone: phoneData.phone
      });

      setPhoneSuccess(true);

      // Update user context
      setUser({ ...user, phone: response.data.phone });

      setTimeout(() => setPhoneSuccess(false), 3000);
    } catch (error) {
      console.error('Error updating phone:', error);
      const message = error.response?.data?.message || 'Failed to update phone number';
      setPhoneError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleProfilePicUpdate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setPicError('');
    setPicSuccess(false);

    try {
      const response = await axios.put('/api/user/update-profile-pic', {
        profilePicUrl: profilePicUrl
      });

      setPicSuccess(true);

      // Update user context
      setUser({ ...user, profilePicUrl: response.data.profilePicUrl });

      setTimeout(() => setPicSuccess(false), 3000);
    } catch (error) {
      console.error('Error updating profile picture:', error);
      const message = error.response?.data?.message || 'Failed to update profile picture';
      setPicError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <p className="text-gray-600 mt-1">Manage your account settings and preferences</p>
      </div>

      {/* Profile Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center gap-6">
          <div className="relative">
            {profilePicUrl ? (
              <img
                src={profilePicUrl}
                alt="Profile"
                className="w-24 h-24 rounded-full object-cover border-4 border-gray-200"
              />
            ) : (
              <div className="w-24 h-24 rounded-full bg-blue-100 flex items-center justify-center">
                <User className="w-12 h-12 text-blue-600" />
              </div>
            )}
          </div>
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              {user?.firstName} {user?.lastName}
            </h2>
            <p className="text-gray-600">{user?.email}</p>
            <p className="text-sm text-gray-500 mt-1">
              Member since {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="border-b border-gray-200">
          <nav className="flex -mb-px">
            <button
              onClick={() => setActiveTab('password')}
              className={`px-6 py-4 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'password'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <Lock className="w-4 h-4 inline-block mr-2" />
              Change Password
            </button>
            <button
              onClick={() => setActiveTab('phone')}
              className={`px-6 py-4 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'phone'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <Phone className="w-4 h-4 inline-block mr-2" />
              Update Phone
            </button>
            <button
              onClick={() => setActiveTab('picture')}
              className={`px-6 py-4 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'picture'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <Camera className="w-4 h-4 inline-block mr-2" />
              Profile Picture
            </button>
          </nav>
        </div>

        <div className="p-6">
          {/* Change Password Tab */}
          {activeTab === 'password' && (
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Change Password</h3>

              {passwordSuccess && (
                <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg flex items-center gap-2">
                  <CheckCircle className="w-5 h-5" />
                  <span className="font-medium">Password changed successfully!</span>
                </div>
              )}

              {passwordError && (
                <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg flex items-start gap-2">
                  <AlertCircle className="w-5 h-5 mt-0.5 flex-shrink-0" />
                  <span className="text-sm">{passwordError}</span>
                </div>
              )}

              <form onSubmit={handlePasswordChange} className="space-y-4 max-w-md">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Current Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.currentPassword}
                    onChange={(e) => {
                      setPasswordData({ ...passwordData, currentPassword: e.target.value });
                      setPasswordError('');
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    New Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.newPassword}
                    onChange={(e) => {
                      setPasswordData({ ...passwordData, newPassword: e.target.value });
                      setPasswordError('');
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                    required
                  />
                  <p className="mt-1 text-xs text-gray-500">At least 6 characters</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.confirmPassword}
                    onChange={(e) => {
                      setPasswordData({ ...passwordData, confirmPassword: e.target.value });
                      setPasswordError('');
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      Updating...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Change Password
                    </>
                  )}
                </button>
              </form>
            </div>
          )}

          {/* Update Phone Tab */}
          {activeTab === 'phone' && (
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Update Phone Number</h3>

              {phoneSuccess && (
                <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg flex items-center gap-2">
                  <CheckCircle className="w-5 h-5" />
                  <span className="font-medium">Phone number updated successfully!</span>
                </div>
              )}

              {phoneError && (
                <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg flex items-start gap-2">
                  <AlertCircle className="w-5 h-5 mt-0.5 flex-shrink-0" />
                  <span className="text-sm">{phoneError}</span>
                </div>
              )}

              <form onSubmit={handlePhoneUpdate} className="space-y-4 max-w-md">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    value={phoneData.phone}
                    onChange={(e) => {
                      setPhoneData({ phone: e.target.value });
                      setPhoneError('');
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                    placeholder="Enter your phone number"
                    required
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      Updating...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Update Phone
                    </>
                  )}
                </button>
              </form>
            </div>
          )}

          {/* Update Profile Picture Tab */}
          {activeTab === 'picture' && (
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Update Profile Picture</h3>

              {picSuccess && (
                <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg flex items-center gap-2">
                  <CheckCircle className="w-5 h-5" />
                  <span className="font-medium">Profile picture updated successfully!</span>
                </div>
              )}

              {picError && (
                <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg flex items-start gap-2">
                  <AlertCircle className="w-5 h-5 mt-0.5 flex-shrink-0" />
                  <span className="text-sm">{picError}</span>
                </div>
              )}

              <form onSubmit={handleProfilePicUpdate} className="space-y-4 max-w-md">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Profile Picture URL
                  </label>
                  <input
                    type="url"
                    value={profilePicUrl}
                    onChange={(e) => {
                      setProfilePicUrl(e.target.value);
                      setPicError('');
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                    placeholder="https://example.com/your-image.jpg"
                  />
                  <p className="mt-1 text-xs text-gray-500">Enter the URL of your profile picture</p>
                </div>

                {profilePicUrl && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Preview</label>
                    <img
                      src={profilePicUrl}
                      alt="Preview"
                      className="w-32 h-32 rounded-full object-cover border-4 border-gray-200"
                      onError={(e) => {
                        e.target.src = '';
                        e.target.style.display = 'none';
                      }}
                    />
                  </div>
                )}

                <button
                  type="submit"
                  disabled={loading}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      Updating...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Update Picture
                    </>
                  )}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Settings;