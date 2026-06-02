import { useState } from 'react';
import { motion } from 'framer-motion';
import { Bell, CreditCard, Palette, Shield, User, Check } from 'lucide-react';
import { toast } from 'sonner';
import { useAuthStore } from '../store/authStore';
import * as authApi from '../api/auth';

type Theme = 'light' | 'dark' | 'system';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.05 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };

function Toggle({ enabled, onChange }: { enabled: boolean; onChange: (v: boolean) => void }) {
  return (
    <button
      type="button"
      onClick={() => onChange(!enabled)}
      className={`w-9 h-5 rounded-full relative cursor-pointer transition-colors ${enabled ? 'bg-[--foreground]' : 'bg-[--muted]'}`}
    >
      <span className={`block w-4 h-4 rounded-full bg-white absolute top-0.5 transition-transform ${enabled ? 'translate-x-[18px]' : 'translate-x-0.5'}`} />
    </button>
  );
}

function SettingsCard({ icon: Icon, title, children }: { icon: React.ElementType; title: string; children: React.ReactNode }) {
  return (
    <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 mb-4">
      <div className="flex items-center gap-2.5 mb-5">
        <Icon size={18} className="text-[--muted-foreground]" />
        <h2 className="display-md">{title}</h2>
      </div>
      {children}
    </div>
  );
}

export function SettingsPage() {
  const user = useAuthStore((s) => s.user);
  const [fullName, setFullName] = useState('User');
  const [notifications, setNotifications] = useState({ email: true, schedule: true, weekly: false });
  const [theme, setTheme] = useState<Theme>('system');
  const [twoFactor, setTwoFactor] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleProfileSave = async () => {
    try {
      await authApi.updateProfile({ fullName });
      toast.success('Profile updated');
    } catch {
      toast.error('Failed to update profile');
    }
  };

  const handlePasswordChange = async () => {
    if (!currentPassword || !newPassword) {
      toast.error('Fill in both password fields');
      return;
    }
    if (newPassword.length < 8) {
      toast.error('New password must be at least 8 characters');
      return;
    }
    try {
      await authApi.changePassword({ currentPassword, newPassword });
      toast.success('Password changed');
      setCurrentPassword('');
      setNewPassword('');
    } catch {
      toast.error('Failed to change password. Check current password.');
    }
  };

  return (
    <div>
      <h1 className="display-lg mb-6">Settings</h1>

      <motion.div variants={container} initial="hidden" animate="show">
        <motion.div variants={item}>
          <SettingsCard icon={User} title="Profile">
            <div className="grid grid-cols-1 gap-3 mb-4">
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Full Name</label>
                <input value={fullName} onChange={(e) => setFullName(e.target.value)} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] text-sm outline-none" />
              </div>
            </div>
            <div className="mb-4">
              <label className="label-sm text-[--muted-foreground] mb-1 block">Email</label>
              <input value={user?.email || ''} disabled className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--muted]/30 text-sm outline-none opacity-60 cursor-not-allowed" />
            </div>
            <button onClick={handleProfileSave} className="bg-[--primary] text-[--primary-foreground] px-4 h-9 text-sm font-medium rounded-[--radius-sm] cursor-pointer">Save Changes</button>
          </SettingsCard>
        </motion.div>

        <motion.div variants={item}>
          <SettingsCard icon={CreditCard} title="Account">
            <p className="label-sm text-[--muted-foreground] mb-3">Current plan: <span className="font-medium text-[--foreground]">University Pro</span></p>
            <button className="border border-[--border] px-4 h-9 text-sm font-medium rounded-[--radius-sm] cursor-pointer hover:bg-[--muted] transition-colors">Manage Subscription</button>
          </SettingsCard>
        </motion.div>

        <motion.div variants={item}>
          <SettingsCard icon={Bell} title="Notifications">
            <div className="divide-y divide-[--border]">
              {[
                { key: 'email' as const, label: 'Email notifications' },
                { key: 'schedule' as const, label: 'Schedule change alerts' },
                { key: 'weekly' as const, label: 'Weekly report' },
              ].map((n) => (
                <div key={n.key} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                  <span className="body-md">{n.label}</span>
                  <Toggle enabled={notifications[n.key]} onChange={(v) => setNotifications({ ...notifications, [n.key]: v })} />
                </div>
              ))}
            </div>
          </SettingsCard>
        </motion.div>

        <motion.div variants={item}>
          <SettingsCard icon={Palette} title="Appearance">
            <div className="flex gap-3">
              {(['light', 'dark', 'system'] as const).map((t) => (
                <button
                  key={t}
                  onClick={() => setTheme(t)}
                  className={`flex-1 flex items-center justify-center gap-2 h-12 border rounded-[--radius-sm] text-sm font-medium cursor-pointer transition-colors ${
                    theme === t ? 'border-[--foreground] bg-[--background]' : 'border-[--border] bg-transparent'
                  }`}
                >
                  {theme === t && <Check size={14} />}
                  {t.charAt(0).toUpperCase() + t.slice(1)}
                </button>
              ))}
            </div>
          </SettingsCard>
        </motion.div>

        <motion.div variants={item}>
          <SettingsCard icon={Shield} title="Security">
            <div className="space-y-3 mb-4">
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Current Password</label>
                <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] text-sm outline-none" />
              </div>
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">New Password</label>
                <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] text-sm outline-none" />
              </div>
              <button onClick={handlePasswordChange} className="border border-[--border] px-4 h-9 text-sm font-medium rounded-[--radius-sm] cursor-pointer hover:bg-[--muted] transition-colors">Change Password</button>
            </div>
            <div className="flex items-center justify-between py-2 border-t border-[--border]">
              <div>
                <p className="body-md">Two-factor authentication</p>
                <p className="label-sm text-[--muted-foreground]">Add an extra layer of security to your account</p>
              </div>
              <Toggle enabled={twoFactor} onChange={setTwoFactor} />
            </div>
          </SettingsCard>
        </motion.div>
      </motion.div>
    </div>
  );
}
