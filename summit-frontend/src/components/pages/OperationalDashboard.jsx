import React, { useState, useEffect } from 'react';
import axios from '../../utils/api';
import {
  Activity,
  Database,
  Users,
  CreditCard,
  TrendingUp,
  Server,
  CheckCircle,
  AlertCircle,
  RefreshCw,
  ArrowRightLeft,
  LineChart
} from 'lucide-react';

const OperationalDashboard = () => {
  const [metrics, setMetrics] = useState(null);
  const [actuatorHealth, setActuatorHealth] = useState(null);
  const [actuatorMetrics, setActuatorMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  const fetchAllData = async () => {
    setLoading(true);
    try {
      // Fetch custom operational metrics
      const metricsRes = await axios.get('/api/metrics/operational');
      setMetrics(metricsRes.data);

      // Fetch Spring Boot Actuator health
      const healthRes = await axios.get('/actuator/health');
      setActuatorHealth(healthRes.data);

      // Fetch JVM metrics from Actuator
      const jvmMemoryRes = await axios.get('/actuator/metrics/jvm.memory.used');
      const systemCpuRes = await axios.get('/actuator/metrics/system.cpu.usage');
      const uptimeRes = await axios.get('/actuator/metrics/process.uptime');

      setActuatorMetrics({
        memory: jvmMemoryRes.data,
        cpu: systemCpuRes.data,
        uptime: uptimeRes.data
      });

      setLastRefresh(new Date());
      setLoading(false);
    } catch (error) {
      console.error('Error fetching operational data:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllData();
    // Auto-refresh every 30 seconds
    const interval = setInterval(fetchAllData, 30000);
    return () => clearInterval(interval);
  }, []);

  const formatBytes = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  };

  const formatUptime = (seconds) => {
    if (!seconds) return '0s';
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${days}d ${hours}h ${minutes}m`;
  };

  const StatCard = ({ title, value, subtitle, icon: Icon, color = 'blue' }) => (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className={`text-3xl font-bold text-${color}-600 mt-2`}>{value}</p>
          {subtitle && <p className="text-sm text-gray-500 mt-1">{subtitle}</p>}
        </div>
        <div className={`bg-${color}-100 rounded-full p-3`}>
          <Icon className={`h-8 w-8 text-${color}-600`} />
        </div>
      </div>
    </div>
  );

  const HealthIndicator = ({ status, component }) => (
    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
      <span className="text-sm font-medium text-gray-700">{component}</span>
      <div className="flex items-center space-x-2">
        {status === 'UP' ? (
          <>
            <CheckCircle className="h-5 w-5 text-green-500" />
            <span className="text-sm font-semibold text-green-600">UP</span>
          </>
        ) : (
          <>
            <AlertCircle className="h-5 w-5 text-red-500" />
            <span className="text-sm font-semibold text-red-600">DOWN</span>
          </>
        )}
      </div>
    </div>
  );

  if (loading || !metrics || !actuatorHealth) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <RefreshCw className="h-12 w-12 text-blue-500 animate-spin mx-auto" />
          <p className="mt-4 text-gray-600">Loading operational metrics...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center">
            <Activity className="h-8 w-8 text-blue-600 mr-3" />
            Operational Dashboard
          </h1>
          <p className="text-gray-600 mt-1">Real-time system monitoring and metrics</p>
        </div>
        <button
          onClick={fetchAllData}
          className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          <RefreshCw className="h-4 w-4" />
          <span>Refresh</span>
        </button>
      </div>

      {/* Last Refresh Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <p className="text-sm text-blue-800">
          <strong>Last Updated:</strong> {lastRefresh.toLocaleString()} | Auto-refreshes every 30 seconds
        </p>
      </div>

      {/* System Health Status */}
      <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
          <Server className="h-6 w-6 text-green-600 mr-2" />
          System Health Status
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <HealthIndicator status={actuatorHealth.status} component="Application" />
          <HealthIndicator
            status={actuatorHealth.components?.db?.status || 'UNKNOWN'}
            component="Database"
          />
          <HealthIndicator
            status={actuatorHealth.components?.diskSpace?.status || 'UP'}
            component="Disk Space"
          />
          <HealthIndicator
            status={actuatorHealth.components?.ping?.status || 'UP'}
            component="Network"
          />
        </div>
      </div>

      {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        <StatCard
          title="Total Users"
          value={metrics.users.total}
          subtitle={`${metrics.users.active} active users`}
          icon={Users}
          color="blue"
        />
        <StatCard
          title="Total Accounts"
          value={metrics.accounts.total}
          subtitle={`${metrics.accounts.frozen} frozen`}
          icon={CreditCard}
          color="green"
        />
        <StatCard
          title="Total Transactions"
          value={metrics.transactions.total}
          subtitle={`${metrics.transactions.accountTransfers} transfers, ${metrics.transactions.stockTrades} trades`}
          icon={ArrowRightLeft}
          color="purple"
        />
        <StatCard
          title="Available Stocks"
          value={`${metrics.stocks.availableStocks}/${metrics.stocks.totalStocks}`}
          subtitle="Stocks in inventory"
          icon={LineChart}
          color="orange"
        />
      </div>

      {/* System Performance */}
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
          <Database className="h-6 w-6 text-blue-600 mr-2" />
          System Performance
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm font-medium text-gray-600">JVM Memory Used</p>
            <p className="text-xl font-bold text-gray-800 mt-2">
              {formatBytes(
                actuatorMetrics?.memory?.measurements?.find((m) => m.statistic === 'VALUE')?.value || 0
              )}
            </p>
            <div className="mt-2 bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 rounded-full h-2"
                style={{
                  width: `${Math.min(
                    ((actuatorMetrics?.memory?.measurements?.find((m) => m.statistic === 'VALUE')
                      ?.value || 0) /
                      (512 * 1024 * 1024)) *
                      100,
                    100
                  )}%`
                }}
              />
            </div>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm font-medium text-gray-600">CPU Usage</p>
            <p className="text-xl font-bold text-gray-800 mt-2">
              {(
                (actuatorMetrics?.cpu?.measurements?.find((m) => m.statistic === 'VALUE')?.value || 0) * 100
              ).toFixed(2)}
              %
            </p>
            <div className="mt-2 bg-gray-200 rounded-full h-2">
              <div
                className="bg-green-600 rounded-full h-2"
                style={{
                  width: `${
                    (actuatorMetrics?.cpu?.measurements?.find((m) => m.statistic === 'VALUE')?.value || 0) *
                    100
                  }%`
                }}
              />
            </div>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm font-medium text-gray-600">Application Uptime</p>
            <p className="text-xl font-bold text-gray-800 mt-2">
              {formatUptime(
                actuatorMetrics?.uptime?.measurements?.find((m) => m.statistic === 'VALUE')?.value || 0
              )}
            </p>
          </div>
        </div>
      </div>

      {/* Database Details */}
      {actuatorHealth.components?.db?.details && (
        <div className="bg-white rounded-lg shadow-lg p-6 mt-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <Database className="h-6 w-6 text-purple-600 mr-2" />
            Database Information
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-purple-50 rounded-lg p-4">
              <p className="text-sm font-medium text-gray-600">Database Type</p>
              <p className="text-lg font-semibold text-gray-800 mt-1">
                {actuatorHealth.components.db.details.database || 'N/A'}
              </p>
            </div>
            <div className="bg-purple-50 rounded-lg p-4">
              <p className="text-sm font-medium text-gray-600">Validation Query</p>
              <p className="text-xs font-mono text-gray-600 mt-1">
                {actuatorHealth.components.db.details.validationQuery || 'isValid()'}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OperationalDashboard;
