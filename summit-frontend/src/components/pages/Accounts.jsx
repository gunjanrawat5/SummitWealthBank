import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  CreditCard, 
  Wallet,
  TrendingUp,
  MoreVertical,
  Download,
  Eye,
  EyeOff
} from 'lucide-react';
import axios from 'axios';

const Accounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showBalances, setShowBalances] = useState(true);
  const [showNewAccountModal, setShowNewAccountModal] = useState(false);
  const [stats, setStats] = useState({
    totalBalance: 0,
    activeAccounts: 0,
    monthlyChange: 0
  });

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/accounts');
      const accountsData = response.data || [];
      setAccounts(accountsData);

      // Calculate stats - use actual status from the account
      const totalBalance = accountsData.reduce((sum, acc) => sum + (acc.balance || 0), 0);
      const activeAccounts = accountsData.filter(acc => !acc.frozen).length;

      setStats({
        totalBalance,
        activeAccounts,
        monthlyChange: 0
      });

      setLoading(false);
    } catch (error) {
      console.error('Error fetching accounts:', error);
      setLoading(false);

      // If authentication error, clear localStorage and redirect
      if (error.response?.status === 401 || error.response?.status === 403) {
        localStorage.clear();
        window.location.href = '/login';
        return;
      }

      // Otherwise show empty state
      setAccounts([]);
      setStats({
        totalBalance: 0,
        activeAccounts: 0,
        monthlyChange: 0
      });
    }
  };

  const formatCurrency = (amount) => {
    if (!showBalances) {
      return '****';
    }
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const NewAccountModal = () => {
    const [accountType, setAccountType] = useState('CHECKING');
    const [initialDeposit, setInitialDeposit] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
      e.preventDefault();
      setSubmitting(true);
      setError('');
      setSuccess(false);

      // Client-side validation
      if (parseFloat(initialDeposit) <= 0) {
        setError('Initial deposit must be greater than $0');
        setSubmitting(false);
        return;
      }

      try {
        await axios.post('/api/accounts/open', {
          type: accountType,
          initialDeposit: parseFloat(initialDeposit)
        });

        setSuccess(true);

        // Close modal and refresh after showing success
        setTimeout(() => {
          setShowNewAccountModal(false);
          fetchAccounts();
        }, 1500);
      } catch (error) {
        console.error('Error opening account:', error);

        // Handle different error status codes
        if (error.response) {
          const status = error.response.status;
          const message = error.response.data?.message;

          switch (status) {
            case 400:
              setError(message || 'Invalid account details. Please check your input.');
              break;
            case 401:
              setError('Session expired. Please log in again.');
              setTimeout(() => {
                localStorage.clear();
                window.location.href = '/login';
              }, 2000);
              break;
            case 403:
              setError('You do not have permission to open an account.');
              break;
            case 409:
              setError(message || 'Account already exists.');
              break;
            case 500:
              setError('Server error. Please try again later.');
              break;
            default:
              setError(message || 'Failed to open account. Please try again.');
          }
        } else if (error.request) {
          setError('Network error. Please check your connection.');
        } else {
          setError('An unexpected error occurred. Please try again.');
        }
      } finally {
        setSubmitting(false);
      }
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-6 w-full max-w-md">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">Open New Account</h3>

          {/* Success Message */}
          {success && (
            <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg flex items-center gap-2">
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span className="font-medium">Account opened successfully!</span>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg flex items-start gap-2">
              <svg className="w-5 h-5 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              <span className="text-sm">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Account Type
              </label>
              <select
                value={accountType}
                onChange={(e) => setAccountType(e.target.value)}
                disabled={submitting || success}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                <option value="CHECKING">Checking Account</option>
                <option value="SAVINGS">Savings Account</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Initial Deposit
              </label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
                <input
                  type="number"
                  value={initialDeposit}
                  onChange={(e) => {
                    setInitialDeposit(e.target.value);
                    setError(''); // Clear error on input change
                  }}
                  disabled={submitting || success}
                  className="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                  placeholder="0.00"
                  min="0.01"
                  step="0.01"
                  required
                />
              </div>
              <p className="mt-1 text-xs text-gray-500">Minimum deposit: $0.01</p>
            </div>
            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={() => setShowNewAccountModal(false)}
                disabled={submitting}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting || success}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {submitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Opening...
                  </>
                ) : success ? (
                  <>
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    Success
                  </>
                ) : (
                  'Open Account'
                )}
              </button>
            </div>
          </form>
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
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Accounts Overview</h1>
          <p className="text-gray-600 mt-1">Manage your checking and savings accounts</p>
        </div>
        <button
          onClick={() => setShowNewAccountModal(true)}
          className="mt-4 sm:mt-0 flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          Open New Account
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <p className="text-sm text-gray-600">Total Balance</p>
            <button
              onClick={() => setShowBalances(!showBalances)}
              className="p-1 hover:bg-gray-100 rounded"
            >
              {showBalances ? (
                <Eye className="w-4 h-4 text-gray-600" />
              ) : (
                <EyeOff className="w-4 h-4 text-gray-600" />
              )}
            </button>
          </div>
          <p className="text-2xl font-bold text-gray-900">
            {formatCurrency(stats.totalBalance)}
          </p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <p className="text-sm text-gray-600 mb-4">Active Accounts</p>
          <p className="text-2xl font-bold text-gray-900">{stats.activeAccounts}</p>
        </div>
      </div>

      {/* Accounts List */}
      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Your Accounts</h2>
        {accounts.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
            <Wallet className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500 mb-4">No accounts yet</p>
            <button
              onClick={() => setShowNewAccountModal(true)}
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              <Plus className="w-4 h-4" />
              Open Your First Account
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {accounts.map((account) => (
            <div key={account.id} className="bg-white rounded-xl border border-gray-200 p-6">
              <div className="flex items-start justify-between">
                <div className="flex items-start gap-4">
                  <div className="p-3 bg-blue-100 rounded-xl">
                    {account.type === 'CHECKING' ? (
                      <CreditCard className="w-6 h-6 text-blue-600" />
                    ) : (
                      <Wallet className="w-6 h-6 text-blue-600" />
                    )}
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">
                      {account.type === 'CHECKING' ? 'Checking Account' : 'Savings Account'}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">
                      Account {account.accountNumber || `#${account.id}`}
                    </p>
                    <div className="flex items-center gap-4 mt-3">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        account.frozen ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                      }`}>
                        {account.frozen ? 'FROZEN' : 'ACTIVE'}
                      </span>
                      {account.createdAt && (
                        <span className="text-sm text-gray-500">
                          Opened {new Date(account.createdAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                
                <div className="text-right">
                  <p className="text-2xl font-bold text-gray-900">
                    {formatCurrency(account.balance)}
                  </p>
                  <div className="flex items-center gap-2 mt-3">
                    <button className="p-2 hover:bg-gray-100 rounded-lg">
                      <Download className="w-4 h-4 text-gray-600" />
                    </button>
                    <button className="p-2 hover:bg-gray-100 rounded-lg">
                      <MoreVertical className="w-4 h-4 text-gray-600" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
          </div>
        )}
      </div>

      {/* New Account Modal */}
      {showNewAccountModal && <NewAccountModal />}
    </div>
  );
};

export default Accounts;
