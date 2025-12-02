import React, { useState, useEffect } from 'react';
import { 
  TrendingUp,
  TrendingDown,
  PieChart,
  BarChart3,
  DollarSign,
  Briefcase,
  Shield,
  Target,
  Plus,
  ArrowUpRight,
  ArrowDownRight,
  Info
} from 'lucide-react';
import { LineChart, Line, AreaChart, Area, PieChart as RePieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import axios from 'axios';

const Wealth = () => {
  const [portfolios, setPortfolios] = useState([]);
  const [portfolio, setPortfolio] = useState({
    totalValue: 0,
    dayChange: 0,
    dayChangePercent: 0,
    totalGain: 0,
    totalGainPercent: 0
  });

  const [holdings, setHoldings] = useState([]);
  const [allocationData, setAllocationData] = useState([]);
  const [showInvestModal, setShowInvestModal] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPortfolioData();
  }, []);

  const fetchPortfolioData = async () => {
    try {
      const response = await axios.get('/api/wealth');
      const portfolioData = response.data || [];
      setPortfolios(portfolioData);

      if (portfolioData.length > 0) {
        // Calculate total values from all portfolios
        const stockPrice = 450; // Simulated stock ETF price
        const bondPrice = 105; // Simulated bond ETF price

        let totalStockValue = 0;
        let totalBondValue = 0;

        const mappedHoldings = portfolioData.map((p, index) => {
          const stockValue = (p.stockUnits || 0) * stockPrice;
          const bondValue = (p.bondUnits || 0) * bondPrice;
          totalStockValue += stockValue;
          totalBondValue += bondValue;

          return [
            {
              symbol: 'VTI',
              name: `Stock ETF (Account ${p.accountId})`,
              shares: p.stockUnits || 0,
              price: stockPrice,
              value: stockValue,
              change: 0,
              allocation: p.stockPercentage || 0
            },
            {
              symbol: 'AGG',
              name: `Bond ETF (Account ${p.accountId})`,
              shares: p.bondUnits || 0,
              price: bondPrice,
              value: bondValue,
              change: 0,
              allocation: p.bondPercentage || 0
            }
          ];
        }).flat().filter(h => h.shares > 0);

        setHoldings(mappedHoldings);

        const totalValue = totalStockValue + totalBondValue;
        setPortfolio({
          totalValue,
          dayChange: 0,
          dayChangePercent: 0,
          totalGain: 0,
          totalGainPercent: 0
        });

        // Set allocation data
        if (totalValue > 0) {
          setAllocationData([
            { name: 'Stocks', value: Math.round((totalStockValue / totalValue) * 100), color: '#3b82f6' },
            { name: 'Bonds', value: Math.round((totalBondValue / totalValue) * 100), color: '#10b981' }
          ]);
        }
      }

      setLoading(false);
    } catch (error) {
      console.error('Error fetching portfolio data:', error);
      setPortfolios([]);
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const InvestModal = () => {
    const [selectedAccountId, setSelectedAccountId] = useState('');
    const [amount, setAmount] = useState('');

    const handleInvest = async (e) => {
      e.preventDefault();
      try {
        await axios.post('/api/wealth/invest', {
          accountId: parseInt(selectedAccountId),
          amount: parseFloat(amount)
        });
        alert('Investment successful!');
        setShowInvestModal(false);
        fetchPortfolioData(); // Refresh data
      } catch (error) {
        console.error('Error making investment:', error);
        alert(error.response?.data?.message || 'Failed to make investment. Please try again.');
      }
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-6 w-full max-w-md">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">New Investment</h3>
          <form onSubmit={handleInvest} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Select Account
              </label>
              <select
                value={selectedAccountId}
                onChange={(e) => setSelectedAccountId(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                required
              >
                <option value="">Choose account to invest from</option>
                {portfolios.map((portfolio) => (
                  <option key={portfolio.accountId} value={portfolio.accountId}>
                    Account {portfolio.accountId} - Portfolio
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Amount to Invest
              </label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                  placeholder="0.00"
                  min="1"
                  step="0.01"
                  required
                />
              </div>
            </div>
            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={() => setShowInvestModal(false)}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Invest
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Wealth Management</h1>
          <p className="text-gray-600 mt-1">Grow and manage your investment portfolio</p>
        </div>
        <button
          onClick={() => setShowInvestModal(true)}
          className="mt-4 sm:mt-0 flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          New Investment
        </button>
      </div>

      {/* Portfolio Summary */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Portfolio Value</p>
            <Briefcase className="w-4 h-4 text-blue-600" />
          </div>
          <p className="text-2xl font-bold text-gray-900">{formatCurrency(portfolio.totalValue)}</p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Day Change</p>
            {portfolio.dayChange >= 0 ? (
              <TrendingUp className="w-4 h-4 text-green-600" />
            ) : (
              <TrendingDown className="w-4 h-4 text-red-600" />
            )}
          </div>
          <p className={`text-2xl font-bold ${portfolio.dayChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {portfolio.dayChange >= 0 ? '+' : ''}{formatCurrency(portfolio.dayChange)}
          </p>
          <p className={`text-sm ${portfolio.dayChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            ({portfolio.dayChangePercent >= 0 ? '+' : ''}{portfolio.dayChangePercent}%)
          </p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Total Gain</p>
            <BarChart3 className="w-4 h-4 text-green-600" />
          </div>
          <p className="text-2xl font-bold text-green-600">
            +{formatCurrency(portfolio.totalGain)}
          </p>
          <p className="text-sm text-green-600">(+{portfolio.totalGainPercent}%)</p>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Risk Level</p>
            <Shield className="w-4 h-4 text-yellow-600" />
          </div>
          <p className="text-2xl font-bold text-gray-900">Moderate</p>
          <p className="text-sm text-gray-500">Balanced portfolio</p>
        </div>
      </div>

      {/* Allocation Chart */}
      {allocationData.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Asset Allocation</h2>
          <ResponsiveContainer width="100%" height={250}>
            <RePieChart>
              <Pie
                data={allocationData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={(entry) => `${entry.name}: ${entry.value}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {allocationData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </RePieChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Holdings Table */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Holdings</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Asset
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Shares
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Price
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Value
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Change
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Allocation
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {holdings.map((holding) => (
                <tr key={holding.symbol} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{holding.symbol}</p>
                      <p className="text-xs text-gray-500">{holding.name}</p>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">{holding.shares}</td>
                  <td className="px-6 py-4 text-sm text-gray-900">{formatCurrency(holding.price)}</td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{formatCurrency(holding.value)}</td>
                  <td className="px-6 py-4">
                    <div className={`flex items-center gap-1 ${holding.change >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {holding.change >= 0 ? (
                        <ArrowUpRight className="w-4 h-4" />
                      ) : (
                        <ArrowDownRight className="w-4 h-4" />
                      )}
                      <span className="text-sm font-medium">{Math.abs(holding.change)}%</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className="flex-1 bg-gray-200 rounded-full h-2">
                        <div
                          className="bg-blue-600 h-2 rounded-full"
                          style={{ width: `${holding.allocation}%` }}
                        />
                      </div>
                      <span className="text-sm text-gray-600">{holding.allocation}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Investment Modal */}
      {showInvestModal && <InvestModal />}
    </div>
  );
};

export default Wealth;
