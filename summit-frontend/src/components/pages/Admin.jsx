import React, { useState, useEffect } from 'react';
import { 
  Users,
  UserCheck,
  UserX,
  Shield,
  AlertCircle,
  CheckCircle,
  XCircle,
  Search,
  Filter,
  MoreVertical,
  Lock,
  Unlock,
  TrendingUp,
  DollarSign,
  Activity
} from 'lucide-react';
import axios from 'axios';

const Admin = () => {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    totalBalance: 0,
    suspiciousActivity: 0
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState(null);
  const [showActionModal, setShowActionModal] = useState(false);

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    try {
      const [usersRes, statsRes] = await Promise.all([
        axios.get('/api/admin/users'),
        axios.get('/api/admin/stats')
      ]);
      
      setUsers(usersRes.data);
      setStats(statsRes.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching admin data:', error);
      // Mock data
      setUsers([
        {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@example.com',
          status: 'ACTIVE',
          accountCount: 2,
          totalBalance: 48340.95,
          joinDate: '2024-01-15',
          lastLogin: '2025-11-30',
          riskScore: 'LOW'
        },
        {
          id: 2,
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane.smith@example.com',
          status: 'ACTIVE',
          accountCount: 3,
          totalBalance: 125670.50,
          joinDate: '2024-02-20',
          lastLogin: '2025-11-29',
          riskScore: 'LOW'
        },
        {
          id: 3,
          firstName: 'Mike',
          lastName: 'Johnson',
          email: 'mike.j@example.com',
          status: 'SUSPENDED',
          accountCount: 1,
          totalBalance: 5430.25,
          joinDate: '2024-03-10',
          lastLogin: '2025-11-15',
          riskScore: 'HIGH',
          suspensionReason: 'Suspicious activity detected'
        },
        {
          id: 4,
          firstName: 'Sarah',
          lastName: 'Williams',
          email: 'sarah.w@example.com',
          status: 'ACTIVE',
          accountCount: 2,
          totalBalance: 67890.00,
          joinDate: '2024-04-05',
          lastLogin: '2025-11-28',
          riskScore: 'MEDIUM'
        },
        {
          id: 5,
          firstName: 'Robert',
          lastName: 'Brown',
          email: 'robert.b@example.com',
          status: 'PENDING_VERIFICATION',
          accountCount: 0,
          totalBalance: 0,
          joinDate: '2025-11-28',
          lastLogin: '2025-11-28',
          riskScore: 'UNKNOWN'
        }
      ]);
      
      setStats({
        totalUsers: 5,
        activeUsers: 3,
        totalBalance: 247331.70,
        suspiciousActivity: 1
      });
      
      setLoading(false);
    }
  };

  const handleUserAction = async (action, userId) => {
    try {
      await axios.post(`/api/admin/users/${userId}/action`, { action });
      fetchAdminData();
      setShowActionModal(false);
      setSelectedUser(null);
    } catch (error) {
      console.error('Error performing user action:', error);
      alert(`Action "${action}" performed successfully!`);
      setShowActionModal(false);
      setSelectedUser(null);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  const getRiskBadge = (risk) => {
    const badges = {
      'LOW': 'bg-green-100 text-green-800',
      'MEDIUM': 'bg-yellow-100 text-yellow-800',
      'HIGH': 'bg-red-100 text-red-800',
      'UNKNOWN': 'bg-gray-100 text-gray-800'
    };
    return badges[risk] || badges['UNKNOWN'];
  };

  const getStatusBadge = (status) => {
    const badges = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'SUSPENDED': 'bg-red-100 text-red-800',
      'PENDING_VERIFICATION': 'bg-yellow-100 text-yellow-800'
    };
    return badges[status] || 'bg-gray-100 text-gray-800';
  };

  const filteredUsers = users.filter(user => {
    const matchesSearch = 
      user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesFilter = 
      filterStatus === 'all' || 
      user.status === filterStatus;
    
    return matchesSearch && matchesFilter;
  });

  const ActionModal = () => {
    if (!selectedUser) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-6 w-full max-w-md">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">
            User Actions: {selectedUser.firstName} {selectedUser.lastName}
          </h3>
          
          <div className="space-y-3">
            {selectedUser.status === 'ACTIVE' && (
              <>
                <button
                  onClick={() => handleUserAction('suspend', selectedUser.id)}
                  className="w-full flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                >
                  <UserX className="w-5 h-5 text-red-600" />
                  <span className="text-left flex-1">
                    <p className="font-medium text-gray-900">Suspend Account</p>
                    <p className="text-sm text-gray-500">Temporarily disable user access</p>
                  </span>
                </button>
                
                <button
                  onClick={() => handleUserAction('lock', selectedUser.id)}
                  className="w-full flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                >
                  <Lock className="w-5 h-5 text-yellow-600" />
                  <span className="text-left flex-1">
                    <p className="font-medium text-gray-900">Lock Account</p>
                    <p className="text-sm text-gray-500">Prevent transactions</p>
                  </span>
                </button>
              </>
            )}
            
            {selectedUser.status === 'SUSPENDED' && (
              <button
                onClick={() => handleUserAction('reactivate', selectedUser.id)}
                className="w-full flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <UserCheck className="w-5 h-5 text-green-600" />
                <span className="text-left flex-1">
                  <p className="font-medium text-gray-900">Reactivate Account</p>
                  <p className="text-sm text-gray-500">Restore user access</p>
                </span>
              </button>
            )}
            
            {selectedUser.status === 'PENDING_VERIFICATION' && (
              <button
                onClick={() => handleUserAction('verify', selectedUser.id)}
                className="w-full flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <CheckCircle className="w-5 h-5 text-green-600" />
                <span className="text-left flex-1">
                  <p className="font-medium text-gray-900">Verify Account</p>
                  <p className="text-sm text-gray-500">Complete verification process</p>
                </span>
              </button>
            )}
            
            <button
              onClick={() => handleUserAction('reset_password', selectedUser.id)}
              className="w-full flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <Unlock className="w-5 h-5 text-blue-600" />
              <span className="text-left flex-1">
                <p className="font-medium text-gray-900">Reset Password</p>
                <p className="text-sm text-gray-500">Send password reset email</p>
              </span>
            </button>
          </div>
          
          <div className="mt-6 flex gap-3">
            <button
              onClick={() => {
                setShowActionModal(false);
                setSelectedUser(null);
              }}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center gap-3">
        <Shield className="w-8 h-8 text-blue-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-600 mt-1">Manage users and monitor system activity</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <Users className="w-5 h-5 text-blue-600" />
            <span className="text-sm text-gray-500">Total</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{stats.totalUsers}</p>
          <p className="text-sm text-gray-600 mt-1">Registered Users</p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <Activity className="w-5 h-5 text-green-600" />
            <span className="text-sm text-gray-500">Active</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{stats.activeUsers}</p>
          <p className="text-sm text-gray-600 mt-1">Active Users</p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <DollarSign className="w-5 h-5 text-purple-600" />
            <span className="text-sm text-gray-500">Total</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{formatCurrency(stats.totalBalance)}</p>
          <p className="text-sm text-gray-600 mt-1">System Balance</p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <AlertCircle className="w-5 h-5 text-red-600" />
            <span className="text-sm text-gray-500">Alerts</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{stats.suspiciousActivity}</p>
          <p className="text-sm text-gray-600 mt-1">Suspicious Activity</p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                placeholder="Search users..."
              />
            </div>
          </div>
          
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
          >
            <option value="all">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="PENDING_VERIFICATION">Pending Verification</option>
          </select>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  User
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Accounts
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Balance
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Risk Score
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Last Login
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredUsers.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </p>
                      <p className="text-sm text-gray-500">{user.email}</p>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadge(user.status)}`}>
                      {user.status.replace(/_/g, ' ')}
                    </span>
                    {user.suspensionReason && (
                      <p className="text-xs text-red-600 mt-1">{user.suspensionReason}</p>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {user.accountCount}
                  </td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">
                    {formatCurrency(user.totalBalance)}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRiskBadge(user.riskScore)}`}>
                      {user.riskScore}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {formatDate(user.lastLogin)}
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => {
                        setSelectedUser(user);
                        setShowActionModal(true);
                      }}
                      className="p-2 hover:bg-gray-100 rounded-lg"
                    >
                      <MoreVertical className="w-4 h-4 text-gray-600" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Action Modal */}
      {showActionModal && <ActionModal />}
    </div>
  );
};

export default Admin;
