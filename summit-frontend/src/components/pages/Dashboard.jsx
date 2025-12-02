import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { 
  TrendingUp, 
  TrendingDown, 
  DollarSign, 
  CreditCard,
  ArrowUpRight,
  ArrowDownRight,
  Activity,
  Wallet,
  Plus
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [stats, setStats] = useState({
    totalBalance: 0,
    monthlyChange: 0,
    activeAccounts: 0,
    totalIncome: 0,
    totalExpenses: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Fetch accounts
      const accountsRes = await axios.get('/api/accounts');
      setAccounts(accountsRes.data || []);

      // Fetch recent transactions
      let transactionsData = [];
      try {
        const transactionsRes = await axios.get('/api/transactions/recent?limit=5');
        transactionsData = transactionsRes.data || [];
        setTransactions(transactionsData);
      } catch (txnError) {
        console.log('No transactions yet');
        setTransactions([]);
      }

      // Calculate stats
      const totalBalance = (accountsRes.data || []).reduce((sum, acc) => sum + acc.balance, 0);
      const activeAccounts = (accountsRes.data || []).filter(acc => !acc.frozen).length;

      // Calculate real income and expenses from transactions
      const totalIncome = transactionsData
        .filter(t => t.type === 'CREDIT')
        .reduce((sum, t) => sum + Math.abs(t.amount), 0);

      const totalExpenses = transactionsData
        .filter(t => t.type === 'DEBIT')
        .reduce((sum, t) => sum + Math.abs(t.amount), 0);

      setStats({
        totalBalance,
        monthlyChange: 0, // Will be calculated based on actual data later
        activeAccounts,
        totalIncome,
        totalExpenses
      });

      setLoading(false);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);

      // If authentication error, clear localStorage and redirect
      if (error.response?.status === 401 || error.response?.status === 403) {
        localStorage.clear();
        window.location.href = '/login';
        return;
      }

      // Otherwise show empty state
      setAccounts([]);
      setTransactions([]);
      setStats({
        totalBalance: 0,
        monthlyChange: 0,
        activeAccounts: 0,
        totalIncome: 0,
        totalExpenses: 0
      });
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
          <h1 className="text-2xl font-bold text-gray-900">
            Welcome back, {user?.firstName}!
          </h1>
          <p className="text-gray-600 mt-1">
            Here's what's happening with your finances today.
          </p>
        </div>
        <button
          onClick={() => navigate('/accounts')}
          className="mt-4 sm:mt-0 flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          Open New Account
        </button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Total Balance */}
        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <div className="p-2 bg-blue-100 rounded-lg">
              <DollarSign className="w-6 h-6 text-blue-600" />
            </div>
          </div>
          <p className="text-sm text-gray-600 mb-1">Total Balance</p>
          <p className="text-2xl font-bold text-gray-900">
            {formatCurrency(stats.totalBalance)}
          </p>
        </div>

        {/* Active Accounts */}
        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <div className="p-2 bg-purple-100 rounded-lg">
              <CreditCard className="w-6 h-6 text-purple-600" />
            </div>
            <Activity className="w-4 h-4 text-gray-400" />
          </div>
          <p className="text-sm text-gray-600 mb-1">Active Accounts</p>
          <p className="text-2xl font-bold text-gray-900">{stats.activeAccounts}</p>
        </div>
      </div>

      {/* Accounts and Transactions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Accounts Overview */}
        <div className="bg-white rounded-xl border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">Your Accounts</h2>
              <button
                onClick={() => navigate('/accounts')}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium"
              >
                View all
              </button>
            </div>
          </div>
          <div className="p-6 space-y-4">
            {accounts.map((account) => (
              <div key={account.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-white rounded-lg">
                    <Wallet className="w-5 h-5 text-gray-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">
                      {account.type === 'CHECKING' ? 'Checking Account' : 'Savings Account'}
                    </p>
                    <p className="text-sm text-gray-500">Account {account.accountNumber}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-gray-900">{formatCurrency(account.balance)}</p>
                  <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    {account.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Transactions */}
        <div className="bg-white rounded-xl border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">Recent Transactions</h2>
              <button
                onClick={() => navigate('/transactions')}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium"
              >
                View all
              </button>
            </div>
          </div>
          <div className="p-6">
            <div className="space-y-3">
              {transactions.map((transaction) => (
                <div key={transaction.id} className="flex items-center justify-between py-3 border-b last:border-0">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${
                      transaction.type === 'CREDIT' ? 'bg-green-100' : 'bg-red-100'
                    }`}>
                      {transaction.type === 'CREDIT' ? (
                        <ArrowDownRight className="w-4 h-4 text-green-600" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4 text-red-600" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{transaction.description}</p>
                      <p className="text-xs text-gray-500">{formatDate(transaction.date)}</p>
                    </div>
                  </div>
                  <p className={`font-medium ${
                    transaction.type === 'CREDIT' ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {transaction.type === 'CREDIT' ? '+' : ''}{formatCurrency(Math.abs(transaction.amount))}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
