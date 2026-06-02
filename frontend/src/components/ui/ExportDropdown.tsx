import { useState, useRef, useEffect } from 'react';
import { Download, FileText, Table2, Image } from 'lucide-react';
import { Button } from './Button';
import { downloadPdf, downloadExcel, downloadPng } from '../../api/schedules';

interface ExportDropdownProps {
  scheduleId: number;
  size?: 'sm' | 'md';
}

export function ExportDropdown({ scheduleId, size = 'sm' }: ExportDropdownProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  return (
    <div ref={ref} className="relative">
      <Button size={size} variant="secondary" onClick={() => setOpen(!open)}>
        <Download size={size === 'sm' ? 16 : 18} /> Export
      </Button>
      {open && (
        <div className="absolute right-0 top-full mt-1 w-44 bg-[--card] border border-[--border] rounded-lg shadow-lg py-1 z-20">
          <button onClick={() => { downloadPdf(scheduleId); setOpen(false); }} className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-[--muted] transition-colors">
            <FileText size={14} /> Export PDF
          </button>
          <button onClick={() => { downloadExcel(scheduleId); setOpen(false); }} className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-[--muted] transition-colors">
            <Table2 size={14} /> Export Excel
          </button>
          <button onClick={() => { downloadPng(scheduleId); setOpen(false); }} className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-[--muted] transition-colors">
            <Image size={14} /> Export PNG
          </button>
        </div>
      )}
    </div>
  );
}