import React, { useState, useEffect } from 'react';
import { 
  CreditCard,
  ShoppingBag,
  Utensils,
  Car,
  Home,
  MapPin,
  Clock,
  AlertCircle,
  CheckCircle,
  XCircle,
  RefreshCw
} from 'lucide-react';
import axios from 'axios';

const CardFeed = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [filter, setFilter] = useState('all'); // all, approved, declined, pending

  useEffect(() => {
    fetchCardTransactions();
    // Set up polling for real-time updates
    const interval = setInterval(fetchCardTransactions, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchCardTransactions = async () => {
    try {
      const response = await axios.get('/api/card-feed');
      // Map backend CardTransaction to frontend format
      const mappedTransactions = (response.data || []).map(tx => ({
        id: tx.id,
        merchant: tx.merchant,
        amount: tx.amount,
        status: tx.flaggedFraud ? 'DECLINED' : 'APPROVED',
        category: 'Shopping', // Default category - can be enhanced later
        location: 'N/A',
        time: formatTime(tx.timestamp),
        cardLast4: '****', // We don't store card numbers
        type: 'DEBIT',
        declineReason: tx.flaggedFraud ? 'Flagged for potential fraud' : null
      }));
      setTransactions(mappedTransactions);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching card transactions:', error);
      setTransactions([]);
      setLoading(false);
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return 'Unknown';
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;

    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchCardTransactions();
    setTimeout(() => setRefreshing(false), 1000);
  };

  const getCategoryIcon = (category) => {
    const icons = {
      'Shopping': <ShoppingBag className="w-4 h-4" />,
      'Dining': <Utensils className="w-4 h-4" />,
      'Transportation': <Car className="w-4 h-4" />,
      'Groceries': <ShoppingBag className="w-4 h-4" />,
      'Entertainment': <Home className="w-4 h-4" />,
      'Other': <CreditCard className="w-4 h-4" />
    };
    return icons[category] || <CreditCard className="w-4 h-4" />;
  };

  const getCategoryColor = (category) => {
    const colors = {
      'Shopping': 'bg-purple-100 text-purple-600',
      'Dining': 'bg-orange-100 text-orange-600',
      'Transportation': 'bg-blue-100 text-blue-600',
      'Groceries': 'bg-green-100 text-green-600',
      'Entertainment': 'bg-pink-100 text-pink-600',
      'Other': 'bg-gray-100 text-gray-600'
    };
    return colors[category] || 'bg-gray-100 text-gray-600';
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'APPROVED':
        return <CheckCircle className="w-5 h-5 text-green-600" />;
      case 'DECLINED':
        return <XCircle className="w-5 h-5 text-red-600" />;
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-600" />;
      default:
        return null;
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const filteredTransactions = filter === 'all' 
    ? transactions 
    : transactions.filter(t => t.status.toLowerCase() === filter);

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
          <h1 className="text-2xl font-bold text-gray-900">Card Activity Feed</h1>
          <p className="text-gray-600 mt-1">Real-time card transaction monitoring</p>
        </div>
        <button
          onClick={handleRefresh}
          className={`mt-4 sm:mt-0 flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-all ${
            refreshing ? 'opacity-50 cursor-not-allowed' : ''
          }`}
          disabled={refreshing}
        >
          <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Live Indicator */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
            <span className="text-sm font-medium text-gray-700">Live Feed Active</span>
          </div>
          <div className="flex gap-2">
            {['all', 'approved', 'declined', 'pending'].map((status) => (
              <button
                key={status}
                onClick={() => setFilter(status)}
                className={`px-3 py-1 rounded-lg text-sm font-medium transition-all ${
                  filter === status
                    ? 'bg-blue-100 text-blue-600'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                {status.charAt(0).toUpperCase() + status.slice(1)}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Transaction Feed */}
      <div className="space-y-4">
        {filteredTransactions.map((transaction) => (
          <div
            key={transaction.id}
            className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start gap-4">
                {/* Status Icon */}
                <div className="mt-1">
                  {getStatusIcon(transaction.status)}
                </div>
                
                {/* Transaction Details */}
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <h3 className="font-semibold text-gray-900 text-lg">
                        {transaction.merchant}
                      </h3>
                      <div className="flex items-center gap-4 mt-1">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium ${getCategoryColor(transaction.category)}`}>
                          {getCategoryIcon(transaction.category)}
                          {transaction.category}
                        </span>
                        <span className="flex items-center gap-1 text-sm text-gray-500">
                          <MapPin className="w-3 h-3" />
                          {transaction.location}
                        </span>
                        <span className="flex items-center gap-1 text-sm text-gray-500">
                          <Clock className="w-3 h-3" />
                          {transaction.time}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Card Info */}
                  <div className="flex items-center gap-2 mt-3">
                    <CreditCard className="w-4 h-4 text-gray-400" />
                    <span className="text-sm text-gray-600">
                      Card ending in {transaction.cardLast4}
                    </span>
                    {transaction.type === 'RECURRING' && (
                      <span className="px-2 py-0.5 bg-blue-100 text-blue-600 text-xs font-medium rounded">
                        Recurring
                      </span>
                    )}
                  </div>

                  {/* Decline Reason */}
                  {transaction.declineReason && (
                    <div className="flex items-start gap-2 mt-3 p-3 bg-red-50 rounded-lg">
                      <AlertCircle className="w-4 h-4 text-red-600 mt-0.5" />
                      <p className="text-sm text-red-700">{transaction.declineReason}</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Amount */}
              <div className="text-right">
                <p className={`text-2xl font-bold ${
                  transaction.status === 'DECLINED' ? 'text-red-600' : 'text-gray-900'
                }`}>
                  {formatCurrency(transaction.amount)}
                </p>
                <p className={`text-sm font-medium mt-1 ${
                  transaction.status === 'APPROVED' ? 'text-green-600' :
                  transaction.status === 'DECLINED' ? 'text-red-600' :
                  'text-yellow-600'
                }`}>
                  {transaction.status}
                </p>
              </div>
            </div>

            {/* Quick Actions */}
            {transaction.status === 'DECLINED' && (
              <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200">
                <button className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium">
                  Report Fraud
                </button>
                <button className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">
                  Retry Transaction
                </button>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Empty State */}
      {filteredTransactions.length === 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
          <CreditCard className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">No transactions found</p>
        </div>
      )}
    </div>
  );
};

export default CardFeed;
