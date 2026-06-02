import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { getAnalytics } from '../api/analytics';
import type { AnalyticsResponse } from '../types';
import { TableSkeleton } from '../components/ui/TableSkeleton';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };
const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', '#EF4444', '#EC4899'];

export function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    setError(false);
    try {
      const data = await getAnalytics();
      setAnalytics(data);
    } catch {
      toast.error('Failed to load analytics');
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  if (loading) {
    return (
      <div>
        <h1 className="display-lg mb-6">Analytics</h1>
        <TableSkeleton rows={3} cols={4} />
      </div>
    );
  }

  if (error || !analytics) {
    return (
      <div className="text-center py-16">
        <AlertCircle size={48} className="mx-auto mb-4 text-[--muted-foreground]" />
        <p className="body-lg mb-4">Failed to load analytics data</p>
        <button onClick={fetchData} className="inline-flex items-center gap-2 px-4 h-9 bg-[--primary] text-[--primary-foreground] rounded-[--radius-sm] text-sm font-medium cursor-pointer">
          <RefreshCw size={16} /> Retry
        </button>
      </div>
    );
  }

  const roomChartData = analytics.roomUtilization.map(r => ({
    name: r.roomLabel,
    Utilization: Number(r.utilizationPercent.toFixed(1)),
    Entries: r.entriesCount,
  }));

  const workloadChartData = analytics.instructorWorkload.map(w => ({
    name: w.instructorName.split(' ').slice(0, 2).join(' '),
    Hours: Number(w.estimatedHours.toFixed(1)),
    Sections: w.sectionCount,
  }));

  const summaryChartData = [
    { name: 'Schedules', value: analytics.totalSchedules },
    { name: 'Courses', value: analytics.totalCourses },
    { name: 'Instructors', value: analytics.totalInstructors },
    { name: 'Rooms', value: analytics.totalRooms },
  ].filter(d => d.value > 0);

  return (
    <div>
      <h1 className="display-lg mb-6">Analytics</h1>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Schedules</p>
          <p className="text-3xl font-bold">{analytics.totalSchedules}</p>
        </motion.div>
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Instructors</p>
          <p className="text-3xl font-bold">{analytics.totalInstructors}</p>
        </motion.div>
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Rooms</p>
          <p className="text-3xl font-bold">{analytics.totalRooms}</p>
        </motion.div>
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Courses</p>
          <p className="text-3xl font-bold">{analytics.totalCourses}</p>
        </motion.div>
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Avg Fitness Score</p>
          <p className="text-3xl font-bold">{analytics.averageFitnessScore.toFixed(2)}</p>
        </motion.div>
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Hard Violations</p>
          <p className="text-3xl font-bold text-red-500">{analytics.totalHardViolations}</p>
        </motion.div>
      </motion.div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="display-md mb-5">Room Utilization</h2>
          {roomChartData.length === 0 ? (
            <p className="text-sm text-[--muted-foreground]">No room data available</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={roomChartData} margin={{ top: 5, right: 5, left: -15, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={60} />
                <YAxis tick={{ fontSize: 11 }} unit="%" />
                <Tooltip
                  contentStyle={{ background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '8px', fontSize: '13px' }}
                  formatter={(value) => [`${Number(value).toFixed(1)}%`, 'Utilization']}
                />
                <Bar dataKey="Utilization" fill="#3B82F6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </motion.div>

        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="display-md mb-5">Instructor Workload (Hours)</h2>
          {workloadChartData.length === 0 ? (
            <p className="text-sm text-[--muted-foreground]">No workload data available</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={workloadChartData} margin={{ top: 5, right: 5, left: 0, bottom: 5 }} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis type="number" tick={{ fontSize: 11 }} />
                <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={80} />
                <Tooltip
                  contentStyle={{ background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '8px', fontSize: '13px' }}
                  formatter={(value) => [`${Number(value).toFixed(1)}h`, 'Hours']}
                />
                <Bar dataKey="Hours" fill="#10B981" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </motion.div>
      </motion.div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="display-md mb-5">Resource Distribution</h2>
          {summaryChartData.length === 0 ? (
            <p className="text-sm text-[--muted-foreground]">No data available</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={summaryChartData} cx="50%" cy="50%" outerRadius={90} innerRadius={50} dataKey="value" paddingAngle={3}>
                  {summaryChartData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                </Pie>
                <Tooltip
                  contentStyle={{ background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '8px', fontSize: '13px' }}
                />
                <Legend wrapperStyle={{ fontSize: '12px' }} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </motion.div>

        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="display-md mb-5">Instructor Section Count</h2>
          {workloadChartData.length === 0 ? (
            <p className="text-sm text-[--muted-foreground]">No workload data available</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={workloadChartData} margin={{ top: 5, right: 5, left: -15, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={60} />
                <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                <Tooltip
                  contentStyle={{ background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '8px', fontSize: '13px' }}
                  formatter={(value) => [`${value} sections`, 'Sections']}
                />
                <Bar dataKey="Sections" fill="#8B5CF6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </motion.div>
      </motion.div>
    </div>
  );
}
