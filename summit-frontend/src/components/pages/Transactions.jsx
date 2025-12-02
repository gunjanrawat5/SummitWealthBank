import React, { useState, useEffect } from 'react';
import {
  Filter,
  Download,
  Search,
  Calendar,
  ArrowUpRight,
  ArrowDownRight,
  ShoppingBag,
  Utensils,
  Car,
  Home,
  Briefcase,
  Heart,
  MoreHorizontal,
  TrendingUp,
  DollarSign,
  CreditCard
} from 'lucide-react';
import axios from 'axios';

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('all'); // all, income, expense
  const [dateRange, setDateRange] = useState('30'); // 7, 30, 90, all

  useEffect(() => {
    fetchTransactions();
  }, []);

  useEffect(() => {
    filterTransactions();
  }, [transactions, searchTerm, filterType, dateRange]);

  const fetchTransactions = async () => {
    try {
      const [transactionsResponse, accountsResponse] = await Promise.all([
        axios.get('/api/transactions/recent?limit=100'),
        axios.get('/api/accounts')
      ]);

      const transactionsData = transactionsResponse.data || [];
      const accountsData = accountsResponse.data || [];

      // Create a map of account IDs to account numbers and user's account IDs
      const accountMap = {};
      const userAccountIds = [];
      accountsData.forEach(acc => {
        accountMap[acc.id] = acc.accountNumber || `#${acc.id}`;
        userAccountIds.push(acc.id);
      });

      // Transform backend Transaction data to match frontend expectations
      const transformedTransactions = transactionsData.map(t => {
        const fromAccountNum = accountMap[t.fromAccountId] || `Account ${t.fromAccountId}`;
        const toAccountNum = accountMap[t.toAccountId] || `Account ${t.toAccountId}`;

        // Determine if this is a credit or debit based on whether user owns from/to account
        const isUserSender = userAccountIds.includes(t.fromAccountId);
        const isUserReceiver = userAccountIds.includes(t.toAccountId);

        let type, accountNumber, merchant;

        if (isUserSender && isUserReceiver) {
          // Internal transfer between user's own accounts - show as both
          type = 'TRANSFER';
          accountNumber = fromAccountNum;
          merchant = `Transfer: ${fromAccountNum} → ${toAccountNum}`;
        } else if (isUserReceiver) {
          // Incoming transfer - CREDIT
          type = 'CREDIT';
          accountNumber = toAccountNum;
          merchant = `From ${fromAccountNum}`;
        } else {
          // Outgoing transfer - DEBIT
          type = 'DEBIT';
          accountNumber = fromAccountNum;
          merchant = `To ${toAccountNum}`;
        }

        return {
          id: t.id,
          description: t.description || 'Transfer',
          amount: t.amount,
          date: t.timestamp,
          type: type,
          category: 'Transfer',
          merchant: merchant,
          accountNumber: accountNumber,
          fromAccountId: t.fromAccountId,
          toAccountId: t.toAccountId
        };
      });

      setTransactions(transformedTransactions);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching transactions:', error);
      // Mock data
      const mockTransactions = [
        {
          id: 1,
          description: 'Amazon Purchase',
          amount: -89.99,
          date: '2025-11-28',
          type: 'DEBIT',
          category: 'Shopping',
          merchant: 'Amazon.com',
          accountNumber: '****4521'
        },
        {
          id: 2,
          description: 'Salary Deposit',
          amount: 3500.00,
          date: '2025-11-27',
          type: 'CREDIT',
          category: 'Income',
          merchant: 'TechCorp Inc',
          accountNumber: '****4521'
        },
        {
          id: 3,
          description: 'Starbucks',
          amount: -12.50,
          date: '2025-11-26',
          type: 'DEBIT',
          category: 'Dining',
          merchant: 'Starbucks',
          accountNumber: '****4521'
        },
        {
          id: 4,
          description: 'Transfer from Savings',
          amount: 500.00,
          date: '2025-11-25',
          type: 'CREDIT',
          category: 'Transfer',
          merchant: 'Internal Transfer',
          accountNumber: '****4521'
        },
        {
          id: 5,
          description: 'Utilities Bill',
          amount: -145.30,
          date: '2025-11-24',
          type: 'DEBIT',
          category: 'Bills',
          merchant: 'City Utilities',
          accountNumber: '****4521'
        },
        {
          id: 6,
          description: 'Grocery Store',
          amount: -78.45,
          date: '2025-11-23',
          type: 'DEBIT',
          category: 'Groceries',
          merchant: 'Whole Foods',
          accountNumber: '****4521'
        },
        {
          id: 7,
          description: 'Gas Station',
          amount: -45.00,
          date: '2025-11-22',
          type: 'DEBIT',
          category: 'Transportation',
          merchant: 'Shell',
          accountNumber: '****4521'
        },
        {
          id: 8,
          description: 'Investment Return',
          amount: 250.00,
          date: '2025-11-20',
          type: 'CREDIT',
          category: 'Investment',
          merchant: 'Vanguard',
          accountNumber: '****7832'
        }
      ];
      setTransactions(mockTransactions);
      setLoading(false);
    }
  };

  const filterTransactions = () => {
    let filtered = [...transactions];

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(t => 
        t.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.merchant.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.category.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by type
    if (filterType === 'income') {
      filtered = filtered.filter(t => t.type === 'CREDIT');
    } else if (filterType === 'expense') {
      filtered = filtered.filter(t => t.type === 'DEBIT');
    }

    // Filter by date range
    if (dateRange !== 'all') {
      const days = parseInt(dateRange);
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - days);
      filtered = filtered.filter(t => new Date(t.date) >= cutoffDate);
    }

    setFilteredTransactions(filtered);
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

  const getCategoryIcon = (category) => {
    const icons = {
      'Shopping': <ShoppingBag className="w-4 h-4" />,
      'Dining': <Utensils className="w-4 h-4" />,
      'Transportation': <Car className="w-4 h-4" />,
      'Bills': <Home className="w-4 h-4" />,
      'Income': <Briefcase className="w-4 h-4" />,
      'Investment': <TrendingUp className="w-4 h-4" />,
      'Healthcare': <Heart className="w-4 h-4" />
    };
    return icons[category] || <MoreHorizontal className="w-4 h-4" />;
  };

  const getCategoryColor = (category) => {
    const colors = {
      'Shopping': 'bg-purple-100 text-purple-600',
      'Dining': 'bg-orange-100 text-orange-600',
      'Transportation': 'bg-blue-100 text-blue-600',
      'Bills': 'bg-red-100 text-red-600',
      'Income': 'bg-green-100 text-green-600',
      'Investment': 'bg-indigo-100 text-indigo-600',
      'Healthcare': 'bg-pink-100 text-pink-600'
    };
    return colors[category] || 'bg-gray-100 text-gray-600';
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
          <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
          <p className="text-gray-600 mt-1">Track and manage your financial activity</p>
        </div>
        <button className="mt-4 sm:mt-0 flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
          <Download className="w-4 h-4" />
          Export
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Search */}
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                placeholder="Search transactions..."
              />
            </div>
          </div>

          {/* Type Filter */}
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
          >
            <option value="all">All Types</option>
            <option value="income">Income</option>
            <option value="expense">Expenses</option>
          </select>

          {/* Date Range */}
          <select
            value={dateRange}
            onChange={(e) => setDateRange(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
          >
            <option value="7">Last 7 days</option>
            <option value="30">Last 30 days</option>
            <option value="90">Last 90 days</option>
            <option value="all">All time</option>
          </select>

          {/* Filter Button */}
          <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Filter className="w-4 h-4" />
            Filter
          </button>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Transaction
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Category
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Account
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredTransactions.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center">
                    <div className="flex flex-col items-center justify-center">
                      <DollarSign className="w-12 h-12 text-gray-400 mb-4" />
                      <p className="text-gray-500 mb-2">No transactions found</p>
                      <p className="text-sm text-gray-400">
                        {searchTerm || filterType !== 'all' || dateRange !== '30'
                          ? 'Try adjusting your filters'
                          : 'Make a transfer to see your transaction history'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredTransactions.map((transaction) => (
                  <tr key={transaction.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
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
                          <p className="text-xs text-gray-500">{transaction.merchant}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium ${getCategoryColor(transaction.category)}`}>
                        {getCategoryIcon(transaction.category)}
                        {transaction.category}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {formatDate(transaction.date)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {transaction.accountNumber}
                    </td>
                    <td className={`px-6 py-4 text-right text-sm font-medium ${
                      transaction.type === 'CREDIT' ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {transaction.type === 'CREDIT' ? '+' : ''}{formatCurrency(Math.abs(transaction.amount))}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Transactions;
