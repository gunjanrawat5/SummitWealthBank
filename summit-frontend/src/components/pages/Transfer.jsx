import React, { useState, useEffect } from 'react';
import { 
  ArrowRight, 
  ArrowRightLeft,
  CreditCard,
  Wallet,
  Send,
  Clock,
  CheckCircle,
  AlertCircle
} from 'lucide-react';
import axios from 'axios';

const Transfer = () => {
  const [accounts, setAccounts] = useState([]);
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [transferType, setTransferType] = useState('internal'); // internal or external
  const [externalAccount, setExternalAccount] = useState('');
  const [routingNumber, setRoutingNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [recentTransfers, setRecentTransfers] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    fetchAccounts();
    fetchRecentTransfers();
  }, []);

  const fetchAccounts = async () => {
    try {
      const response = await axios.get('/api/accounts');
      setAccounts(response.data);
    } catch (error) {
      console.error('Error fetching accounts:', error);
      // Mock data
      setAccounts([
        { id: 1, accountNumber: '****4521', type: 'CHECKING', balance: 12450.75 },
        { id: 2, accountNumber: '****7832', type: 'SAVINGS', balance: 35890.20 }
      ]);
    }
  };

  const fetchRecentTransfers = async () => {
    try {
      const response = await axios.get('/api/transactions/recent');
      setRecentTransfers(response.data);
    } catch (error) {
      console.error('Error fetching transfers:', error);
      // Mock data
      setRecentTransfers([
        {
          id: 1,
          from: '****4521',
          to: '****7832',
          amount: 500.00,
          status: 'COMPLETED',
          date: '2025-11-28',
          description: 'Monthly savings'
        },
        {
          id: 2,
          from: '****7832',
          to: '****4521',
          amount: 1000.00,
          status: 'COMPLETED',
          date: '2025-11-25',
          description: 'Emergency fund withdrawal'
        },
        {
          id: 3,
          from: '****4521',
          to: 'External: ****9876',
          amount: 250.00,
          status: 'PENDING',
          date: '2025-11-30',
          description: 'Rent payment'
        }
      ]);
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    // Client-side validation
    if (parseFloat(amount) <= 0) {
      setError('Transfer amount must be greater than $0');
      setLoading(false);
      return;
    }

    if (transferType === 'internal' && fromAccount === toAccount) {
      setError('Cannot transfer to the same account');
      setLoading(false);
      return;
    }

    try {
      const transferData = {
        fromAccountId: parseInt(fromAccount),
        amount: parseFloat(amount),
        description: description
      };

      if (transferType === 'internal') {
        transferData.toAccountId = parseInt(toAccount);
      } else {
        transferData.externalAccount = externalAccount;
        transferData.routingNumber = routingNumber;
      }

      await axios.post('/api/transfer', transferData);

      setSuccess(true);

      // Reset form and refresh after showing success
      setTimeout(() => {
        setFromAccount('');
        setToAccount('');
        setAmount('');
        setDescription('');
        setExternalAccount('');
        setRoutingNumber('');
        setSuccess(false);

        // Refresh data
        fetchAccounts();
        fetchRecentTransfers();
      }, 2000);
    } catch (error) {
      console.error('Error making transfer:', error);

      // Handle different error status codes
      if (error.response) {
        const status = error.response.status;
        const message = error.response.data?.message;

        switch (status) {
          case 400:
            setError(message || 'Invalid transfer details. Please check your input.');
            break;
          case 401:
            setError('Session expired. Please log in again.');
            setTimeout(() => {
              localStorage.clear();
              window.location.href = '/login';
            }, 2000);
            break;
          case 403:
            setError(message || 'You do not have permission to make this transfer.');
            break;
          case 500:
            setError('Server error. Please try again later.');
            break;
          default:
            setError(message || 'Failed to complete transfer. Please try again.');
        }
      } else if (error.request) {
        setError('Network error. Please check your connection.');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
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

  const getStatusIcon = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-4 h-4 text-green-600" />;
      case 'PENDING':
        return <Clock className="w-4 h-4 text-yellow-600" />;
      case 'FAILED':
        return <AlertCircle className="w-4 h-4 text-red-600" />;
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Transfer Money</h1>
        <p className="text-gray-600 mt-1">Transfer funds between accounts or to external accounts</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Transfer Form */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">New Transfer</h2>

            {/* Success Message */}
            {success && (
              <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg flex items-center gap-2">
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <span className="font-medium">Transfer completed successfully!</span>
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

            {/* Transfer Type Selector */}
            <div className="flex gap-4 mb-6">
              <button
                type="button"
                onClick={() => setTransferType('internal')}
                className={`flex-1 p-4 rounded-lg border-2 transition-all ${
                  transferType === 'internal'
                    ? 'border-blue-600 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <ArrowRightLeft className="w-5 h-5 text-blue-600 mb-2" />
                <p className="font-medium text-gray-900">Between Accounts</p>
                <p className="text-sm text-gray-500 mt-1">Transfer to your own accounts</p>
              </button>
              <button
                type="button"
                onClick={() => setTransferType('external')}
                className={`flex-1 p-4 rounded-lg border-2 transition-all ${
                  transferType === 'external'
                    ? 'border-blue-600 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <Send className="w-5 h-5 text-blue-600 mb-2" />
                <p className="font-medium text-gray-900">External Transfer</p>
                <p className="text-sm text-gray-500 mt-1">Send to another bank</p>
              </button>
            </div>

            <form onSubmit={handleTransfer} className="space-y-4">
              {/* From Account */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  From Account
                </label>
                <select
                  value={fromAccount}
                  onChange={(e) => {
                    setFromAccount(e.target.value);
                    setError('');
                  }}
                  disabled={loading || success}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                  required
                >
                  <option value="">Select account</option>
                  {accounts.map((account) => (
                    <option key={account.id} value={account.id}>
                      {account.type} {account.accountNumber} - {formatCurrency(account.balance)}
                    </option>
                  ))}
                </select>
              </div>

              {/* To Account / External Account */}
              {transferType === 'internal' ? (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    To Account
                  </label>
                  <select
                    value={toAccount}
                    onChange={(e) => {
                      setToAccount(e.target.value);
                      setError('');
                    }}
                    disabled={loading || success}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                    required
                  >
                    <option value="">Select account</option>
                    {accounts
                      .filter(acc => acc.id !== parseInt(fromAccount))
                      .map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.type} {account.accountNumber} - {formatCurrency(account.balance)}
                        </option>
                      ))}
                  </select>
                </div>
              ) : (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Account Number
                    </label>
                    <input
                      type="text"
                      value={externalAccount}
                      onChange={(e) => setExternalAccount(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                      placeholder="Enter account number"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Routing Number
                    </label>
                    <input
                      type="text"
                      value={routingNumber}
                      onChange={(e) => setRoutingNumber(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                      placeholder="Enter routing number"
                      required
                    />
                  </div>
                </>
              )}

              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Amount
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
                  <input
                    type="number"
                    value={amount}
                    onChange={(e) => {
                      setAmount(e.target.value);
                      setError('');
                    }}
                    disabled={loading || success}
                    className="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                    placeholder="0.00"
                    min="0.01"
                    step="0.01"
                    required
                  />
                </div>
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Description (Optional)
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                  placeholder="Add a note"
                />
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading || success}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Processing...
                  </>
                ) : success ? (
                  <>
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    Success
                  </>
                ) : (
                  <>
                    <Send className="w-5 h-5" />
                    Transfer Money
                  </>
                )}
              </button>
            </form>
          </div>
        </div>

        {/* Recent Transfers */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Transfers</h2>
            <div className="space-y-4">
              {recentTransfers.map((transfer) => (
                <div key={transfer.id} className="pb-4 border-b last:border-0 last:pb-0">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{transfer.description}</p>
                      <p className="text-xs text-gray-500 mt-1">
                        {transfer.from} → {transfer.to}
                      </p>
                    </div>
                    <p className="font-semibold text-gray-900">
                      {formatCurrency(transfer.amount)}
                    </p>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-xs text-gray-500">{formatDate(transfer.date)}</p>
                    <div className="flex items-center gap-1">
                      {getStatusIcon(transfer.status)}
                      <span className={`text-xs font-medium ${
                        transfer.status === 'COMPLETED' ? 'text-green-600' :
                        transfer.status === 'PENDING' ? 'text-yellow-600' :
                        'text-red-600'
                      }`}>
                        {transfer.status}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Transfer;
